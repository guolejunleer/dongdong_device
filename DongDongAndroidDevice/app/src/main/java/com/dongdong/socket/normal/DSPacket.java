package com.dongdong.socket.normal;

import com.dongdong.db.entry.UnlockLogBean;
import com.dongdong.socket.beat.ALinuxData;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * DeviceService数据包打包类
 */
public class DSPacket {
    private ByteInput mByteInput;

    /**
     * 内部接口: 1 如果音视频包太大，则需要分包回调给上层
     */
    public interface DSMediaPacketCallback {
        /**
         * 音视频数据分片包
         *
         * @param mdpkt       音视频数据头指针
         * @param mdpktlength 音视频数据长度
         */
        void onMediaPacket(byte[] mdpkt, int mdpktlength);
    }

    /**
     * 构造函数
     */
    public DSPacket() {
        mByteInput = new ByteInput();
    }

    /**
     * 打包协议头
     */
    private void packetHeader(int cmdflag, short cmdid, short totalseg,
                              short subseg) {
        mByteInput.initOffset();
        mByteInput.putByte((byte) 0xdb); // groupcode
        mByteInput.putShort(cmdid);
        mByteInput.putByte((byte) 1); // version
        mByteInput.putInt(cmdflag);
        mByteInput.putShort(totalseg);
        mByteInput.putShort(subseg);
        mByteInput.putShort((short) 0); // segflag
        mByteInput.putShort((short) 0); // reserved1
        mByteInput.putInt(0); // reserved2
    }

    // 必选实现

    /**
     * 打开音视频数据回应
     *
     * @param cmdflag   与请求消息中cmdflag一致
     * @param result    执行结果
     * @param channelid 设备通道ID
     * @param mediamode 播放类型
     */
    public byte[] playResult(int cmdflag, int result, int channelid, int mediamode) {
        APlatData.debugLog("DSPacket.clazz--->>>playResult: cmdflag:" + cmdflag
                + " result:" + result + " channelid:" + channelid
                + " mediamode:" + mediamode);
        packetHeader(cmdflag, APlatData.CMD_PLAY_RESULT, (short) 1, (short) 1);
        mByteInput.putInt(result);
        mByteInput.putInt(channelid);
        mByteInput.putInt(mediamode);
        return mByteInput.getCopyBytes();
    }

    /**
     * 关闭音视频数据回应
     *
     * @param cmdflag   与请求消息中cmdflag一致
     * @param result    执行结果
     * @param channelid 设备通道ID
     * @param mediamode 播放类型
     */
    public byte[] stopResult(int cmdflag, int result, int channelid, int mediamode) {
        APlatData.debugLog("DSPacket.clazz--->>>stopResult: cmdflag:" + cmdflag
                + " result:" + result + " channelid:" + channelid
                + " mediamode:" + mediamode);
        packetHeader(cmdflag, APlatData.CMD_STOP_RESULT, (short) 1, (short) 1);
        mByteInput.putInt(result);
        mByteInput.putInt(channelid);
        mByteInput.putInt(mediamode);
        return mByteInput.getCopyBytes();
    }

    /**
     * 呼叫通知请求
     *
     * @param cmdflag    请求消息的用户数据，相应的回应消息回传该值
     * @param roomnumber 房号
     */
    public byte[] callRequest(int cmdflag, String roomnumber) {
        APlatData.debugLog("DSPacket.clazz--->>>callRequest: cmdflag:" + cmdflag
                + " roomnumber:" + roomnumber);
        packetHeader(cmdflag, APlatData.CMD_CALL_REQUEST, (short) 1, (short) 1);

        int rnlength = roomnumber.length();
        if (rnlength > APlatData.ROOM_NUMBER_LENGTH) {
            APlatData.debugLog("DSPacket.clazz--->>>callRequest: too long ROOM_NUMBER_LENGTH:"
                    + rnlength);
            return null;
        }

        mByteInput.putString(roomnumber);
        for (int i = 0; i < (APlatData.ROOM_NUMBER_LENGTH - rnlength); i++) {
            byte b = 0;
            mByteInput.putByte(b);
        }
        return mByteInput.getCopyBytes();
    }

