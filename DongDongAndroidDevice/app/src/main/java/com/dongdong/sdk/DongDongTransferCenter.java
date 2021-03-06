package com.dongdong.sdk;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.dongdong.AppConfig;
import com.dongdong.base.BaseApplication;
import com.dongdong.db.LocalCardOpe;
import com.dongdong.db.UnlockLogOpe;
import com.dongdong.db.entry.CardBean;
import com.dongdong.db.entry.UnlockLogBean;
import com.dongdong.interf.LauncherCallback;
import com.dongdong.media.DDAudioPlay;
import com.dongdong.media.DDAudioRecorder;
import com.dongdong.socket.normal.APlatData;
import com.dongdong.socket.normal.DeviceServiceCallback;
import com.dongdong.socket.normal.InfoNetParam;
import com.dongdong.utils.DDLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Sock通信的信息事件转发者，同事也处理一些开门记录事情
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class DongDongTransferCenter implements DeviceServiceCallback {

    private LauncherCallback mLauncherCallback;
    private DDAudioRecorder mAudioRecorder = DDAudioRecorder.getInstance();
    private DDAudioPlay mDDAudioPlay = new DDAudioPlay();

    private static final int PLAY_REQUEST_WHAT = 0;
    private static final int STOP_REQUEST_WHAT = 1;
    private static final int UNLOCK_REQUEST_WHAT = 2;
    private static final int GET_NET_RESULT_WHAT = 3;
    private static final int SET_NET_RESULT_WHAT = 4;
    private static final int QUERY_ROOM_OR_MONITOR_STATUS_WHAT = 5;
    private static final int CALL_RESULT_WHAT = 6;
    private static final int CHECK_CARD_RESULT_WHAT = 7;
    private static final int CHECK_PASSWORD_RESULT_WHAT = 8;
    private static final int PHONE_CALL_REQUEST_WHAT = 9;
    private static final int STOP_PHONE_CALL_REQUEST_WHAT = 10;
    private static final int DISABLE_PHONE_CALL_REQUEST_WHAT = 11;
    private static final int GET_TIMESTAMP_RESULT_WHAT = 12;
    private static final int LOCAL_UNLOCK_RECORD_REQUEST_WHAT = 13;


    private static final String RESULT = "result";
    private static final String CMD_FLAG = "cmd_flag";
    private static final String CHANNEL_ID = "channel_id";
    private static final String MEDIA_MODE = "media_mode";
    private static final String CARD_NUMBER = "card_number";
    private static final String ROOM_NUMBER = "room_number";
    private static final String PHONE_NUMBER = "phone_number";
    private static final String UNLOCK_TYPE = "unlock_type";
    private static final String CARD_PHONE_NUMBER = "card_phone_number";

    public interface GsmCoderCallback {
        void audioRecord(byte[] coderData);
    }

    public DongDongTransferCenter(LauncherCallback launcherCallback) {
        this.mLauncherCallback = launcherCallback;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case PLAY_REQUEST_WHAT:
                    Bundle dataPlay = msg.getData();
                    // 播放请求回调
                    if (dataPlay.getInt(MEDIA_MODE) == 1) {//1.视频源核心板取
                        // videoRecorder.startVideo();
                    }
                    if (dataPlay.getInt(MEDIA_MODE) == 2) {//2.音频Android板取
                        mAudioRecorder.startSendAudio();
                        mDDAudioPlay.openSound();
                    }
                    break;
                case STOP_REQUEST_WHAT:
                    Bundle dataStopPlay = msg.getData();
                    // 停止播放请求回调
                    if (dataStopPlay.getInt(MEDIA_MODE) == 1) {
                        // videoRecorder.stopSurface();
                        // videoRecorder.stopVideo();
                    }
                    if (dataStopPlay.getInt(MEDIA_MODE) == 2) {
                        mAudioRecorder.closeSendAudio();
                        mDDAudioPlay.closeSound();
                    }
                    break;
                case UNLOCK_REQUEST_WHAT:
                    // 手机开锁请求回调
                    int unlockType = msg.getData().getInt(UNLOCK_TYPE);
                    String cardOrPhoneNum = msg.getData().getString(CARD_PHONE_NUMBER);
                    String roomNumber = msg.getData().getString(ROOM_NUMBER);
                    mLauncherCallback.onUnlockRequest(unlockType, cardOrPhoneNum, roomNumber);
                    break;
                case GET_NET_RESULT_WHAT:
                    // 请求核心板网络信息回调
                    InfoNetParam netParam = (InfoNetParam) msg.obj;
                    mLauncherCallback.onGetWifiParams(netParam);
                    break;
                case SET_NET_RESULT_WHAT:
                    // 设置核心板网络信息成功或者失败回调
                    int wifiResult = msg.arg1;
                    mLauncherCallback.onSetWifiParams(wifiResult);
                    break;
                case QUERY_ROOM_OR_MONITOR_STATUS_WHAT:
                    //呼叫房号或者监视时设备状态
                    int status = msg.arg1;
                    mLauncherCallback.onPlayOrStopDevice(status);
                    break;
                case CALL_RESULT_WHAT:
                    //拨打电话成功后的回调
                    int resultCall = msg.arg1;
                    String roomNum = (String) msg.obj;
                    int timer = msg.arg2;
                    mLauncherCallback.onQueryRoomResult(resultCall, roomNum, timer);
                    break;
                case CHECK_CARD_RESULT_WHAT:
                    //检查卡号的回调
                    int result = msg.getData().getInt(RESULT);
                    String cardNum = msg.getData().getString(CARD_NUMBER);
                    mLauncherCallback.onCheckCardResult(result, cardNum);
                    break;
                case CHECK_PASSWORD_RESULT_WHAT:
                    //检查密码的回调
                    int resultCheckPwd = msg.arg1;
                    mLauncherCallback.onPwdUnlock(resultCheckPwd);
                    break;
                case PHONE_CALL_REQUEST_WHAT:
                    //请求拨打电话的回调
                    Bundle dataPhoneCallReq = msg.getData();
                    String room = dataPhoneCallReq.getString(ROOM_NUMBER);
                    String phone = dataPhoneCallReq.getString(PHONE_NUMBER);
                    mLauncherCallback.onDialRequest(room, phone);
                    break;
                case STOP_PHONE_CALL_REQUEST_WHAT:
                    //停止拨打电话请求的回调
                    mLauncherCallback.onStopPhoneCallRequest();
                    break;
                case DISABLE_PHONE_CALL_REQUEST_WHAT:
                    //设备余额不足的回调
                    int reason = msg.arg1;
                    mLauncherCallback.onDisablePhoneCallRequest(reason);
                    break;
                case GET_TIMESTAMP_RESULT_WHAT:
                    //获取平台时间
                    //String platformTime = (String) msg.obj;
                    int platformTime = msg.arg1;
                    mLauncherCallback.onGetTimestampResult(platformTime);
                    break;
                case LOCAL_UNLOCK_RECORD_REQUEST_WHAT:
                    mLauncherCallback.onGetHistoryUnLockRecordRequest();
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    public int onPlayRequest(int cmdFlag, int channelId, int mediaMode) {
        DDLog.i("DongDongTransferCenter.class onPlayRequest cmdFlag: "
                + cmdFlag + "; mediaMode:" + mediaMode);// 1视频，2取音频，4，播音频
        Message msg = Message.obtain(mHandler, PLAY_REQUEST_WHAT);
        Bundle bundle = new Bundle();
        bundle.putInt(CMD_FLAG, cmdFlag);
        bundle.putInt(CHANNEL_ID, channelId);
        bundle.putInt(MEDIA_MODE, mediaMode);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        return 0;
    }

    @Override
    public int onStopRequest(int cmdFlag, int channelId, int mediaMode) {
        DDLog.i("DongDongTransferCenter.class onStopRequest  cmdFlag: " + cmdFlag
                + "; mediaMode:" + mediaMode);
        Message msg = Message.obtain(mHandler, STOP_REQUEST_WHAT);
        Bundle bundle = new Bundle();
        bundle.putInt(CMD_FLAG, cmdFlag);
        bundle.putInt(CHANNEL_ID, channelId);
        bundle.putInt(MEDIA_MODE, mediaMode);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        return 0;
    }

    @Override
    public int onSendMediaRequest(int cmdFlag, int channelId, int sequence,
                                  byte isKeyFrame, byte[] mediaData) {
        //这是向核心板发送音频，所以在这子线程中进行
        mDDAudioPlay.OnAudioData(cmdFlag, sequence, mediaData);
        return 0;
    }

    @Override
    public int onSetVolumeRequest() {
        return 0;
    }

    @Override
    public int onSetVideoModeRequest() {
        return 0;
    }

    @Override
    public int onSetVideoAttrRequest() {
        return 0;
    }

    @Override
    public int onUnlockRequest(int unlockType, String cardOrPhoneNum, String roomNum) {
        DDLog.i("DongDongTransferCenter.class onUnlockRequest--->>>> unlockType:"
                + unlockType + ";cardOrPhoneNum:" + cardOrPhoneNum);
        Message msg = Message.obtain(mHandler, UNLOCK_REQUEST_WHAT);
        msg.arg1 = unlockType;
        msg.obj = cardOrPhoneNum;
        Bundle bundle = new Bundle();
        bundle.putInt(UNLOCK_TYPE, unlockType);
        bundle.putString(CARD_PHONE_NUMBER, cardOrPhoneNum);
        bundle.putString(ROOM_NUMBER, roomNum);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        return 0;
    }

    @Override
    public int onGetNetResult(int cmdFlag, InfoNetParam netParam) {
        Message msg = Message.obtain(mHandler, GET_NET_RESULT_WHAT);
        msg.obj = netParam;
        mHandler.sendMessage(msg);
        return 0;
    }

    @Override
    public int onSetNetResult(int cmdFlag, int result) {
        Message msg = Message.obtain(mHandler, SET_NET_RESULT_WHAT);
        msg.arg1 = result;
        mHandler.sendMessage(msg);
        return 0;
    }

    @Override
    public int onDelCardRequest(int cmdFlag, String cardNum) {
        //这是向核心板请求删除卡号，所以在这子线程中进行
        cardNum = cardNum.toLowerCase();
        CardBean cardBean = LocalCardOpe.queryDataByCardNum(BaseApplication.context(), cardNum);
        if (cardBean != null) {
            LocalCardOpe.deleteDataByCardNum(BaseApplication.context(), cardNum);
        }
        return 0;
    }

    @Override
    public int onTunnelPushRequest() {
        return 0;
    }

    @Override
    public int onTunnelCmdRequest() {
        return 0;
    }

    @Override
    public int onPlayOrStopDeviceStatusCallback(int status) {
        Message msg = Message.obtain(mHandler, QUERY_ROOM_OR_MONITOR_STATUS_WHAT);
        msg.arg1 = status;
        mHandler.sendMessage(msg);
        return 0;
    }

    @Override
    public int onCallResult(int cmdFlag, int result, String roomNum,
                            int timer) {
        Message msg = Message.obtain(mHandler, CALL_RESULT_WHAT);
        msg.arg1 = result;
        msg.obj = roomNum;
        msg.arg2 = timer;
        mHandler.sendMessage(msg);
        return 0;
    }

    @Override
    public int onCheckCardResult(int cmdFlag, int result, String cardNum) {
        Message msg = Message.obtain(mHandler, CHECK_CARD_RESULT_WHAT);
        Bundle bundle = new Bundle();
        bundle.putInt(RESULT, result);
        bundle.putString(CARD_NUMBER, cardNum);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        return 0;
    }

    @Override
    public int onCheckPasswordResult(int cmdFlag, int result, String roomNum, String pwd) {
        Message msg = Message.obtain(mHandler, CHECK_PASSWORD_RESULT_WHAT);
        msg.arg1 = result;
        mHandler.sendMessage(msg);
        return 0;
    }

    @Override
    public int onPhoneCallRequest(String roomNum, String phoneNum) {
        Message msg = Message.obtain(mHandler, PHONE_CALL_REQUEST_WHAT);
        Bundle bundle = new Bundle();
        bundle.putString(ROOM_NUMBER, roomNum);
        bundle.putString(PHONE_NUMBER, phoneNum);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        return 0;
    }

    @Override
    public int onStopPhoneCallRequest() {
        Message msg = Message.obtain(mHandler, STOP_PHONE_CALL_REQUEST_WHAT);
        mHandler.sendMessage(msg);
        return 0;
    }

    @Override
    public int onDisablePhoneCallRequest(int reason) {
        Message msg = Message.obtain(mHandler, DISABLE_PHONE_CALL_REQUEST_WHAT);
        msg.arg1 = reason;
        mHandler.sendMessage(msg);
        return 0;
    }

    @Override
    public int onUnlockStateResult(int result, int unlockCount, List<Integer> unlockIndex) {
        if (result == APlatData.RESULT_SUCCESS) {
            DDLog.i("DongDongTransferCenter.clazz--->>onUnlockStateResult unlockCount: "
                    + unlockCount + ",unlockIndex:" + unlockIndex);
            List<UnlockLogBean> unlockLogBeanList = new ArrayList<>();
            for (int i = 0; i < unlockIndex.size(); i++) {
                UnlockLogBean unlockLogBean = UnlockLogOpe.
                        queryDataByUnLockId(BaseApplication.context(), unlockIndex.get(i));
                if (unlockLogBean == null) {
                    break;
                }
                UnlockLogBean bean = new UnlockLogBean();
                bean.setId((long) unlockIndex.get(i));
                bean.setUnlockType(unlockLogBean.getUnlockType());
                bean.setUnlockTime(unlockLogBean.getUnlockTime());
                bean.setCardOrPhoneNum(unlockLogBean.getCardOrPhoneNum());
                bean.setUpload(AppConfig.UNLOCK_RECORD_UPLOAD);
                bean.setRoomNum(unlockLogBean.getRoomNum());
                unlockLogBeanList.add(bean);
            }
            UnlockLogOpe.updateDataByUnLockList(BaseApplication.context(), unlockLogBeanList);
        }

        return 0;
    }

    @Override
    public int onGetTimestampResult(int platformTime) {
        Message msg = Message.obtain(mHandler, GET_TIMESTAMP_RESULT_WHAT);
        msg.arg1 = platformTime;
        mHandler.sendMessage(msg);
        return 0;
    }
    @Override
    public int onGetHistoryUnLockRecordRequest() {
        Message msg = Message.obtain(mHandler, LOCAL_UNLOCK_RECORD_REQUEST_WHAT);
        mHandler.sendMessage(msg);
        return 0;
    }

}
