package com.dongdong.socket.normal;

import com.dongdong.DeviceApplication;
import com.dongdong.bean.RoomInfoBean;
import com.dongdong.db.entry.RoomCardBean;
import com.dongdong.sdk.DongDongCenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * DeviceService数据包解析类
 */
public class DSParse {

    private ByteOutput mByteOutput;
    private volatile DeviceServiceCallback mCallback;

    /**
     * 协议头
     */
    private byte groupCode = 0;
    private short cmdId = 0;
    private byte version = 0;
    private int cmdFlag = 0;
    private short totalSeg = 0;
    private short subSeg = 0;
    private short segFlag = 0;
    private short reserved1 = 0;
    private int reserved2 = 0;

    /**
     * 媒体类型
     */
    private int mMediaMode = 0;

    /**
     * 状态
     */
    private int mStatus = APlatData.STATUS_ORIGINAL;

    /**
     * 构造函数
     */
    public DSParse() {
        mByteOutput = new ByteOutput();
    }

    /**
     * 解析收到DeviceService的数据包
     *
     * @param bytes  数据字节组
     * @param length 数据长度
     * @throws IOException
     */
    public void parseReceiveData(byte[] bytes, int length) throws IOException {
        APlatData.debugLog("//////////////////DSParse.clazz parseReceiveData-->>>cmdId:"
                + cmdId + ",length:" + length + "///////////////");
        if (length < APlatData.PACKET_HEADER_LENGTH) {
            APlatData.debugLog("DSParse.clazz parseReceiveData bytes length(" +
                    length + ") too short");
            return;
        }
        if (mByteOutput == null) {
            APlatData.debugLog("DSParse.clazz ByteOutput is null!!!");
            return;
        }
        mByteOutput.setBytes(bytes, length);
        if (!parseHeader()) {
            return;
        }
        switch (cmdId) {
            case APlatData.CMD_PLAY_REQUEST:
                onPlayRequest();
                break;
            case APlatData.CMD_STOP_REQUEST:
                onStopRequest();
                break;
            case APlatData.CMD_SENDMEDIA_REQUEST:
                onSendMediaRequest();
                break;
            case APlatData.CMD_SETVOLUME_REQUEST:
                onSetVolumeRequest();
                break;
            case APlatData.CMD_SETVIDEOMODE_REQUEST:
                onSetVideoModeRequest();
                break;
            case APlatData.CMD_SETVIDEOATTR_REQUEST:
                onSetVideoAttrRequest();
                break;
            case APlatData.CMD_UNLOCK_REQUEST:
                onUnlockRequest();
                break;
            case APlatData.CMD_GETNET_RESULT:
                onGetNetResult();
                break;
            case APlatData.CMD_SETNET_RESULT:
                onSetNetResult();
                break;
            case APlatData.CMD_DELCARD_REQUEST:
                onDelCardRequest();
                break;
            case APlatData.CMD_TUNNEL_PUSH_REQUEST:
                onTunnelPushRequest();
                break;
            case APlatData.CMD_TUNNEL_CMD_REQUEST:
                onTunnelCmdRequest();
                break;
            case APlatData.CMD_CALL_RESULT:
                onQueryRoomResult();
                break;
            case APlatData.CMD_CARD_RESULT:
                onCheckCardResult();
                break;
            case APlatData.CMD_PWD_RESULT:
                onCheckPasswordResult();
                break;
            case APlatData.CMD_PHONECALL_REQUEST:
                onDialRequest();
                break;
            case APlatData.CMD_STOPPHONECALL_REQUEST:
                onStopPhoneCallRequest();
                break;
            case APlatData.CMD_DISABLE_PHONE_CALL_REQUEST:
                onDisablePhoneCallRequest();
                break;
            case APlatData.CMD_UNLOCK_TYPE_TIME_DATA_RESULT:
                onUnLockTimeDataResult();
                break;
            case APlatData.CMD_GET_UNLOCK_TYPE__HISTORY_DATA_REQUEST:
                onGetHistoryUnLockRecordRequest();
                break;
            case APlatData.CMD_UNLOCK_TYPE_HISTORY_DATA_RESULT:
                onUnlockHistoryTimeResult();
                break;
            case APlatData.CMD_GET_TIMESTAMP_RESULT:
                onGetTimestampResult();
                break;
            case APlatData.CMD_GET_ROOMCARD_INFO_RESULT:
                onGetRoomCardInfoResult();
                break;
            default:
                APlatData.debugLog("DsParse.clazz no match cmdId:"
                        + cmdId);
                break;
        }
    }