    /**
     * 发送音视频数据请求
     *
     * @param sink       音视频数据分包回调指针
     * @param cmdflag    请求消息的用户数据，相应的回应消息回传该值
     * @param channelid  设备通道ID
     * @param sequence   音视频数据总分片索引,第一个分片开始从1依次递增
     * @param iskeyframe 是否为关键帧
     * @param mediadata  音视频包
     * @param datalen    音视频包长度
     * @param audioflag  音视包标识
     */
    public void sendMediaRequest(DSMediaPacketCallback sink, int cmdflag, int channelid,
                                 int sequence, byte iskeyframe, byte[] mediadata,
                                 int datalen, boolean audioflag) {
        if (sink == null) {
            APlatData.debugLog("DSPacket.clazz--->>>sendMediaRequest sink is null");
            return;
        }

        int max_onepktlength = APlatData.MAX_MEDIAREQUEST_LENGTH
                - APlatData.PACKET_HEADER_LENGTH - 13;

        // 单包发送
        if (datalen <= max_onepktlength) {
            short cmdid = APlatData.CMD_SENDMEDIA_REQUEST;
            if (audioflag)
                cmdid = APlatData.CMD_SENDMEDIA_REQUEST_EX;
            packetHeader(cmdflag, cmdid, (short) 1, (short) 1);
            mByteInput.putInt(channelid);
            mByteInput.putInt(sequence);
            mByteInput.putByte(iskeyframe);
            if (audioflag)
                mByteInput.putByte((byte) 0);
            mByteInput.putInt(datalen);
            mByteInput.putBytes(mediadata, 0, datalen);
            sink.onMediaPacket(mByteInput.getBytes(), mByteInput.getLength());
            return;
        }

        // 分包发送
        int totalseg = datalen / max_onepktlength;
        if ((datalen % max_onepktlength) != 0) {
            totalseg++;
        }
        for (int i = 0; i < totalseg; i++) {
            int offset = i * max_onepktlength;
            int byteslength = max_onepktlength;
            if (i == (totalseg - 1)) {
                byteslength = datalen - offset;
            }
            short subseg = (short) (i + 1);
            packetHeader(cmdflag, APlatData.CMD_SENDMEDIA_REQUEST,
                    (short) totalseg, subseg);
            mByteInput.putInt(channelid);
            mByteInput.putInt(sequence);
            mByteInput.putByte(iskeyframe);
            mByteInput.putInt(byteslength);
            mByteInput.putBytes(mediadata, offset, byteslength);
            sink.onMediaPacket(mByteInput.getBytes(), mByteInput.getLength());
        }
    }

    /**
     * 发送音视频数据回应
     *
     * @param cmdflag  与请求消息中cmdflag一致
     * @param sequence 音视频数据总分片索引,第一个分片开始从1依次递增
     */
    public byte[] sendMediaResult(int cmdflag, int sequence) {
        packetHeader(cmdflag, APlatData.CMD_SENDMEDIA_RESULT, (short) 1,
                (short) 1);
        mByteInput.putInt(sequence);
        return mByteInput.getCopyBytes();
    }

    // 可选实现

    /**
     * 获取网口配置信息请求
     *
     * @param cmdflag 请求消息的用户数据，相应的回应消息回传该值
     */
    public byte[] getNetRequest(int cmdflag) {
        APlatData.debugLog("DSPacket.class getNetRequest cmdflag:" + cmdflag);
        packetHeader(cmdflag, APlatData.CMD_GETNET_REQUEST, (short) 1, (short) 1);
        return mByteInput.getCopyBytes();
    }

    /**
     * 设置网口配置信息请求
     *
     * @param cmdflag  请求消息的用户数据，相应的回应消息回传该值
     * @param netparam 网络参数
     */
    public byte[] setNetRequest(int cmdflag, InfoNetParam netparam) {
        APlatData.debugLog("DSPacket.clazz--->>>setNetRequest cmdflag:" + cmdflag);
        packetHeader(cmdflag, APlatData.CMD_SETNET_REQUEST, (short) 1, (short) 1);
        mByteInput.putByte(netparam.nettype);
        mByteInput.putByte(netparam.ethaddrtype);
        mByteInput.putInt(netparam.ethip);
        mByteInput.putInt(netparam.ethmask);
        mByteInput.putInt(netparam.defaultgw);
        mByteInput.putByte(netparam.dnsaddrtype);
        mByteInput.putInt(netparam.primarydns);
        mByteInput.putInt(netparam.secondarydns);
        byte[] mac = netparam.getMacAddress();
        APlatData.debugLog("DSPacket.clazz--->>>setNetRequest mac:" + mac.length);
        for (int i = 0; i < mac.length; i++) {
            APlatData.debugLog("DSPacket.clazz--->>>setNetRequest mac[" + i + "]:"
                    + mac[i]);
        }
        mByteInput.putBytes(mac);
        return mByteInput.getCopyBytes();
    }

    /**
     * 挂断通话请求
     *
     * @param cmdflag 请求消息的用户数据，相应的回应消息回传该值
     * @param reason  挂断的原因：1=用户强制挂断 2=软件超时挂断
     */
    public byte[] handUpRequest(int cmdflag, int reason) {
        APlatData.debugLog("DSPacket.clazz--->>>handUpRequest cmdflag:" + cmdflag
                + " reason:" + reason);
        packetHeader(cmdflag, APlatData.CMD_HANGUP_REQUEST, (short) 1, (short) 1);
        mByteInput.putInt(reason);
        return mByteInput.getCopyBytes();
    }


