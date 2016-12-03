package com.dongdong.prompt;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.dongdong.AppConfig;
import com.dongdong.DeviceApplication;
import com.dongdong.base.BaseApplication;
import com.dongdong.socket.normal.APlatData;
import com.dongdong.ui.dialog.CommonDialog;
import com.dongdong.utils.DDLog;
import com.dongdong.utils.SPUtils;
import com.dongdong.widget.RippleView;
import com.jr.door.R;

import io.vov.vitamio.widget.VideoView;

/**
 * 所有Launcher界面状态对话框的管理者
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class KeyEventDialogManager {

    private CommonDialog mNormalDialog;
    private CommonDialog mQueryRoomDialog;
    private ImageView mIvDialog;
    private Activity mActivity;
    private VideoView mVideoView;

    private RippleView mRippleLayout;
    private final TextView mTvCountTime;//监控或对讲时间

    public KeyEventDialogManager(Activity activity, VideoView videoView) {
        this.mActivity = activity;
        this.mVideoView = videoView;
        //普通标识设备状态对话框
        View diaView = View.inflate(activity, R.layout.device_normal_dialog_layout, null);
        mIvDialog = (ImageView) diaView.findViewById(R.id.iv_device_dialog);
        mTvCountTime = (TextView) diaView.findViewById(R.id.tv_count_time);
        mNormalDialog = new CommonDialog(activity, R.style.dialog_transparent);
        mNormalDialog.setContent(diaView);
        Window normalWindow = mNormalDialog.getWindow();
        if (normalWindow != null)
            normalWindow.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);//去焦点

        // 呼叫房号后的对话框
        View callDiaView = View.inflate(activity, R.layout.query_roomnumber_dialog_layout, null);
        mRippleLayout = (RippleView) callDiaView.findViewById(R.id.ripple_view);
        mQueryRoomDialog = new CommonDialog(activity, R.style.dialog_transparent);
        mQueryRoomDialog.setContent(callDiaView);
        Window queryWindow = mQueryRoomDialog.getWindow();
        if (queryWindow != null)
            queryWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);//去焦点
    }

    public void setTvCountTime(String time) {
        if (mNormalDialog.isShowing()) {
            String newTime = String.format(mActivity.getResources().
                    getString(R.string.second_sign), time);
            mTvCountTime.setText(newTime);
        }
    }

    void showQueryRoomDialog() {
        if (mQueryRoomDialog.isShowing()) {
            mQueryRoomDialog.dismiss();
        }
        DeviceApplication.DEVICE_WORKING_STATUS = DeviceApplication.DEVICE_WORKING;
        DeviceApplication.isCallStatus = true;
        CountTimeRunnable.mCallOvertimeCount = 0;
        MediaMusicOfCall.playMusic();
        if (mRippleLayout.isStarting()) {
            mRippleLayout.stop();
        }
        mRippleLayout.start();
        mQueryRoomDialog.show();
    }

    public void showNormalDialog(int mipmap) {
        if (mNormalDialog.isShowing()) {
            mNormalDialog.dismiss();
        }
        DeviceApplication.DEVICE_WORKING_STATUS = DeviceApplication.DEVICE_WORKING;
        mIvDialog.setImageResource(mipmap);
        mNormalDialog.show();
        mTvCountTime.setText("");
    }

    /**
     * 关闭呼叫住户的对话框界面
     */
    public void dismissQueryRoomDialog() {
        if (mQueryRoomDialog.isShowing()) {
            DeviceApplication.DEVICE_WORKING_STATUS = DeviceApplication.DEVICE_FREE;
            DeviceApplication.isCallStatus = false;
            DeviceApplication.isYTXPhoneCall = false;
            mRippleLayout.stop();
//            mMediaMusic.stopMusic();
            MediaMusicOfCall.stopMusic();
            mQueryRoomDialog.dismiss();
        }
    }

    /**
     * 关闭设备一般状态对话框界面
     */
    public void dismissNormalDialog() {
        if (mNormalDialog.isShowing()) {
            DeviceApplication.DEVICE_WORKING_STATUS = DeviceApplication.DEVICE_FREE;
            DeviceApplication.isYTXPhoneCall = false;
//            mMediaMusic.stopMusic();
            MediaMusicOfCall.stopMusic();
            mNormalDialog.dismiss();
        }
    }

    public void setAdCurrVolume() {
        //2.广告声音
        float curAdVolume = (float) SPUtils.getParam(BaseApplication.context(),
                SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_AD_VOLUME, 1.0f);
        DDLog.i("KeyEventDialogManager.clazz--->>> curAdVolume:" + curAdVolume);
        if (mVideoView != null)
            mVideoView.setVolume(curAdVolume, curAdVolume);
    }

    public void setAdVolumeSilentVolume() {
        if (mVideoView != null)
            mVideoView.setVolume(0.0f, 0.0f);
    }

    /**
     * 播放(监控、对讲)或者停止设备后的界面状态显示
     *
     * @param status 返回状态
     */
    public void onPlayOrStopDevice(int status) {
        DDLog.i("KeyEventDialogManager.class onPlayOrStopDevice and status is " + status);
        switch (status) {
            case APlatData.STATUS_ORIGINAL: // 初始状态
                setAdCurrVolume();
                CountTimeRunnable.stopTalkingOrMonitoring();
                break;
            case APlatData.STATUS_CALLING:// 呼叫中
                setAdVolumeSilentVolume();
                break;
            case APlatData.STATUS_CALL_SUCCESS:// 呼叫成功后，请通话
                setAdVolumeSilentVolume();
                showNormalDialog(R.mipmap.talking);
                PromptSound.callResult(true);
                CountTimeRunnable.startTalkingOrMonitoring(AppConfig.MAX_TALKING_OR_MONITORING_TIME,
                        AppConfig.DIALOG_TEXT_NORMAL);
                break;
            case APlatData.STATUS_CALL_END: // 通话结束
                dismissNormalDialog();
                BaseApplication.showToast(R.string.talking_end);
                PromptSound.talkOver();
                setAdCurrVolume();
                break;
            case APlatData.STATUS_MONITORING:// 监视中
                setAdVolumeSilentVolume();
                showNormalDialog(R.mipmap.monitoring);
                CountTimeRunnable.startTalkingOrMonitoring(AppConfig.MAX_TALKING_OR_MONITORING_TIME,
                        AppConfig.DIALOG_TEXT_NORMAL);
                break;
            case APlatData.STATUS_MONITOR_END: // 监视结束
                setAdCurrVolume();
                dismissNormalDialog();
                BaseApplication.showToast(R.string.monitoring_end);
                CountTimeRunnable.stopTalkingOrMonitoring();
                break;
            default:
                break;
        }
    }

    /**
     * 呼叫住户后的界面状态显示
     *
     * @param callResult   查询房号返回结果
     * @param roomNum      房号
     * @param callOvertime 呼叫超时时间
     */
    public void onQueryRoomResult(int callResult, String roomNum, int callOvertime) {
        DDLog.i("KeyEventDialogManager.clazz onQueryRoomResult callResult: " + callResult
                + ",roomNum:" + roomNum + ",callOvertime:" + callOvertime);
        switch (callResult) {
            case APlatData.RESULT_SUCCESS: //呼叫住户后查询成功
                setAdVolumeSilentVolume();
                break;
            case APlatData.RESULT_FAILED://呼叫住户后查询失败
                String tip = BaseApplication.context().getString(R.string.call_room_failed)
                        + AppConfig.CALL_ROOM_NO_DATA_ERROR;
                BaseApplication.showToast(tip);
                PromptSound.callResult(false);
                break;
            case APlatData.RESULT_NO_ROOM://无此房号
                String noRoom = String.format(BaseApplication.resources().
                        getString(R.string.no_room), roomNum);
                BaseApplication.showToast(noRoom);
                PromptSound.callResult(false);
                break;
            case APlatData.RESULT_NO_USER://无用户
                BaseApplication.showToast(R.string.no_register_user);
                PromptSound.callResult(false);
                break;
            case APlatData.RESULT_NO_OFFLINE://设备离线
                BaseApplication.showToast(R.string.device_offline);
                PromptSound.callResult(false);
                break;
            case APlatData.RESULT_ONLY_PHONE://设备在线，用户之前登录过，现处于注销状态，只能拨打电话
                BaseApplication.showToast(R.string.user_offline);
                break;
            default:
                break;
        }
    }
}