    /**
     * 设置解析数据包之后的回调接口
     *
     * @param callback 回调实体
     */
    public void setDSCallback(DeviceServiceCallback callback) {
        APlatData.debugLog("DsParse.clazz setDSCallback DeviceServiceCallback=" + callback);
        this.mCallback = callback;
    }

    /**
     * 设置状态 挂断则将状态置为DSDefine.STATUS_CALL_END
     *
     * @param status 状态值 见DSDefine.java
     */
    public void setDSStatus(int status) {
        APlatData.debugLog("DSParse.clazz -->>setDSStatus:" + status);
        this.mStatus = status;
    }

    /**
     * 解析协议头
     */
    private boolean parseHeader() {
        groupCode = mByteOutput.getByte();
        cmdId = mByteOutput.getShort();
        version = mByteOutput.getByte();
        cmdFlag = mByteOutput.getInt();
        totalSeg = mByteOutput.getShort();
        subSeg = mByteOutput.getShort();
        segFlag = mByteOutput.getShort();
        reserved1 = mByteOutput.getShort();
        reserved2 = mByteOutput.getInt();

        if (groupCode != (byte) 0xdb) {
            APlatData.debugLog("DSParse.clazz--->>>parseHeader wrong groupCode:"
                    + groupCode);
            return false;
        }
        return true;
    }

    /**
     * 回调
     */
    private boolean onPlayOrStopDeviceStatusCallback(int status) {
        String stateStr = "";
        if (status == APlatData.STATUS_CALLING) {
            stateStr = "STATUS_CALLING";
        } else if (status == APlatData.STATUS_CALL_SUCCESS) {
            stateStr = "STATUS_CALL_SUCCESS";
        } else if (status == APlatData.STATUS_CALL_END) {//
            stateStr = "STATUS_CALL_END";
        } else if (status == APlatData.STATUS_MONITORING) {
            stateStr = "STATUS_MONITORING";
        } else if (status == APlatData.STATUS_MONITOR_END) {
            stateStr = "STATUS_MONITOR_END";
        }
        APlatData.debugLog("DSParse.clazz onPlayOrStopDevice-->>> old mStatus is "
                + status + ";new mStatus is " + this.mStatus + " str:" + stateStr);
        this.mStatus = status;
        if (mCallback != null) {
            mCallback.onPlayOrStopDeviceStatusCallback(status);
        }
        return true;
    }

    //////////////////////////////////////回调/////////////////////////////////////////////////

