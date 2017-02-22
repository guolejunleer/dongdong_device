package com.dongdong.socket.beat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import com.dongdong.AppConfig;
import com.dongdong.DeviceApplication;
import com.dongdong.base.BaseApplication;
import com.dongdong.db.RoomCardOpe;
import com.dongdong.db.RoomIndexOpe;
import com.dongdong.db.entry.RoomCardBean;
import com.dongdong.db.entry.RoomIndexBean;
import com.dongdong.sdk.DongDongCenter;
import com.dongdong.utils.DDLog;
import com.dongdong.utils.SPUtils;

/**
 * 数据包解析类
 */
public class Parser {
    private ByteOutput mByteOutput;
    private Packet mPacket;
    private DatagramSocket mUnisocket;
    private InetAddress mSendIntAddr;

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

    public Parser() {
        mByteOutput = new ByteOutput();
        mPacket = new Packet();
        try {
            mUnisocket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析协议头
     *
     * @return 结果
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

        if (groupCode != (byte) 0xdc) {
            ALinuxData.debugLog("Parser.clazz parseHeader wrong :" + groupCode);
            return false;
        }
        return true;
    }

    /**
     * 解析收到DeviceService的数据包
     *
     * @param dp 数据包
     * @throws IOException
     */
    public void parseReceiveData(DatagramPacket dp) throws IOException {
        mSendIntAddr = dp.getAddress();
        int port = dp.getPort();
        int dataLen = dp.getLength();
        ALinuxData.debugLog("//////////////////Parser.clazz parseReceiveData from:"
                + mSendIntAddr + "; port:" + port + "; dataLen:" + dataLen + " /////////////// ");
        if (dataLen < ALinuxData.PACKET_HEADER_LENGTH) {
            ALinuxData.debugLog("Parser.clazz parseReceiveData bytes to short!!!");
            return;
        }

        mByteOutput.setBytes(dp.getData(), dataLen);
        if (!parseHeader()) {
            return;
        }
        ALinuxData.debugLog("Parser.clazz parseReceiveData cmdId:" + cmdId);
        switch (cmdId) {
            case ALinuxData.CMD_SCAN_REQUEST:
                onScanRequest();
                break;
//            case ALinuxData.CMD_SCAN_RESPONSE:
//                onScanRequest();
//                break;
            case ALinuxData.CMD_KEEPALIVE_REQUEST:
                onKeepAliveRequest();
                break;
            case ALinuxData.CMD_GETPARAM_RESPONSE:
                onGetParamResponse();
                break;
            default:
                ALinuxData.debugLog("Parser.clazz no match cmdId:"
                        + cmdId);
                break;
        }
    }

    /**
     * 关闭Socket
     */
    public void closeSocket() {
        if (mUnisocket != null) {
            mUnisocket.close();
            mUnisocket = null;
        }
    }

    /**
     * 发送单播
     *
     * @throws IOException
     */
    private void sendResponse() throws IOException {
        byte[] bytes = mPacket.scanResponse(cmdFlag);
        DatagramPacket dp = new DatagramPacket(bytes, bytes.length, mSendIntAddr,
                ALinuxData.UNICAST_PORT);
        mUnisocket.send(dp);
        ALinuxData.debugLog("Parser.clazz sendResponse to " + mSendIntAddr
                + ":" + ALinuxData.UNICAST_PORT + ",dataLen:" + bytes.length
                + ",cmdFlag:" + cmdFlag);
    }

    /**
     * 发送单播
     *
     * @param reserved1 头协议的保留字段
     * @throws IOException
     */
    private void sendResponse(short reserved1) throws IOException {
        byte[] bytes = mPacket.scanResponse(cmdFlag, reserved1);
        DatagramPacket dp = new DatagramPacket(bytes, bytes.length, mSendIntAddr,
                ALinuxData.UNICAST_PORT);
        mUnisocket.send(dp);
        ALinuxData.debugLog("Parser.clazz sendResponse to " + mSendIntAddr
                + ":" + ALinuxData.UNICAST_PORT + ",dataLen:" + bytes.length + ",cmdFlag:" + cmdFlag);
    }

    public static byte mPullRoomIdFlag = 1;
    private static boolean mIsStartGetRoomInfo = false;

    /**
     * 心跳包回应
     *
     * @return state
     * @throws IOException
     */
    private int onScanRequest() throws IOException {
        int roomCountPre = DeviceApplication.mRoomIDSet.size();
        int regState;
        String linuxVersion;
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 4) {
            ALinuxData.debugLog("Parser.clazz get camera data too short");
            return -1;
        }
        int cameraId = mByteOutput.getInt();
        if (cameraId != ALinuxData.mCameraId) {
            ALinuxData.debugLog("Parser.clazz not same camera:" + cameraId);
            return -1;
        }
        remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < (4 + ALinuxData.VERSION_LENGTH)) {
            ALinuxData.debugLog("Parser.clazz get register and linuxVersion to short!!!");
            return -1;
        }
        regState = mByteOutput.getInt();
        linuxVersion = mByteOutput.getString(ALinuxData.VERSION_LENGTH);
        // 回调注册状态值regState和版本号linuxVersion
        if (ALinuxData.mSink != null) {
            ALinuxData.mSink.onPeerAddress(mSendIntAddr.toString().replaceAll("/", ""));
            ALinuxData.mSink.onDrive(regState, linuxVersion);
        }
        //同步房间卡号
        if (mPullRoomIdFlag == 1) {
            //1.应用起来第一次向Linux要数据
            mPullRoomIdFlag = 2;
            DeviceApplication.mRoomIDSet.clear();
            ALinuxData.debugLog("Parser.clazz mPullRoomIdFlag==1 we get data first!!!");
            //回复要完整数据心跳包
            sendResponse((short) 1);
            return 0;
        } else if (mPullRoomIdFlag == 2) {
            //2.如果count为0,mPullRoomIdFlag=3，和本地数据比对
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < 4) {
                ALinuxData.debugLog("Parser.clazz get roomCount to short!!!");
                return -1;
            }
            int roomCount = mByteOutput.getInt();
            ALinuxData.debugLog("Parser.clazz mPullRoomIdFlag==2,roomCount:"
                    + roomCount + ",reserved1:" + reserved1);
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < (4 + 4) * roomCount) {
                ALinuxData.debugLog("Parser.clazz get roomInfo to short!!!");
                return -1;
            }
            if (reserved1 == 1) {
                mIsStartGetRoomInfo = true;
                for (int i = 0; i < roomCount; i++) {
                    int roomID = mByteOutput.getInt();
                    int cardIndex = mByteOutput.getInt();
                    if (cardIndex != 0) {///cardIndex为0的话不进入集合，校验数据的时候会本地清除
                        RoomIndexBean localRIBeans = RoomIndexOpe.queryDataByRoomId(
                                BaseApplication.context(), roomID);
                        ALinuxData.debugLog("Parser.clazz mPullRoomIdFlag==2,reserved1 == 1 cardIndex:"
                                + cardIndex + ",roomID:" + roomID + ",localRIBeans:" + localRIBeans);
//                        if ((localRIBeans == null || localRIBeans.getCardIndex() != cardIndex)) {
                        DeviceApplication.mRoomIDSet.add(roomID);
//                         }这里不用再判断本地是否有数据和cardIndex的值，加了这句会导致第二次启动应用会删除所有的的数据!!!
                    }
                }
                sendResponse((short) 0);
            } else {
                if (!mIsStartGetRoomInfo) {
                    ALinuxData.debugLog("Parser.clazz mPullRoomIdFlag==2," +
                            " try get all data again!!! reserved1:" + reserved1);
                    //回复要完整心跳包
                    sendResponse((short) 1);
                } else {
                    //校验:如果集合有数据,那么就和本地数据对比,清除旧卡
                    boolean isEmpty = DeviceApplication.mRoomIDSet.isEmpty();
                    ALinuxData.debugLog("Parser.clazz mPullRoomIdFlag" +
                            "==2  yel !!!!!!!!!!!!!!!!!!!!!!!!!!! we had all linux" +
                            " data ,and data size is " + DeviceApplication.mRoomIDSet.size());
                    if (!isEmpty) {
                        List<RoomIndexBean> localRIBeans = RoomIndexOpe.queryAll(
                                BaseApplication.context());
                        ALinuxData.debugLog("Parser.clazz mPullRoomIdFlag" +
                                "==2  yel !!!!!!!!!!!!!!!!!!!!!!!!!!! we had all linux" +
                                " data ,and local data size is " + localRIBeans);
                        for (RoomIndexBean been : localRIBeans) {//循环本地Room_Index表数据
                            int roomId = been.getRoomId();
                            boolean contains = DeviceApplication.mRoomIDSet.contains(roomId);
                            // DeviceApplication.mRoomIDSet
                            if (!contains) {//本地数据没有平台房间id,那么删除本地旧记录
                                RoomIndexOpe.deleteDataByCardRoomId(BaseApplication.context(), roomId);
                                List<RoomCardBean> localRCBeans = RoomCardOpe.queryDataListByRoomId(
                                        BaseApplication.context(), roomId);
                                if (localRCBeans != null && !localRCBeans.isEmpty()) {
                                    RoomCardOpe.deleteDataByRoomId(BaseApplication.context(),
                                            roomId);
                                }
                            }
                        }
                        ALinuxData.debugLog("Parser.clazz mPullRoomIdFlag==2 start" +
                                " get room card !!! local data size:" + localRIBeans.size());
                        //本地数据对比完成,接着向Linux请求数据
                        DongDongCenter.getRoomCardInfo(cmdFlag, DeviceApplication.mRoomIDSet);
                    } else {
                        //平台将数据删除，第一次同步,如果Linux没有数据那么本地数据也要删除
                        List<RoomIndexBean> localRIBeans = RoomIndexOpe.queryAll(BaseApplication.context());
                        List<RoomCardBean> localRCBeans = RoomCardOpe.queryAll(BaseApplication.context());
                        if (!localRIBeans.isEmpty()) {
                            RoomIndexOpe.deleteAllData(BaseApplication.context());
                        }
                        if (!localRCBeans.isEmpty()) {
                            RoomCardOpe.deleteAllData(BaseApplication.context());
                        }
                    }
                    //回归到正常情况
                    mPullRoomIdFlag = 3;
                    sendResponse((short) 0);
                }
            }
        } else if (mPullRoomIdFlag == 3) {
            //3.正常方式
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < 4) {
                ALinuxData.debugLog("Parser.clazz get roomCount to short!!!");
                return -1;
            }
            int roomCount = mByteOutput.getInt();
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < (4 + 4) * roomCount) {
                ALinuxData.debugLog("Parser.clazz get roomInfo to short!!!");
                return -1;
            }
            ALinuxData.debugLog("Parser.clazz mPullRoomIdFlag==3 roomCount:"
                    + roomCount + ",reserved1:" + reserved1);
            if (roomCount > 0) {
                for (int i = 0; i < roomCount; i++) {
                    int roomID = mByteOutput.getInt();
                    int cardIndex = mByteOutput.getInt();
                    RoomIndexBean roomIndexBean = RoomIndexOpe.queryDataByRoomId(
                            BaseApplication.context(), roomID);
                    //如果数据库没有房号信息,或者房号信息有更新,那么就将这条信息储存到集合
                    if ((roomIndexBean == null && cardIndex != 0) //防止房间无卡号的情况
                            || (roomIndexBean != null && roomIndexBean.getCardIndex() != cardIndex)) {
                        DeviceApplication.mRoomIDSet.add(roomID);
                    }
                }
            }
            //如果集合有数据,那么就向Linux请求需要数据信息
            int roomCountCur = DeviceApplication.mRoomIDSet.size();
            if (roomCountCur > 0 && roomCountPre == 0) {
                DongDongCenter.getRoomCardInfo(cmdFlag, DeviceApplication.mRoomIDSet);
            }
            ALinuxData.debugLog("Parser.clazz-->>>mPullRoomIdFlag==3 roomCount:" + roomCount
                    + ",RoomIDSet:" + DeviceApplication.mRoomIDSet + ",cmdFlag:" + cmdFlag);
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < 4) {
                DDLog.i("Parser.clazz get deviceId to short!!!");
                return -1;
            }
            //3.获取设备数据库Id
            int deviceId = mByteOutput.getInt();
            DDLog.i("GT", "Parser.clazz-->>>mPullRoomIdFlag==3 deviceId:" + deviceId);

            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < 4) {
                DDLog.e("Parser.clazz get bulletinIndex to short!!!");
                return -1;
            }
            //4.获取物业公告Index
            int bulletinIndex = mByteOutput.getInt();
            if (!((Integer) SPUtils.getParam(BaseApplication.context(), SPUtils.DD_CONFIG_SHARE_PREF_NAME,
                    SPUtils.SP_KEY_BULLETIN_INDEX, 0) == bulletinIndex)) {
                //4.1检测到Index有变化，那么向物业平台请求最新物业公告
                DongDongCenter.getBulletinFromNet(deviceId);
                //4.2将最新Index保存到本地
                SPUtils.setParam(BaseApplication.context(), SPUtils.DD_CONFIG_SHARE_PREF_NAME,
                        SPUtils.SP_KEY_BULLETIN_INDEX, bulletinIndex);
            }
            DDLog.i("Parser.clazz-->>>mPullRoomIdFlag==3 bulletinIndex:" + bulletinIndex);
            //回复心跳包
            sendResponse((short) 0);
        }
        return 0;
    }

    /**
     * 这个方法已经用不上
     *
     * @return state
     * @throws IOException
     */
    private int onKeepAliveRequest() throws IOException {
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < 4) {
            ALinuxData.debugLog("Parser.clazz data length too short");
            return -1;
        }
        int cameraId = mByteOutput.getInt();
        ALinuxData.debugLog("Parser.clazz onKeepAliveRequest mCameraId:"
                + cameraId);
        if (cameraId != ALinuxData.mCameraId) {
            ALinuxData.debugLog("Parser.clazz not same mCameraId");
            return 0;
        }
        sendResponse();
        return 0;
    }

    /**
     * 获取拨打电话参数
     *
     * @return state
     * @throws IOException
     */
    private int onGetParamResponse() throws IOException {
        ALinuxData.debugLog("Parser.clazz onGetParamResponse:"
                + mByteOutput.getRemainDataLength());
        int remainLen = mByteOutput.getRemainDataLength();
        if (remainLen < (4 + 4 + 1)) {
            ALinuxData.debugLog("Parser.clazz get cameraId type too short");
            return -1;
        }

        int cameraId = mByteOutput.getInt();
        int type = mByteOutput.getInt();
        if (cameraId != ALinuxData.mCameraId) {
            ALinuxData.debugLog("Parser.clazz not same Camera:" + cameraId);
            return 0;
        }

        if (type == AppConfig.DAIL_TYPE_YUNZHIXUN) {// 第一家拨打电话厂商
            byte userNameLen = mByteOutput.getByte();
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < (userNameLen + 1)) {
                ALinuxData.debugLog("Parser.clazz data too short userNameLen:"
                        + userNameLen);
                return -1;
            }
            String username = mByteOutput.getString(userNameLen);
            byte pwdLen = mByteOutput.getByte();
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < (pwdLen + 1)) {
                ALinuxData.debugLog("Parser.clazz data too short pwdLen:"
                        + pwdLen);
                return -1;
            }
            String password = mByteOutput.getString(pwdLen);
            byte appIdLen = mByteOutput.getByte();
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < appIdLen) {
                ALinuxData.debugLog("Parser.clazz data too short  appIdLen:"
                        + appIdLen);
                return -1;
            }
            String appId = mByteOutput.getString(appIdLen);
            ALinuxData.debugLog("Parser.clazz yzx-->>:username:" + username
                    + " password:" + password + " appId:" + appId);
            if (ALinuxData.mSink != null) {
                if ((type & 1) == 1) {
                    ALinuxData.mSink.onUcpaasInfo(username, password, appId);
                }
            }
        } else if (type == AppConfig.DAIL_TYPE_YUNTONGXUN) {// 第二家拨打电话厂商
            byte userIdLen = mByteOutput.getByte();
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < (userIdLen + 1)) {
                ALinuxData.debugLog("Parser.clazz data too short userIdLen:"
                        + userIdLen);
                return -1;
            }
            String userId = mByteOutput.getString(userIdLen);
            byte accountSidLen = mByteOutput.getByte();
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < (accountSidLen)) {
                ALinuxData.debugLog("Parser.clazz data too short accountSidLen:"
                        + accountSidLen);
                return -1;
            }
            String accountSid = mByteOutput.getString(accountSidLen);
            byte authTokenLen = mByteOutput.getByte();
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < authTokenLen) {
                ALinuxData.debugLog("Parser.clazz data too short authTokenLen:"
                        + authTokenLen);
                return -1;
            }
            String authToken = mByteOutput.getString(authTokenLen);
            byte appKeyLen = mByteOutput.getByte();
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < appKeyLen) {
                ALinuxData.debugLog("Parser.clazz data too short appKeyLen:"
                        + appKeyLen);
                return -1;
            }
            String appKey = mByteOutput.getString(appKeyLen);
            byte vendPhoneLen = mByteOutput.getByte();
            remainLen = mByteOutput.getRemainDataLength();
            if (remainLen < vendPhoneLen) {
                ALinuxData.debugLog("Parser.clazz data too short vendPhoneLen:"
                        + vendPhoneLen);
                return -1;
            }
            String vendPhone = mByteOutput.getString(vendPhoneLen);
            ALinuxData.debugLog("Parser.clazz  ytx-->>:userId:" + userId
                    + ";accountSid:" + accountSid + ";authToken:" + authToken
                    + ";appKey:" + appKey + ";vendPhone:" + vendPhone + ",mSink:" + ALinuxData.mSink);
            if (ALinuxData.mSink != null) {
                ALinuxData.mSink.onGetYunTongXunInfo(userId, accountSid,
                        authToken, appKey, vendPhone);
            }
        }
        return 0;
    }
}