    /**
     * 检查刷卡卡号请求
     *
     * @param cmdflag    请求消息的用户数据，相应的回应消息回传该值
     * @param cardnumber 卡号
     * @return data 返回打包数据
     */
    public byte[] isValidCardRequest(int cmdflag, String cardnumber) {
        APlatData.debugLog("DSPacket.clazz--->>>isValidCardRequest: cmdflag:"
                + cmdflag + " cardnumber:" + cardnumber);
        packetHeader(cmdflag, APlatData.CMD_CARD_REQUEST, (short) 1, (short) 1);

        int cnlength = cardnumber.length();
        if (cnlength > APlatData.CARD_NUMBER_LENGTH) {
            APlatData.debugLog("DSPacket.clazz isValidCardRequest: too long CARD_NUMBER_LENGTH:"
                    + cnlength);
            return null;
        }

        mByteInput.putString(cardnumber);
        for (int i = 0; i < (APlatData.CARD_NUMBER_LENGTH - cnlength); i++) {
            byte b = 0;
            mByteInput.putByte(b);
        }
        return mByteInput.getCopyBytes();
    }


    /**
     * 检查开门密码请求
     *
     * @param cmdflag    请求消息的用户数据，相应的回应消息回传该值
     * @param roomnumber 房号
     * @param pwd        开门密码
     * @return data 返回打包数据
     */
    public byte[] isValidPasswordRequest(int cmdflag, String roomnumber,
                                         String pwd) {
        APlatData.debugLog("DSPacket.clazz--->>>isValidPasswordRequest: cmdflag:"
                + cmdflag + " roomnumber:" + roomnumber + " pwd:" + pwd);
        packetHeader(cmdflag, APlatData.CMD_PWD_REQUEST, (short) 1, (short) 1);

        int rnlength = roomnumber.length();
        if (rnlength > APlatData.ROOM_NUMBER_LENGTH) {
            APlatData.debugLog("DSPacket.clazz isValidPasswordRequest: too long ROOM_NUMBER_LENGTH:"
                    + rnlength);
            return null;
        }

        mByteInput.putString(roomnumber);
        for (int i = 0; i < (APlatData.ROOM_NUMBER_LENGTH - rnlength); i++) {
            byte b = 0;
            mByteInput.putByte(b);
        }

        int pwdlength = pwd.length();
        if (pwdlength > APlatData.UNLOCK_PWD_LENGTH) {
            APlatData.debugLog("DSPacket.clazz isValidPasswordRequest: too long UNLOCK_PWD_LENGTH:"
                    + pwdlength);
            return null;
        }

        mByteInput.putString(pwd);
        for (int i = 0; i < (APlatData.UNLOCK_PWD_LENGTH - pwdlength); i++) {
            byte b = 0;
            mByteInput.putByte(b);
        }
        return mByteInput.getCopyBytes();
    }


    /**
     * 获取平台时间请求
     *
     * @param cmdflag 请求消息的用户数据，相应的回应消息回传该值
     * @return data 返回打包数据
     */
    public byte[] getTimestamp(int cmdflag) {
        APlatData.debugLog("DSPacket.clazz--->>>getTimestamp: cmdflag:" + cmdflag);
        packetHeader(cmdflag, APlatData.CMD_GET_TIMESTAMP_REQUEST, (short) 1, (short) 1);
        return mByteInput.getCopyBytes();
    }

    /**
     * 推送消息透传回应
     *
     * @return data 返回打包数据
     */
    public byte[] TunnelPushResult() {
        APlatData.debugLog("DSPacket.clazz--->>>TunnelPushResult");

        return null;
    }

    /**
     * 客户端信令透传回应
     *
     * @return data 返回打包数据
     */
    public byte[] TunnelCmdResult() {
        APlatData.debugLog("DSPacket.clazz--->>>TunnelCmdResult");

        return null;
    }

    /**
     * 文件上传平台请求
     *
     * @return data 返回打包数据
     */
    public byte[] uploadRequest() {
        APlatData.debugLog("DSPacket.clazz--->>>uploadRequest");

        return null;
    }