    /**
     * 1.打开音视频数据请求
     */
    private int onPlayRequest() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 4 + 4) {
            APlatData.debugLog("DSParse.clazz--->>>onPlayRequest get channelId mediaMode short");
            return -1;
        }
        int channelId = mByteOutput.getInt();
        int mediaMode = mByteOutput.getInt();
        this.mMediaMode |= mediaMode;
        APlatData.debugLog("DSParse.clazz--->>>onPlayRequest channelId:" + channelId
                + ",mediaMode:" + mediaMode + " mStatus:" + mStatus);
        if ((mediaMode & APlatData.MEDIAMODE_AUDIO_PLAY) != 0) {
            onPlayOrStopDeviceStatusCallback(APlatData.STATUS_CALL_SUCCESS);
        } else if ((mediaMode & APlatData.MEDIAMODE_VIDEO) != 0) {
            if ((mStatus != APlatData.STATUS_CALLING)
                    && (mStatus != APlatData.STATUS_CALL_SUCCESS)) {
                onPlayOrStopDeviceStatusCallback(APlatData.STATUS_MONITORING);
            }
        }
        APlatData.debugLog("DSParse.clazz--->>>onPlayRequest mediaMode:" + mediaMode
                + ",mStatus:" + mStatus + ",this.mMediaMode:" + this.mMediaMode);
        if (mCallback != null) {
            mCallback.onPlayRequest(cmdFlag, channelId, mediaMode);
        }
        return 0;
    }

    /**
     * 2.关闭音视频数据请求
     */
    private int onStopRequest() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 4 + 4) {
            APlatData.debugLog("DSParse.clazz onStopRequest data length too short:"
                    + remainLen);
            return -1;
        }

        int channelId = mByteOutput.getInt();
        int mediaMode = mByteOutput.getInt();
        this.mMediaMode &= ~mediaMode;
        APlatData.debugLog("DSParse.clazz onStopRequest:channelId:" + channelId + ";mediaMode:"
                + mediaMode + ";mStatus:" + mStatus + ";this.mMediaMode:" + this.mMediaMode);

        if ((mediaMode & APlatData.MEDIAMODE_AUDIO_PLAY) != 0) {
            if (mStatus == APlatData.STATUS_CALL_SUCCESS) {
                onPlayOrStopDeviceStatusCallback(APlatData.STATUS_CALL_END);
            }
        } else if ((mediaMode & APlatData.MEDIAMODE_VIDEO) != 0) {
            if (mStatus == APlatData.STATUS_MONITORING) {
                onPlayOrStopDeviceStatusCallback(APlatData.STATUS_MONITOR_END);
            }
        }

        if (this.mMediaMode == 0) {
            onPlayOrStopDeviceStatusCallback(APlatData.STATUS_ORIGINAL);
        }
        if (mCallback != null) {
            mCallback.onStopRequest(cmdFlag, channelId, mediaMode);
        }
        return 0;
    }

    /**
     * 3.发送音视频流请求
     */
    private int onSendMediaRequest() {
        // APlatData.debugLog("onSendMediaRequest:" + mByteOutput.getRemainDataLength());
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 4 + 4 + 1 + 4) {
            APlatData.debugLog("DSParse.clazz onSendMediaRequest data length too short:"
                    + remainLen);
            return -1;
        }

        int channelId = mByteOutput.getInt();
        int sequence = mByteOutput.getInt();
        byte isKeyFrame = mByteOutput.getByte();
        int mediaLen = mByteOutput.getInt();
        if (mByteOutput.getRemainDataLength() < mediaLen) {
            APlatData.debugLog("DSParse.clazz onSendMediaRequest less length:"
                    + mediaLen);
            return -1;
        }
        if (mediaLen != APlatData.AUDIO_DECODE_LENGTH) {
            APlatData.debugLog("**********mediaLen is not 320");
            return -1;
        }

        byte[] mediaData = mByteOutput.getBytes(mediaLen);
        if (mCallback != null) {
            mCallback.onSendMediaRequest(cmdFlag, channelId, sequence, isKeyFrame, mediaData);
        }
        return 0;
    }

    /**
     * 4.设置音频音量请求
     */
    private int onSetVolumeRequest() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 4 + 4) {
            APlatData.debugLog("DSParse.clazz onSetVolumeRequest data length too short:"
                    + remainLen);
            return -1;
        }
        if (mCallback != null) {
            mCallback.onSetVolumeRequest();
        }
        return 0;
    }

    /**
     * 5.设置视频品质请求
     */
    private int onSetVideoModeRequest() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 4 + 1 + 1 + 2) {
            APlatData.debugLog("DSParse.clazz onSetVideoModeRequest data length too short:"
                    + remainLen);
            return -1;
        }
        if (mCallback != null) {
            mCallback.onSetVideoModeRequest();
        }
        return 0;
    }

    /**
     * 6.设置视频属性请求
     */
    private int onSetVideoAttrRequest() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 4 + 4 + 1) {
            APlatData.debugLog("DSParse.clazz onSetVideoAttrRequest data length too short:"
                    + remainLen);
            return -1;
        }
        if (mCallback != null) {
            mCallback.onSetVideoAttrRequest();
        }
        return 0;
    }

    /**
     * 7.开锁请求
     */
    private int onUnlockRequest() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 4 + APlatData.PHONE_NUMBER_LENGTH) {
            APlatData.debugLog("DSParse.clazz onUnlockRequest data length too short:"
                    + remainLen);
            return -1;
        }
        int unlockType = mByteOutput.getInt();
        String cardOrPhoneNumber = mByteOutput.getString(APlatData.PHONE_NUMBER_LENGTH);
        remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < APlatData.ROOM_NUMBER_LENGTH) {
            APlatData.debugLog("DSParse.clazz onUnlockRequest roomNumber length too short:"
                    + remainLen);
            return -1;
        }
        String roomNumber = mByteOutput.getString(APlatData.ROOM_NUMBER_LENGTH);
        APlatData.debugLog("DSParse.clazz onUnlockRequest:unlockType:" + unlockType
                + ";cardOrPhoneNumber:" + cardOrPhoneNumber + " ,roomNumber:" + roomNumber);
        if (mCallback != null) {
            mCallback.onUnlockRequest(unlockType, cardOrPhoneNumber, roomNumber);
        }
        return 0;
    }

    /**
     * 8.获取网口配置信息回应
     */
    private int onGetNetResult() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 1 + 1 + 4 + 4 + 4 + 1 + 4 + 4 + APlatData.MAC_ADDRESS_LENGTH) {
            APlatData.debugLog("DSParse.clazz onGetNetResult data length too short:"
                    + remainLen);
            return -1;
        }
        InfoNetParam netParam = new InfoNetParam();
        netParam.nettype = mByteOutput.getByte();
        netParam.ethaddrtype = mByteOutput.getByte();
        netParam.ethip = mByteOutput.getInt();
        netParam.ethmask = mByteOutput.getInt();
        netParam.defaultgw = mByteOutput.getInt();
        netParam.dnsaddrtype = mByteOutput.getByte();
        netParam.primarydns = mByteOutput.getInt();
        netParam.secondarydns = mByteOutput.getInt();
        byte[] mac = mByteOutput.getBytes(6);
        netParam.setMacAddress(mac);

        APlatData.debugLog("DSParse.clazz onGetNetResult netParam" + netParam
                + ";mCallback is " + mCallback);
        if (mCallback != null) {
            mCallback.onGetNetResult(cmdFlag, netParam);
        }
        return 0;
    }

    /**
     * 9.设置网口配置信息回应
     */
    private int onSetNetResult() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 4) {
            APlatData.debugLog("DSParse.clazz onSetNetResult data length too short:"
                    + remainLen);
            return -1;
        }
        int result = mByteOutput.getInt();
        if (mCallback != null) {
            mCallback.onSetNetResult(cmdFlag, result);
        }
        return 0;
    }

    /**
     * 10.删除刷卡卡号请求
     */
    private int onDelCardRequest() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < APlatData.CARD_NUMBER_LENGTH) {
            APlatData.debugLog("DSParse.clazz onDelCardRequest data length too short:"
                    + remainLen);
            return -1;
        }
        return 0;
