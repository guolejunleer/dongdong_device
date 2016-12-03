package com.dongdong.prompt;

import android.os.Handler;

import com.dongdong.AppConfig;
import com.dongdong.DeviceApplication;
import com.dongdong.base.BaseApplication;
import com.dongdong.interf.TimerCallback;
import com.dongdong.manager.RoomInfoOpeManager;
import com.dongdong.sdk.DongDongCenter;
import com.dongdong.socket.beat.Parser;
import com.dongdong.utils.DDLog;

/**
 * 应用系统计时事件执行者
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class CountTimeRunnable implements Runnable {

    public static int mCallOvertime = AppConfig.MAX_QUERY_TIME;
    public static int mKeyboardEventViewTimeCount;//按键数字清除倒计时,10s为准
    public static int mPhoneTimeCount;// 接通状态的倒计时时间，最长50秒
    public static int mUpdateRoomCardCount;//向Linux请求更新房间卡号，30s为准
    public static boolean isTalkingOrMonitoring;// true:接通状态,显示UI
    public static boolean isUnlocking;//规避检测刷卡超时

    public static int mCallOvertimeCount;//界面超时时间，30s为准
    private static int mNetStateCount;//android和linux链接状态，12s为准
    private static int mPlatformTimeCount;//向平台请求时间，60s为准
    private static int mTextFlag = AppConfig.DIALOG_TEXT_NORMAL;//界面倒计时种类
//    private static int mADTimeCount;//广告倒计时,5s为准

    private TimerCallback mTimerCallback;
    private Handler mHandler;

    public CountTimeRunnable(TimerCallback timerCallback, Handler handler) {
        this.mTimerCallback = timerCallback;
        this.mHandler = handler;
    }

    public static void startTalkingOrMonitoring(int callOvertime, int textFlag) {
        mTextFlag = textFlag;
        mPhoneTimeCount = 0;
        isTalkingOrMonitoring = true;
        DeviceApplication.DEVICE_WORKING_STATUS = DeviceApplication.DEVICE_WORKING;
    }

    public static void stopTalkingOrMonitoring() {
        isTalkingOrMonitoring = false;
        DeviceApplication.DEVICE_WORKING_STATUS = DeviceApplication.DEVICE_FREE;
    }

    @Override
    public void run() {
        if (isTalkingOrMonitoring) {
            mPhoneTimeCount++;
        }
        mKeyboardEventViewTimeCount++;
        mCallOvertimeCount++;
        mNetStateCount++;
        mUpdateRoomCardCount++;
        mPlatformTimeCount++;
//        mADTimeCount++;

//        DDLog.i("CountTimeRunnable.clazz--->>>run() mKeyboardEventViewTimeCount:"
//                + mKeyboardEventViewTimeCount + ";mCallOvertimeCount: " + mCallOvertimeCount
//                + ";mNetStateCount:" + mNetStateCount + ";mPlatformTimeCount:"
//                + mPlatformTimeCount + ";mCallOvertime:" + mCallOvertime);
        mTimerCallback.countTalkingOrMonitoringTime(isTalkingOrMonitoring, mPhoneTimeCount, mTextFlag);
        if (DeviceApplication.isCallStatus && mCallOvertimeCount > mCallOvertime) {// 到了自动挂断
            DDLog.i("CountTimeRunnable.clazz--->>>run() mCallOvertime:" + mCallOvertime
                    + ";callOvertimeCount: " + mCallOvertimeCount);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mCallOvertimeCount = 0;
                    DeviceApplication.isCallStatus = false;
                    mTimerCallback.onNoAnswered();
                }
            });
        }

        //如果是正在刷卡状态，5s过后设备还没反应那么就提示开锁失败；
        // 10s过后，清除屏幕按键数字 or 清除屏幕按键密码框
        if (isUnlocking) {
            if (mKeyboardEventViewTimeCount > 2) {//暂时定义检查平台卡号开锁时间为3s
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        isUnlocking = false;
                        mTimerCallback.unLockTip();
                    }
                });
            }
        }
        if (mKeyboardEventViewTimeCount > 10) {
            DDLog.i("CountTimeRunnable.clazz--->>>run() mKeyboardEventViewTimeCount:"
                    + mKeyboardEventViewTimeCount);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mKeyboardEventViewTimeCount = 0;
                    mTimerCallback.resetKeyboardEventStatus();
                }
            });
        }

        //检测Android板和Linux板通信状态
        if (mNetStateCount > 12) {
            DDLog.i("CountTimeRunnable.clazz--->>>run() mNetStateCount:" + mNetStateCount
                    + ",mTimerCallback:" + mTimerCallback);
            if (mNetStateCount > 20) System.exit(0);//应用上就重启
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mNetStateCount = 0;
                    mTimerCallback.reportALConnectedState(DeviceApplication.mNetStateCount == 0);
                }
            });
        }

        //向Linux请求更新房间卡号
        if (mUpdateRoomCardCount > 30) {
            DDLog.i("CountTimeRunnable.clazz--->>>run() mUpdateRoomCardCount:" + mUpdateRoomCardCount
                    + ",RoomIDSet size:" + DeviceApplication.mRoomIDSet.size()
                    + ",VerifyRoomInfo size:" + DeviceApplication.mVerifyRoomList.size());
            mUpdateRoomCardCount = 0;
            if (!DeviceApplication.mRoomIDSet.isEmpty() && Parser.mPullRoomIdFlag == 3) {
                DDLog.i("CountTimeRunnable.clazz--->>>run() getRoomCardInfo!!!!");
                DongDongCenter.getRoomCardInfo(0, DeviceApplication.mRoomIDSet);
            }
        }

        //更新平台时间，单位分钟
        if (mPlatformTimeCount > 60) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPlatformTimeCount = 0;
                    mTimerCallback.onTime("", false);
                }
            });
        }

        //请求到数据写入到数据库
        if (!DeviceApplication.mVerifyRoomList.isEmpty()) {
            DDLog.i("CountTimeRunnable.clazz--->>>---->>>> VerifyRoom Info size:"
                    + DeviceApplication.mVerifyRoomList.size());
            RoomInfoOpeManager.checkRoomInfo(BaseApplication.context(),
                    DeviceApplication.mVerifyRoomList, AppConfig.MAX_CHECK_ROOM_INFO_COUNT);
        }

        //每隔5s更新界面广告图片
//        if (mADTimeCount > 5) {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    mADTimeCount = 0;
//                    mTimerCallback.onADPagerChanged();
//                }
//            });
//        }
    }
}
