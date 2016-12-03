package com.dongdong.interf;

/**
 * 应用系统每秒都会执行的回调接口
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public interface TimerCallback {
    void onTime(String time, boolean flag);

    void onNoAnswered();

    void resetKeyboardEventStatus();

    void phoneCallWaitTimeOut();

    void countTalkingOrMonitoringTime(boolean isTalkingOrMonitoring, int phoneTimeing, int textFlag);

    void reportALConnectedState(boolean isOut);

    void unLockTip();

    void onADPagerChanged();
}