//        暂时封闭平台发送删除本地卡号的功能
//        String cardNum = mByteOutput.getString(APlatData.CARD_NUMBER_LENGTH);
//        APlatData.debugLog("DSParse.clazz onDelCardRequest cardNum:"
//                + cardNum);
//        if (mCallback != null) {
//            mCallback.onDelCardRequest(cmdFlag, cardNum);
//        }
//        return 0;
    }

    /**
     * 11.推送消息透传请求
     */
    private int onTunnelPushRequest() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < APlatData.ROOM_NUMBER_LENGTH) {
            APlatData.debugLog("DSParse.clazz onTunnelPushRequest data length too short:"
                    + remainLen);
            return -1;
        }
        if (mCallback != null) {
            mCallback.onTunnelPushRequest();
        }
        return 0;
    }

    /**
     * 12.客户端信令透传请求
     */
    private int onTunnelCmdRequest() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < APlatData.PLATFORM_KEY_LENGTH) {
            APlatData.debugLog("DSParse.clazz onTunnelCmdRequest data length too short:"
                    + remainLen);
            return -1;
        }
        if (mCallback != null) {
            mCallback.onTunnelCmdRequest();
        }
        return 0;
    }

    /**
     * 13.呼叫房号通知回应
     */
    private int onQueryRoomResult() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 4 + APlatData.ROOM_NUMBER_LENGTH + 4) {
            APlatData.debugLog("DSParse.clazz onQueryRoomResult data length too short:"
                    + remainLen);
            return -1;
        }
        int result = mByteOutput.getInt();
        String roomNum = mByteOutput.getString(APlatData.ROOM_NUMBER_LENGTH);
        int timer = mByteOutput.getInt();
        APlatData.debugLog("DSParse.clazz onQueryRoomResult result:" + result + ",roomNum:"
                + roomNum + ",timer:" + timer);
        if (mCallback != null) {
            mCallback.onCallResult(cmdFlag, result, roomNum, timer);
        }
        return 0;
    }

    /**
     * 14.检查刷卡卡号回应
     */
    private int onCheckCardResult() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < (4 + APlatData.CARD_NUMBER_LENGTH)) {
            APlatData.debugLog("DSParse.clazz onCheckCardResult data length too short:"
                    + remainLen);
            return -1;
        }
        int result = mByteOutput.getInt();
        String cardNum = mByteOutput.getString(APlatData.CARD_NUMBER_LENGTH);
        APlatData.debugLog("DSParse.clazz onCheckCardResult result:" + result
                + ",cardNum:" + cardNum);

        if (mCallback != null) {
            mCallback.onCheckCardResult(cmdFlag, result, cardNum);
        }
        return 0;
    }

    /**
     * 15.检查开门密码回应
     */
    private int onCheckPasswordResult() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < (4 + APlatData.ROOM_NUMBER_LENGTH
                + APlatData.UNLOCK_PWD_LENGTH)) {
            APlatData.debugLog("DSParse.clazz onCheckPasswordResult data length too short:"
                    + remainLen);
            return -1;
        }
        int result = mByteOutput.getInt();
        String roomNum = mByteOutput.getString(APlatData.ROOM_NUMBER_LENGTH);
        String pwd = mByteOutput.getString(APlatData.UNLOCK_PWD_LENGTH);
        APlatData.debugLog("DSParse.clazz onCheckCardResult result:" + result
                + ";roomNum:" + roomNum + ";pwd:" + pwd);

        if (mCallback != null) {
            mCallback.onCheckPasswordResult(cmdFlag, result, roomNum, pwd);
        }
        return 0;
    }

    /**
     * 16.拨打电话请求
     */
    private int onDialRequest() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < APlatData.ROOM_NUMBER_LENGTH + APlatData.PHONE_NUMBER_LENGTH) {
            APlatData.debugLog("DSParse.clazz onDialRequest data length too short:"
                    + remainLen);
            return -1;
        }
        String roomNum = mByteOutput.getString(APlatData.ROOM_NUMBER_LENGTH);
        String phoneNum = mByteOutput.getString(APlatData.PHONE_NUMBER_LENGTH);
        APlatData.debugLog("DSParse.clazz onDialRequest roomNum:" + roomNum
                + ",phoneNum:" + phoneNum);
        if (mCallback != null) {
            mCallback.onPhoneCallRequest(roomNum, phoneNum);
        }
        return 0;
    }

    /**
     * 17.停止拨打电话请求
     */
    private int onStopPhoneCallRequest() {
        APlatData.debugLog("DSParse.clazz onStopPhoneCallRequest:"
                + mByteOutput.getRemainDataLength());
        if (mCallback != null) {
            mCallback.onStopPhoneCallRequest();
        }
        return 0;
    }

    /**
     * 18.设备余额不足停止拨打电话请求
     *
     * @return result
     */
    private int onDisablePhoneCallRequest() {
        APlatData.debugLog("DSParse.clazz onDisablePhoneCallRequest:"
                + mByteOutput.getRemainDataLength());
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 1) {
            APlatData.debugLog("DSParse.clazz onDisablePhoneCallRequest get reason short:"
                    + remainLen);
            return -1;
        }
        byte reason = mByteOutput.getByte();
        if (mCallback != null) {
            mCallback.onDisablePhoneCallRequest(reason);
        }
        return 0;
    }

    /**
     * 19.上传开门记录回应
     *
     * @return result
     */
    private int onUnlockHistoryTimeResult() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < (4 + 4)) {
            APlatData.debugLog("DSParse.clazz onUnlockStateResult get result count short:"
                    + remainLen);
            return -1;
        }
        int result = mByteOutput.getInt();
        int count = mByteOutput.getInt();

        APlatData.debugLog("DSParse.clazz onUnlockStateResult  result:"
                + result + ";count:" + count);
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < 4) {
                APlatData.debugLog("onUnlockStateResult card get getUnlockNameByType " +
                        "unLockIndex short:" + remainLen);
                break;
            }
            int unLockIndex = mByteOutput.getInt();
            APlatData.debugLog("DSParse.clazz onUnlockStateResult  " +
                    "unLockIndex:" + unLockIndex);
            list.add(unLockIndex);
        }
        if (mCallback != null) {
            mCallback.onUnlockStateResult(result, count, list);
        }
        return 0;
    }

    /**
     * 20.获取平台时间
     * 单位:秒
     *
     * @return state
     */
    private int onGetTimestampResult() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 4) {
            APlatData.debugLog("DSParse.clazz onGetTimestampResult data length too short:"
                    + remainLen);
            return -1;
        }
        int platformTime = mByteOutput.getInt();
        if (mCallback != null) {
            mCallback.onGetTimestampResult(platformTime);
        }
        return 0;
    }

    /**
     * 21.获取房间卡号信息
     *
     * @return state
     */
    private int onGetRoomCardInfoResult() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 4) {
            APlatData.debugLog("DSParse.clazz onGetRoomCardInfoResult get roomCount short:"
                    + remainLen);
            return -1;
        }
        int roomCount = mByteOutput.getInt();
        APlatData.debugLog("****************RoomIDSet.size:" + DeviceApplication.mRoomIDSet.size()
                + ",roomCount:" + roomCount);
        for (int i = 0; i < roomCount; i++) {
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < 4 + 4 + 4) {
                APlatData.debugLog("DSParse.clazz onGetRoomCardInfoResult get roomId" +
                        " cardIndex cardCount short:" + remainLen);
                return -1;
            }
            int roomId = mByteOutput.getInt();
            int cardIndex = mByteOutput.getInt();
            int cardCount = mByteOutput.getInt();

            RoomInfoBean roomInfoBean = new RoomInfoBean();
            roomInfoBean.setRoomId(roomId);
            roomInfoBean.setCardIndex(cardIndex);
            roomInfoBean.setCardCount(cardCount);

            boolean contains = DeviceApplication.mRoomIDSet.contains(roomId);
            //如果得到linux回应这个房间号的消息，那么将这个房间移除出集合
            if (contains) DeviceApplication.mRoomIDSet.remove(roomId);
            APlatData.debugLog("DSParse.clazz--->>>RoomIDSet size:"
                    + DeviceApplication.mRoomIDSet.size() + ",roomId:" + roomId + ",contains:"
                    + contains + ",cardIndex:" + cardIndex + ",cardCount:" + cardCount);

            //获取平台卡号信息
            List<RoomCardBean> tempPlatCards = new ArrayList<>();
            for (int j = 0; j < cardCount; j++) {
                remainLen = mByteOutput.getRemainDataLength();
                if (remainLen < 1 + 1) {
                    APlatData.debugLog("DSParse.clazz onGetRoomCardInfoResult get cardType"
                            + " cardNumLen short:" + remainLen);
                    break;
                }
                byte cardType = mByteOutput.getByte();
                byte cardNumLen = mByteOutput.getByte();
                remainLen = mByteOutput.getRemainDataLength();
                if (remainLen < cardNumLen) {
                    APlatData.debugLog("DSParse.clazz onGetRoomCardInfoResult get cardNum"
                            + " short:" + remainLen);
                    break;
                }
                String cardNum = mByteOutput.getString(cardNumLen);

                RoomCardBean bean = new RoomCardBean();
                bean.setCardNum(cardNum);
                bean.setRoomId(roomId);
                tempPlatCards.add(bean);
            }
            roomInfoBean.setPlatCards(tempPlatCards);
            //先检查是否之前存在状态，将这个房号信息实体类放入集合，等定时器去完成数据库操作
            boolean checkContains = DeviceApplication.mVerifyRoomList.contains(roomInfoBean);
            if (checkContains) DeviceApplication.mVerifyRoomList.remove(roomInfoBean);
            DeviceApplication.mVerifyRoomList.add(roomInfoBean);
            APlatData.debugLog("DSParse.clazz onGetRoomCardInfoResult VerifyRoomInfo size:"
                    + DeviceApplication.mVerifyRoomList.size() + ",checkContains:" + checkContains);
        }
        //8.如果集合还有数据,那么就向Linux请求需要数据信息
        int remainSize = DeviceApplication.mRoomIDSet.size();
        APlatData.debugLog("DSParse.clazz!!!!!!!!!!!!!!!!!!!!!!!!remainSize:"
                + remainSize + ",roomCount:" + roomCount);
        if (remainSize > 0) {
            DongDongCenter.getRoomCardInfo(0, DeviceApplication.mRoomIDSet);
        }
        return 0;
    }
    /**
     * 22.获取历史开门记录请求
     */
    private int onGetHistoryUnLockRecordRequest() {
        APlatData.debugLog("DSParse.clazz onGetHistoryUnLockRecordRequest:"
                + mByteOutput.getRemainDataLength());
        if (mCallback != null) {
            mCallback.onGetHistoryUnLockRecordRequest();
        }
        return 0;
    }

    /**
     * 23.上传实时开门记录回应
     */

    private int onUnLockTimeDataResult() {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < (4 + 4)) {
            APlatData.debugLog("DSParse.clazz onUnLockTimeDataResult get  result UnlockIndex short:"
                    + remainLen);
            return -1;
        }
        int result = mByteOutput.getInt();
        int unLockIndex = mByteOutput.getInt();
        List<Integer> unLockIndexList = new ArrayList<>();
        unLockIndexList.add(unLockIndex);
        APlatData.debugLog("DSParse.clazz onUnLockTimeDataResult cmdFlag:" + cmdFlag
                + ";result:" + result + ";unLockIndex:" + unLockIndex);
        if (mCallback != null) {
            mCallback.onUnlockStateResult(result, 1, unLockIndexList);
        }
        return 0;
    }
}