    /**
     * 拨打电话回应
     *
     * @param cmdflag     请求消息的用户数据，相应的回应消息回传该值
     * @param roomnumber  房号
     * @param phonenumber 电话号码
     * @param result      结果 0-开始拨打 1-正在响铃 2-已接通 3-通话结束 4-呼叫超时 5-通话超时
     * @return data 返回打包数据
     */
    public byte[] phoneCallResult(int cmdflag, String roomnumber,
                                  String phonenumber, int result) {
        APlatData.debugLog("DSPacket.clazz--->>> phoneCallResult roomnumber:"
                + roomnumber + " phonenumber:" + phonenumber + " result:"
                + result);
        packetHeader(cmdflag, APlatData.CMD_PHONECALL_RESULT, (short) 1,
                (short) 1);

        int rnlength = roomnumber.length();
        if (rnlength > APlatData.ROOM_NUMBER_LENGTH) {
            APlatData.debugLog("DSPacket.clazz--->>>phoneCallResult: too long ROOM_NUMBER_LENGTH:"
                    + rnlength);
            return null;
        }
        mByteInput.putString(roomnumber);
        for (int i = 0; i < (APlatData.ROOM_NUMBER_LENGTH - rnlength); i++) {
            byte b = 0;
            mByteInput.putByte(b);
        }

        int pnlength = phonenumber.length();
        if (pnlength > APlatData.PHONE_NUMBER_LENGTH) {
            APlatData.debugLog("DSPacket.clazz--->>>phoneCallResult: too long PHONE_NUMBER_LENGTH:"
                    + pnlength);
            return null;
        }
        mByteInput.putString(phonenumber);
        for (int i = 0; i < (APlatData.PHONE_NUMBER_LENGTH - pnlength); i++) {
            byte b = 0;
            mByteInput.putByte(b);
        }

        mByteInput.putInt(result);
        return mByteInput.getCopyBytes();
    }

    /**
     * 开门记录结果上传数据打包
     *
     * @return data 返回打包数据
     */
    public byte[] unLockTypeResult(int cmdflag, int dataType, List<UnlockLogBean> unlocks) {
        if (null == unlocks) {
            APlatData.debugLog("DSPacket.clazz--->>>unLockTypeResult UnlockLogBeans is null");
        }

        int count = unlocks.size();
        UnlockLogBean firstBean = unlocks.get(0);
        int firstId = firstBean.getId().intValue();

        APlatData.debugLog("DSPacket.clazz--->>>unLockTypeResult unlockCount:" + count
                + ";firstBeanId:" + firstId);
        packetHeader(firstId, APlatData.CMD_UNLOCKTYPE_REQUEST, (short) 1, (short) 1);
        mByteInput.putInt(dataType);
        mByteInput.putInt(count);
        APlatData.debugLog("DSPacket.clazz unLockTypeResult ============:" + count
                + ";firstBeanId:" + firstId);
        for (int i = 0; i < count; i++) {
            UnlockLogBean bean = unlocks.get(i);
            APlatData.debugLog("DSPacket.clazz unLockTypeResult bean.getId():" + bean.getId());
            mByteInput.putInt(bean.getUnlockType());
            mByteInput.putInt(bean.getDeviceId());
            mByteInput.putInt(bean.getRoomId());
            mByteInput.putInt(bean.getUserId());
            int carNumLength = bean.getCardOrPhoneNum().length();
            if (carNumLength > APlatData.CARD_NUMBER_LENGTH) {
                APlatData.debugLog("DSPacket.clazz unLockTypeResult: too long length:"
                        + carNumLength);
                return null;
            }
            mByteInput.putString(bean.getCardOrPhoneNum());
            for (int j = 0; j < (APlatData.CARD_NUMBER_LENGTH - carNumLength); j++) {
                byte b = 0;
                mByteInput.putByte(b);
            }
            mByteInput.putInt(bean.getUnlockTime());
        }
        return mByteInput.getCopyBytes();
    }

    public byte[] getRoomCardInfoRequest(int cmdflag, CopyOnWriteArraySet<Integer> roomIDSet) {
        ALinuxData.debugLog("DSPacket.clazz--->>>getRoomCardInfoRequest count:"
                + roomIDSet.size());
        packetHeader(cmdflag, APlatData.CMD_GET_ROOMCARD_INFO_REQUEST, (short) 1,
                (short) 1);
        int count = 0;
        int cutIndex = mByteInput.getLength();
        mByteInput.putInt(count);

        for (Integer id : roomIDSet) {
            if (mByteInput.getLength() < 1400) {
                int roomId = id;
                mByteInput.putInt(roomId);
                if (count < 10) {
                    ALinuxData.debugLog("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$roomId:"
                            + roomId + ",it is first 10 roomid");
                }
                count++;
            } else {
                break;
            }
        }
        int curLen = mByteInput.getLength();
        mByteInput.setOffset(cutIndex);
        mByteInput.putInt(count);
        mByteInput.setOffset(curLen);
        return mByteInput.getCopyBytes();
    }
}
