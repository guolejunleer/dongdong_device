package com.dongdong.interf;

import com.dongdong.socket.normal.InfoNetParam;

/**
 * Launcher界面接收各种事件信息后的回调接口
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public interface LauncherCallback {

    int onPlayOrStopDevice(int status);

    int onUnlockRequest(int unlockType, String cardOrPhoneNum, String roomNum);

    void onCheckCardResult(int result, String cardNum);

    void onPwdUnlock(int result);

    void onQueryRoomResult(int result, String roomNub, int timer);

    int onDialRequest(String roomNub, String phoneNum);

    int onStopPhoneCallRequest();

    int onDisablePhoneCallRequest(int reason);

    void onGetWifiParams(InfoNetParam netParam);

    void onSetWifiParams(int result);

    int onGetTimestampResult(int platformTime);

    int onGetHistoryUnLockRecordRequest();
}
