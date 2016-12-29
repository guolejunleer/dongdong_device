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
         * @param mdPkt       音视频数据头指针
         * @param mdPktLength 音视频数据长度
         */
        void onMediaPacket(byte[] mdPkt, int mdPktLength);
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
     * @param cmdFlag   与请求消息中cmdflag一致
     * @param result    执行结果
     * @param channelId 设备通道ID
     * @param mediaMode 播放类型
     */
    public byte[] playResult(int cmdFlag, int result, int channelId, int mediaMode) {
        APlatData.debugLog("DSPacket.clazz--->>>playResult: cmdFlag:" + cmdFlag
                + " result:" + result + " channelId:" + channelId
                + " mediaMode:" + mediaMode);
        packetHeader(cmdFlag, APlatData.CMD_PLAY_RESULT, (short) 1, (short) 1);
        mByteInput.putInt(result);
        mByteInput.putInt(channelId);
        mByteInput.putInt(mediaMode);
        return mByteInput.getCopyBytes();
    }

    /**
     * 关闭音视频数据回应
     *
     * @param cmdFlag   与请求消息中cmdflag一致
     * @param result    执行结果
     * @param channelId 设备通道ID
     * @param mediaMode 播放类型
     */
    public byte[] stopResult(int cmdFlag, int result, int channelId, int mediaMode) {
        APlatData.debugLog("DSPacket.clazz--->>>stopResult: cmdFlag:" + cmdFlag
                + " result:" + result + " channelId:" + channelId
                + " mediaMode:" + mediaMode);
        packetHeader(cmdFlag, APlatData.CMD_STOP_RESULT, (short) 1, (short) 1);
        mByteInput.putInt(result);
        mByteInput.putInt(channelId);
        mByteInput.putInt(mediaMode);
        return mByteInput.getCopyBytes();
    }

    /**
     * 呼叫通知请求
     *
     * @param cmdFlag    请求消息的用户数据，相应的回应消息回传该值
     * @param roomNumber 房号
     */
    public byte[] callRequest(int cmdFlag, String roomNumber) {
        APlatData.debugLog("DSPacket.clazz--->>>callRequest: cmdFlag:" + cmdFlag
                + " roomNumber:" + roomNumber);
        packetHeader(cmdFlag, APlatData.CMD_CALL_REQUEST, (short) 1, (short) 1);

        int rnLength = roomNumber.length();
        if (rnLength > APlatData.ROOM_NUMBER_LENGTH) {
            APlatData.debugLog("DSPacket.clazz--->>>callRequest: too long ROOM_NUMBER_LENGTH:"
                    + rnLength);
            return null;
        }

        mByteInput.putString(roomNumber);
        for (int i = 0; i < (APlatData.ROOM_NUMBER_LENGTH - rnLength); i++) {
            byte b = 0;
            mByteInput.putByte(b);
        }
        return mByteInput.getCopyBytes();
    }

    /**
     * 发送音视频数据请求
     *
     * @param sink       音视频数据分包回调指针
     * @param cmdFlag    请求消息的用户数据，相应的回应消息回传该值
     * @param channelId  设备通道ID
     * @param sequence   音视频数据总分片索引,第一个分片开始从1依次递增
     * @param isKeyFrame 是否为关键帧
     * @param mediaData  音视频包
     * @param dataLen    音视频包长度
     * @param audioFlag  音视包标识
     */
    public void sendMediaRequest(DSMediaPacketCallback sink, int cmdFlag, int channelId,
                                 int sequence, byte isKeyFrame, byte[] mediaData,
                                 int dataLen, boolean audioFlag) {
        if (sink == null) {
            APlatData.debugLog("DSPacket.clazz--->>>sendMediaRequest sink is null");
            return;
        }

        int max_onePktLength = APlatData.MAX_MEDIAREQUEST_LENGTH
                - APlatData.PACKET_HEADER_LENGTH - 13;

        // 单包发送
        if (dataLen <= max_onePktLength) {
            short cmdId = APlatData.CMD_SENDMEDIA_REQUEST;
            if (audioFlag)
                cmdId = APlatData.CMD_SENDMEDIA_REQUEST_EX;
            packetHeader(cmdFlag, cmdId, (short) 1, (short) 1);
            mByteInput.putInt(channelId);
            mByteInput.putInt(sequence);
            mByteInput.putByte(isKeyFrame);
            if (audioFlag)
                mByteInput.putByte((byte) 0);
            mByteInput.putInt(dataLen);
            mByteInput.putBytes(mediaData, 0, dataLen);
            sink.onMediaPacket(mByteInput.getBytes(), mByteInput.getLength());
            return;
        }

        // 分包发送
        int totalSeg = dataLen / max_onePktLength;
        if ((dataLen % max_onePktLength) != 0) {
            totalSeg++;
        }
        for (int i = 0; i < totalSeg; i++) {
            int offset = i * max_onePktLength;
            int bytesLength = max_onePktLength;
            if (i == (totalSeg - 1)) {
                bytesLength = dataLen - offset;
            }
            short subSeg = (short) (i + 1);
            packetHeader(cmdFlag, APlatData.CMD_SENDMEDIA_REQUEST,
                    (short) totalSeg, subSeg);
            mByteInput.putInt(channelId);
            mByteInput.putInt(sequence);
            mByteInput.putByte(isKeyFrame);
            mByteInput.putInt(bytesLength);
            mByteInput.putBytes(mediaData, offset, bytesLength);
            sink.onMediaPacket(mByteInput.getBytes(), mByteInput.getLength());
        }
    }

    /**
     * 发送音视频数据回应
     *
     * @param cmdFlag  与请求消息中cmdFlag一致
     * @param sequence 音视频数据总分片索引,第一个分片开始从1依次递增
     */
    public byte[] sendMediaResult(int cmdFlag, int sequence) {
        packetHeader(cmdFlag, APlatData.CMD_SENDMEDIA_RESULT, (short) 1,
                (short) 1);
        mByteInput.putInt(sequence);
        return mByteInput.getCopyBytes();
    }

    // 可选实现

    /**
     * 获取网口配置信息请求
     *
     * @param cmdFlag 请求消息的用户数据，相应的回应消息回传该值
     */
    public byte[] getNetRequest(int cmdFlag) {
        APlatData.debugLog("DSPacket.class getNetRequest cmdFlag:" + cmdFlag);
        packetHeader(cmdFlag, APlatData.CMD_GETNET_REQUEST, (short) 1, (short) 1);
        return mByteInput.getCopyBytes();
    }

    /**
     * 设置网口配置信息请求
     *
     * @param cmdFlag  请求消息的用户数据，相应的回应消息回传该值
     * @param netParam 网络参数
     */
    public byte[] setNetRequest(int cmdFlag, InfoNetParam netParam) {
        APlatData.debugLog("DSPacket.clazz--->>>setNetRequest cmdFlag:" + cmdFlag);
        packetHeader(cmdFlag, APlatData.CMD_SETNET_REQUEST, (short) 1, (short) 1);
        mByteInput.putByte(netParam.nettype);
        mByteInput.putByte(netParam.ethaddrtype);
        mByteInput.putInt(netParam.ethip);
        mByteInput.putInt(netParam.ethmask);
        mByteInput.putInt(netParam.defaultgw);
        mByteInput.putByte(netParam.dnsaddrtype);
        mByteInput.putInt(netParam.primarydns);
        mByteInput.putInt(netParam.secondarydns);
        byte[] mac = netParam.getMacAddress();
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
     * @param cmdFlag 请求消息的用户数据，相应的回应消息回传该值
     * @param reason  挂断的原因：1=用户强制挂断 2=软件超时挂断
     */
    public byte[] handUpRequest(int cmdFlag, int reason) {
        APlatData.debugLog("DSPacket.clazz--->>>handUpRequest cmdFlag:" + cmdFlag
                + " reason:" + reason);
        packetHeader(cmdFlag, APlatData.CMD_HANGUP_REQUEST, (short) 1, (short) 1);
        mByteInput.putInt(reason);
        return mByteInput.getCopyBytes();
    }


    /**
     * 检查刷卡卡号请求
     *
     * @param cmdFlag    请求消息的用户数据，相应的回应消息回传该值
     * @param cardNumber 卡号
     * @return data 返回打包数据
     */
    public byte[] isValidCardRequest(int cmdFlag, String cardNumber) {
        APlatData.debugLog("DSPacket.clazz--->>>isValidCardRequest: cmdFlag:"
                + cmdFlag + " cardNumber:" + cardNumber);
        packetHeader(cmdFlag, APlatData.CMD_CARD_REQUEST, (short) 1, (short) 1);

        int cnLength = cardNumber.length();
        if (cnLength > APlatData.CARD_NUMBER_LENGTH) {
            APlatData.debugLog("DSPacket.clazz isValidCardRequest: too long CARD_NUMBER_LENGTH:"
                    + cnLength);
            return null;
        }

        mByteInput.putString(cardNumber);
        for (int i = 0; i < (APlatData.CARD_NUMBER_LENGTH - cnLength); i++) {
            byte b = 0;
            mByteInput.putByte(b);
        }
        return mByteInput.getCopyBytes();
    }


    /**
     * 检查开门密码请求
     *
     * @param cmdFlag    请求消息的用户数据，相应的回应消息回传该值
     * @param roomNumber 房号
     * @param pwd        开门密码
     * @return data 返回打包数据
     */
    public byte[] isValidPasswordRequest(int cmdFlag, String roomNumber,
                                         String pwd) {
        APlatData.debugLog("DSPacket.clazz--->>>isValidPasswordRequest: cmdFlag:"
                + cmdFlag + " roomNumber:" + roomNumber + " pwd:" + pwd);
        packetHeader(cmdFlag, APlatData.CMD_PWD_REQUEST, (short) 1, (short) 1);

        int rnLength = roomNumber.length();
        if (rnLength > APlatData.ROOM_NUMBER_LENGTH) {
            APlatData.debugLog("DSPacket.clazz isValidPasswordRequest: too long ROOM_NUMBER_LENGTH:"
                    + rnLength);
            return null;
        }

        mByteInput.putString(roomNumber);
        for (int i = 0; i < (APlatData.ROOM_NUMBER_LENGTH - rnLength); i++) {
            byte b = 0;
            mByteInput.putByte(b);
        }

        int pwdLength = pwd.length();
        if (pwdLength > APlatData.UNLOCK_PWD_LENGTH) {
            APlatData.debugLog("DSPacket.clazz isValidPasswordRequest: too long UNLOCK_PWD_LENGTH:"
                    + pwdLength);
            return null;
        }

        mByteInput.putString(pwd);
        for (int i = 0; i < (APlatData.UNLOCK_PWD_LENGTH - pwdLength); i++) {
            byte b = 0;
            mByteInput.putByte(b);
        }
        return mByteInput.getCopyBytes();
    }


    /**
     * 获取平台时间请求
     *
     * @param cmdFlag 请求消息的用户数据，相应的回应消息回传该值
     * @return data 返回打包数据
     */
    public byte[] getTimestamp(int cmdFlag) {
        APlatData.debugLog("DSPacket.clazz--->>>getTimestamp: cmdFlag:" + cmdFlag);
        packetHeader(cmdFlag, APlatData.CMD_GET_TIMESTAMP_REQUEST, (short) 1, (short) 1);
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
     * @param cmdFlag     请求消息的用户数据，相应的回应消息回传该值
     * @param roomNumber  房号
     * @param phoneNumber 电话号码
     * @param result      结果 0-开始拨打 1-正在响铃 2-已接通 3-通话结束 4-呼叫超时 5-通话超时
     * @return data 返回打包数据
     */
    public byte[] phoneCallResult(int cmdFlag, String roomNumber,
                                  String phoneNumber, int result) {
        APlatData.debugLog("DSPacket.clazz--->>> phoneCallResult roomNumber:"
                + roomNumber + " phoneNumber:" + phoneNumber + " result:"
                + result);
        packetHeader(cmdFlag, APlatData.CMD_PHONECALL_RESULT, (short) 1,
                (short) 1);

        int rnLength = roomNumber.length();
        if (rnLength > APlatData.ROOM_NUMBER_LENGTH) {
            APlatData.debugLog("DSPacket.clazz--->>>phoneCallResult: too long ROOM_NUMBER_LENGTH:"
                    + rnLength);
            return null;
        }
        mByteInput.putString(roomNumber);
        for (int i = 0; i < (APlatData.ROOM_NUMBER_LENGTH - rnLength); i++) {
            byte b = 0;
            mByteInput.putByte(b);
        }

        int pnLength = phoneNumber.length();
        if (pnLength > APlatData.PHONE_NUMBER_LENGTH) {
            APlatData.debugLog("DSPacket.clazz--->>>phoneCallResult: too long PHONE_NUMBER_LENGTH:"
                    + pnLength);
            return null;
        }
        mByteInput.putString(phoneNumber);
        for (int i = 0; i < (APlatData.PHONE_NUMBER_LENGTH - pnLength); i++) {
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
    public byte[] unLockTypeResult(int cmdFlag, int dataType, List<UnlockLogBean> unlocks) {
        int count;
        if (null == unlocks) {
            count = 0;
            APlatData.debugLog("DSPacket.clazz--->>>unLockTypeResult UnlockLogBeans is null");
        } else {
            count = unlocks.size();
        }
        APlatData.debugLog("DSPacket.clazz--->>>unLockTypeResult unlockCount:" + count);
        if (dataType == APlatData.UNLOCK_TIME_DATA) {
            packetHeader(cmdFlag, APlatData.CMD_UNLOCK_TYPE_TIME_DATA_REQUEST,
                    (short) 1, (short) 1);
        } else {
            packetHeader(cmdFlag, APlatData.CMD_UNLOCK_TYPE_HISTORY_DATA_REQUEST,
                    (short) 1, (short) 1);
            mByteInput.putInt(count);
        }
        for (int i = 0; i < count; i++) {
            UnlockLogBean bean = unlocks.get(i);
            APlatData.debugLog("DSPacket.clazz unLockTypeResult bean.getId():" + bean.getId());
            mByteInput.putInt(bean.getId().intValue());
            mByteInput.putInt(bean.getUnlockType());
            int carNumLength = bean.getCardOrPhoneNum().length();
            if (carNumLength > APlatData.CARD_NUMBER_LENGTH) {
                APlatData.debugLog("DSPacket.clazz unLockTypeResult:carNumLength too long length:"
                        + carNumLength);
                return null;
            }
            mByteInput.putString(bean.getCardOrPhoneNum());
            for (int j = 0; j < (APlatData.CARD_NUMBER_LENGTH - carNumLength); j++) {
                byte b = 0;
                mByteInput.putByte(b);
            }
            mByteInput.putInt(bean.getUnlockTime());
            int roomNumLength = bean.getRoomNum().length();
            if (roomNumLength > APlatData.ROOM_NUMBER_LENGTH) {
                APlatData.debugLog("DSPacket.clazz unLockTypeResult:roomNumLength too long length:"
                        + roomNumLength);
                return null;
            }
            mByteInput.putString(bean.getRoomNum());
            for (int j = 0; j < (APlatData.ROOM_NUMBER_LENGTH - roomNumLength); j++) {
                byte b = 0;
                mByteInput.putByte(b);
            }
        }
        return mByteInput.getCopyBytes();
    }

    public byte[] getRoomCardInfoRequest(int cmdFlag, CopyOnWriteArraySet<Integer> roomIDSet) {
        ALinuxData.debugLog("DSPacket.clazz--->>>getRoomCardInfoRequest count:"
                + roomIDSet.size());
        packetHeader(cmdFlag, APlatData.CMD_GET_ROOMCARD_INFO_REQUEST, (short) 1,
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
                            + roomId + ",it is first 10 roomId");
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

    /**
     * 获取访客留影配置请求
     *
     * @param cmdFlag 请求消息的用户数据，相应的回应消息回传该值
     * @return data    返回打包数据
     */
    public byte[] getVisitorPicCfgRequest(int cmdFlag) {
        APlatData.debugLog("DSPacket.clazz--->>>getVisitorPicCfgRequest: cmdFlag:" + cmdFlag);
        packetHeader(cmdFlag, APlatData.CMD_GET_VISITOR_PIC_CFG_REQUEST, (short) 1, (short) 1);
        return mByteInput.getCopyBytes();
    }

    /**
     * 设置访客留影配置请求
     *
     * @param cmdFlag 请求消息的用户数据，相应的回应消息回传该值
     * @param configure 配置参数
     * @return
     */
    public byte[] setVisitorPicCfgRequest(int cmdFlag,int configure){
        APlatData.debugLog("DSPacket.clazz--->>>setVisitorPicCfgRequest cmdFlag:" + cmdFlag
                + " configure:" + configure);
        packetHeader(cmdFlag, APlatData.CMD_SET_VISITOR_PIC_CFG_REQUEST, (short) 1, (short) 1);
        mByteInput.putInt(configure);
        return mByteInput.getCopyBytes();
    }
}
