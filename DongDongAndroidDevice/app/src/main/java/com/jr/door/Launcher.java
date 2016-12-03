package com.jr.door;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dongdong.AppConfig;
import com.dongdong.DeviceApplication;
import com.dongdong.DeviceApplication.OnKeyboardEventsChangeListener;
import com.dongdong.base.BaseApplication;
import com.dongdong.interf.LauncherCallback;
import com.dongdong.interf.TimerCallback;
import com.dongdong.phone.ytx.YTXAccountMessage;
import com.dongdong.phone.ytx.YTXPlayPhone;
import com.dongdong.prompt.CountTimeRunnable;
import com.dongdong.prompt.KeyEventDialogManager;
import com.dongdong.prompt.KeyEventManager;
import com.dongdong.prompt.MediaMusicOfCall;
import com.dongdong.prompt.PromptSound;
import com.dongdong.sdk.DongDongCenter;
import com.dongdong.sdk.SocketThreadManager;
import com.dongdong.socket.beat.ALinuxData;
import com.dongdong.socket.beat.PeerAddressCallback;
import com.dongdong.socket.beat.Search;
import com.dongdong.socket.normal.APlatData;
import com.dongdong.socket.normal.InfoNetParam;
import com.dongdong.utils.DDLog;
import com.dongdong.utils.DeviceInfoUtils;
import com.dongdong.utils.NetUtils;
import com.dongdong.utils.SPUtils;
import com.dongdong.utils.TimeZoneUtil;
import com.dongdong.utils.TimerTaskManager;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatReportStrategy;
import com.tencent.stat.StatService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

/**
 * 启动界面，负责所有事务回调
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class Launcher extends Activity implements LauncherCallback, TimerCallback,
        PeerAddressCallback, OnKeyboardEventsChangeListener {

    private Unbinder mUnBinder;
    @BindView(R.id.unitdevice)
    LinearLayout mLlUnitDevice;
    @BindView(R.id.walldevice)
    LinearLayout mLlWallDevice;
    @BindView(R.id.walldevice_dong_hao)
    LinearLayout mLlWallDeviceDongHao;
    @BindView(R.id.walldevice_unit)
    LinearLayout mLlWallDeviceUnit;
    @BindView(R.id.walldevice_room_number)
    LinearLayout mLlWallDeviceRoomNum;

    @BindView(R.id.tv_time)
    TextView mTvPlatformTime;

    @BindView(R.id.netcard_one_statu)
    View mAndroidState;
    @BindView(R.id.netcard_two_statu)
    View mLinuxState;

    @BindView(R.id.surface_view)
    VideoView mVideoView;
//    @BindView(R.id.ad_view_pager)
//    AdViewPager mViewPager;

    @BindView(R.id.bt_test_right)
    Button mBtTestLeft;
    @BindView(R.id.bt_test_left)
    Button mBtTestRight;

    public static boolean mIsALConnected;

    private YTXPlayPhone mYTXPlayPhoneManager;
    private YTXAccountMessage mAccountMessage = new YTXAccountMessage();

    private KeyEventManager mKeyEventManager;
    private KeyEventDialogManager mKeyEventDialogManager;
//    private MediaMusicOfCall mMediaMusic;

    private Search mSearchCast;
    private NetBroadcastReceiver mReceiver;
    private IntentFilter mFilter = new IntentFilter();

    private final static ThreadLocal<SimpleDateFormat> mDateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            //yyyy-MM-dd HH:mm:ss
            return new SimpleDateFormat("HH:mm", Locale.getDefault());
        }
    };
    private String xmlPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ad.avi";

    public static final int UPDATE_TALKING_MONITORING_TIME_WHAT = 0;
    public static final int UPDATE_DIALOG_WHAT = 1;

    /**
     * 执行时时任务
     */
    public Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case UPDATE_TALKING_MONITORING_TIME_WHAT://通话、监视、呼叫更新对话框
                    int time = msg.arg1;
                    int testType = msg.arg2;
                    String remainTime = String.valueOf(AppConfig.MAX_TALKING_OR_MONITORING_TIME - time);
                    if (testType == AppConfig.DIALOG_TEXT_DIAL) {
                        remainTime = String.format(BaseApplication.resources().
                                getString(R.string.phone_calling_tip), remainTime);
                    }
                    DDLog.d("Launcher.clazz--->>>time:" + time + ",testType:" + testType);
                    mKeyEventDialogManager.setTvCountTime(remainTime);
                    if (time > AppConfig.MAX_TALKING_OR_MONITORING_TIME) {
                        DDLog.d("Launcher.clazz--->>>mHandler what 0 "
                                + "timeout of 55s and auto handUp!!!");
                        mKeyEventDialogManager.setAdCurrVolume();
                        mKeyEventDialogManager.dismissNormalDialog();
                        DongDongCenter.handUp(2);
                        mYTXPlayPhoneManager.hangUp();
                        CountTimeRunnable.stopTalkingOrMonitoring();
                    }
                    break;
                case UPDATE_DIALOG_WHAT://按#号键后如果2秒内没收到linux回应，那么关闭对话框
                    mKeyEventDialogManager.dismissQueryRoomDialog();
                    mKeyEventManager.resetKeyboardEventStatus();
                    String tip = BaseApplication.context().getString(R.string.call_room_failed)
                            + AppConfig.CALL_ROOM_UNCOMMINU_ERROR;
                    BaseApplication.showToast(tip);
                    PromptSound.callResult(false);
                    DDLog.i("Launcher.clazz--->>>Handler UPDATE_DIALOG_WHAT dismissQueryRoomDialog");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new ContextWrapper(newBase) {
            @Override
            public Object getSystemService(String name) {
                if (Context.AUDIO_SERVICE.equals(name))
                    return getApplicationContext().getSystemService(name);
                return super.getSystemService(name);
            }
        });
        DDLog.i("Launcher.clazz--->>> attachBaseContext...............");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Vitamio.isInitialized(getApplicationContext());
        setContentView(R.layout.activity_main);
        // 通过注解绑定控件
        mUnBinder = ButterKnife.bind(this);
        // 腾讯MAT///////////////////////////////////////
        // 打开debug开关，可查看mta上报日志或错误
        // 发布时，请务必要删除本行或设为false
        // StatConfig.setDebugEnable(true);
        initMTAConfig(false);
        StatService.trackCustomEvent(this, "onCreate", "");
        // 自动activity埋点
        StatService.registerActivityLifecycleCallbacks(getApplication());
        // ///////////////////////////////////////
        String accountSID = (String) SPUtils.getParam(getApplicationContext(),
                SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_ACCOUNT_SID, "");
        String authToken = (String) SPUtils.getParam(getApplicationContext(),
                SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_AUTH_TOKEN, "");
        String appID = (String) SPUtils.getParam(getApplicationContext(),
                SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_APP_ID, "");
        String vendorPhone = (String) SPUtils.getParam(getApplicationContext(),
                SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_VENDOR_PHONE, "");
        mAccountMessage.setAccountSID(accountSID);
        mAccountMessage.setAuthToken(authToken);
        mAccountMessage.setAppID(appID);
        mAccountMessage.setVendorPhone(vendorPhone);
//        mMediaMusic = new MediaMusicOfCall(Launcher.this);
        MediaMusicOfCall.intPlayData(BaseApplication.context());
        mKeyEventDialogManager = new KeyEventDialogManager(Launcher.this, mVideoView);
        DongDongCenter.getInstance().initSDK(Launcher.this);// 初始化sdk
        mReceiver = new NetBroadcastReceiver();//注册网络广播
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        mYTXPlayPhoneManager = new YTXPlayPhone(Launcher.this, mKeyEventDialogManager);//拔打电话类
        if (mAccountMessage.getEffect()) {
            mYTXPlayPhoneManager.setMessageInfo(mAccountMessage);
        }
        mKeyEventManager = new KeyEventManager(Launcher.this, mKeyEventDialogManager,
                mYTXPlayPhoneManager);
        mKeyEventManager.setUIControlParams(mLlUnitDevice, mLlWallDevice, mLlWallDeviceDongHao,
                mLlWallDeviceUnit, mLlWallDeviceRoomNum);
        initTimer();// 初始化界面工作线程池
        DDLog.i("Launcher.clazz--->>> onCreate...............");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBtTestLeft.setVisibility(DDLog.isDebug ? View.VISIBLE : View.INVISIBLE);
        mBtTestRight.setVisibility(DDLog.isDebug ? View.VISIBLE : View.INVISIBLE);
        initAppStatus();// 初始化各状态值
        StatService.onResume(this);// 腾讯MAT 页面开始
        // ///////////////////////////
        if (mSearchCast == null) {
            String ID = DeviceInfoUtils.getDeviceID(getApplicationContext());
            if (!ID.equals("0") && !TextUtils.isEmpty(ID)) {
                mSearchCast = new Search();
                mSearchCast.setPeerAddressCallback(Launcher.this);
                ALinuxData.mCameraId = Integer.parseInt(ID);
                DDLog.i("Launcher.clazz--->>> onResume init Search!!!");
            } else {
                NetUtils.withoutDeviceIdAlert(Launcher.this);
            }
            //5s后的操作
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 1.开始播放视频
                    playVideo();
                    //2.保证每次设备启动都能请求初始化拨打电话功能
                    String deviceId = DeviceInfoUtils.getDeviceID(getApplicationContext());
                    YTXAccountMessage.getVOIPParamRequest(Integer.parseInt(deviceId), 2);

                    //test 图片广告
                    //mViewPager.showUI();
                }
            }, 5 * 1000);
        }
        // ///////////////////////////
        ((DeviceApplication) getApplication()).addOnKeyboardEventsChangeListener(this);
        registerReceiver(mReceiver, mFilter);
        DDLog.i("Launcher.clazz--->>> onResume...............");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 腾讯MAT 页面结束
        StatService.onPause(this);
        unregisterReceiver(mReceiver);
        DDLog.i("Launcher.clazz--->>> onPause............");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //要保证什么时候都能刷卡开锁
        ((DeviceApplication) getApplication()).removeOnKeyboardEventsChangeListener(this);
        DongDongCenter.getInstance().finishSDK();
        mHandler.removeCallbacksAndMessages(null);
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            mVideoView = null;
        }
        Debug.stopMethodTracing();
//        mMediaMusic.release();
        MediaMusicOfCall.release();
        stopTimer();
        if (mSearchCast != null) {
            mSearchCast.stopSearch();
            mSearchCast.stopUnicastSearch();
        }
        mUnBinder.unbind();
        DDLog.i("Launcher.clazz--->>>onDestroy..........");
    }

    /**
     * 初始化各状态
     */
    public void initAppStatus() {
        DeviceApplication.DEVICE_WORKING_STATUS = DeviceApplication.DEVICE_FREE;//设备处于空闲状态
        CountTimeRunnable.isTalkingOrMonitoring = false;//设备处于非监控或者对讲状态
        DeviceApplication.isSystemSettingStatus = false;// true---系统设置状态  false--非系统设置状态
        DeviceApplication.isCallStatus = false;// 是否在呼叫状态
        DeviceApplication.isYTXPhoneCall = false;// 是否云通讯在打电话状态
    }

    public KeyEventManager getKeyboardEvents() {
        return mKeyEventManager;
    }

    private class NetBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                boolean isConnected = NetUtils.isConnected(context);
                Launcher.this.onNetConnect(isConnected);
            }
        }
    }

    private CountTimeRunnable mRunnable = new CountTimeRunnable(Launcher.this, mHandler);
    //private CountTimeRunnable mRunnable = new CountTimeRunnable();

    /**
     * 开始执行任务
     */
    private void initTimer() {
        // 调用此方法便执行一次，然后每1秒执行一次，此处最好不要用Timer定时器，Timer是根据系统时间来做为执行
        //事件单位，也不要用Handler,原因一样。如果系统时间有问题，那么我们的任务就死了。这里最好用线程池！！！
        TimerTaskManager.addOneTask(mRunnable);
//        TimerTaskManager.addOneTask(mRunnable2);
        DDLog.i("Launcher.clazz--->>>initTimer  !!!!");
    }

    /**
     * 结束任务
     */
    private void stopTimer() {
        TimerTaskManager.exitExecutor();
    }

    private void playVideo() {
        //初始化vitamio框架
        boolean initialized = Vitamio.isInitialized(BaseApplication.context());
        DDLog.i("Launcher.clazz--->>>Vitamio ########HardwareDecoder no%%%%%%%%%%%%%%%%#############initialized:" + initialized);
        if (initialized) {
            File video = new File(xmlPath);
            if (mVideoView != null) {
                if (video.exists()) {
                    mVideoView.setVideoPath(xmlPath);
                    // 不用MediaController可以过滤点击事件
                    // MediaController mediaController = new
                    // MediaController(this);
                    // mVideoView.setMediaController(mediaController);
                    mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_ORIGIN, 0);
                    //mVideoView.setHardwareDecoder(true);//硬解码
                    mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.setLooping(true);
                            DDLog.i("Launcher.clazz--->>>setOnPreparedListener!!!!!!!!!!!!!!!!  !!!!");
                            mKeyEventDialogManager.setAdCurrVolume();
                        }
                    });
                    mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            //播放完之后的回调
                        }
                    });
                    mVideoView.setOnErrorListener(new OnErrorListener() {

                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            mp.release();
                            mVideoView.stopPlayback();
                            DDLog.i("Launcher.clazz--->>>setOnErrorListener!!!!!!!!!!!!!!!!  !!!!");
                            String tip = BaseApplication.context().getString(
                                    R.string.error_tip) + AppConfig.PLAY_VIDEO_ERROR;
                            BaseApplication.showToast(tip);
                            return false;
                        }
                    });
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////以下是响应各种回调
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 呼叫回来的状态
     *
     * @param status 呼叫回来的状态
     * @return result
     */
    @Override
    public int onPlayOrStopDevice(final int status) {// 呼叫回来的状态
        DDLog.i("Launcher.clazz*****************onPlayOrStopDevice status:"
                + status);
        mKeyEventManager.resetKeyboardEventStatus();
        mKeyEventDialogManager.dismissQueryRoomDialog();
        mKeyEventDialogManager.onPlayOrStopDevice(status);
        return 0;
    }

    /**
     * 呼叫住户后查询的回调
     *
     * @param result       查询结果
     * @param callOvertime 超时时间
     */
    @Override
    public void onQueryRoomResult(int result, String roomNum, int callOvertime) {
        boolean hasMessages = mHandler.hasMessages(UPDATE_DIALOG_WHAT);
        DDLog.i("Launcher.clazz#################### onQueryRoomResult result:"
                + result + ",roomNum" + roomNum + ",callOvertime: " + callOvertime
                + ",hasMessages:" + hasMessages);
        if (hasMessages) {
            mHandler.removeMessages(UPDATE_DIALOG_WHAT);
        }
        mKeyEventManager.resetKeyboardEventStatus();
        if (result != APlatData.RESULT_SUCCESS) {
            DeviceApplication.isCallStatus = false;
            CountTimeRunnable.mCallOvertime = callOvertime;
            mKeyEventDialogManager.dismissQueryRoomDialog();
        }
        mKeyEventDialogManager.onQueryRoomResult(result, roomNum, callOvertime);
    }

    /**
     * 查询住户号直接拨打电话的回调
     *
     * @param roomNum  查询的房号
     * @param phoneNum 房号下的手机号码(住户)
     * @return state
     */
    @Override
    public int onDialRequest(String roomNum, String phoneNum) {
        DDLog.i("Launcher.clazz onDialRequest phoneNum:" + phoneNum
                + ",roomNum:" + roomNum);
        if (mAccountMessage.getEffect()) {
            mKeyEventDialogManager.dismissQueryRoomDialog();
            mKeyEventDialogManager.showNormalDialog(R.mipmap.calling_state);
            DeviceApplication.isYTXPhoneCall = true;
            CountTimeRunnable.startTalkingOrMonitoring(AppConfig.MAX_TALKING_OR_MONITORING_TIME,
                    AppConfig.DIALOG_TEXT_DIAL);
            mKeyEventDialogManager.setAdVolumeSilentVolume();
            mYTXPlayPhoneManager.makeCall(roomNum, phoneNum);
        } else {
            int deviceId = Integer.parseInt(DeviceInfoUtils.getDeviceID(getApplicationContext()));
            DDLog.i("Launcher.clazz onDialRequest AccountMessage is not effect " +
                    "and we getParams:" + phoneNum + ",roomNum:" + roomNum);
            String tip = getString(R.string.phone_call_error) + AppConfig.PARAMS_NO_EFFECT_ERROR;
            BaseApplication.showToast(tip);
            YTXAccountMessage.getVOIPParamRequest(deviceId, 2);
        }
        return 0;
    }

    /**
     * 向核心板发送卡号开锁
     *
     * @param cardNum 卡号
     */
    @Override
    public void onSendCardUnlock(String cardNum) {
        DDLog.i("Launcher.clazz--->>>onSendCardUnlock cardNum:" + cardNum);
        CountTimeRunnable.mKeyboardEventViewTimeCount = 0;
        CountTimeRunnable.isUnlocking = true;
        DongDongCenter.validCardRequest(cardNum);
    }

    /**
     * 更新界面按键图标
     *
     * @param str 图标数字
     */
    @Override
    public void onUpdateNumberView(String str) {
        CountTimeRunnable.mKeyboardEventViewTimeCount = 0;
        DDLog.i("Launcher.clazz--->>>onUpdateNumberView str:" + str);
        if (!DeviceApplication.isSystemSettingStatus) {
            mKeyEventManager.onUpdateNumberView(str);
        }
    }

    /**
     * 查询平台卡号开锁返回结果
     *
     * @param result  执行结果
     * @param cardNum 卡号
     */
    @Override
    public void onCheckCardResult(int result, String cardNum) {
        DDLog.i("Launcher.clazz--->>>onCheckCardResult  result:" + result);
        CountTimeRunnable.isUnlocking = false;
        mKeyEventManager.unlockRequest(result, AppConfig.UNLOCK_TYPE_PLATFORM_CARD, cardNum);
    }

    /**
     * 密码开锁
     *
     * @param result 执行结果
     */
    @Override
    public void onPwdUnlock(int result) {
        DDLog.i("Launcher.clazz--->>>onPwdUnlock  result:" + result);
        mKeyEventManager.unlockRequest(result, AppConfig.UNLOCK_TYPE_PASSWORD, "0");
    }

    /**
     * App请求开锁
     *
     * @param unlockType     开锁类型
     * @param cardOrPhoneNum 电话号码
     * @return state
     */
    @Override
    public int onUnlockRequest(int unlockType, String cardOrPhoneNum) {
        DDLog.i("Launcher.clazz--->>>onUnlockRequest  unlockType:" + unlockType);
        mKeyEventManager.unlockRequest(0, unlockType, cardOrPhoneNum);
        return 0;
    }

    /**
     * 查询本地卡号开锁
     *
     * @param cardNum 卡号
     */
    @Override
    public void onLocalCardUnlock(int unlockType, String cardNum) {
        DDLog.i("Launcher.clazz--->>>onLocalCardUnlock  unlockType:" + unlockType);
        mKeyEventManager.unlockRequest(0, unlockType, cardNum);
    }

    /**
     * TimerCallback 每分钟向Linux请求最新时间，并更新界面
     *
     * @param time 暂时没用
     * @param flag 暂时没用
     */
    @Override
    public void onTime(String time, boolean flag) {
        if (mIsALConnected) {
            DongDongCenter.getTimeStamp(0);//去向Linux要时间
            if (mTvPlatformTime != null)
                mTvPlatformTime.setVisibility(View.VISIBLE);
        } else {
            if (mTvPlatformTime != null)
                mTvPlatformTime.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * TimerCallback 呼叫超时
     */
    @Override
    public void onNoAnswered() {
        DDLog.i("Launcher.clazz--->>>onNoAnswered overtime!!!");
        mKeyEventManager.nobodyAnswered();
        mKeyEventDialogManager.setAdCurrVolume();
        mKeyEventDialogManager.dismissQueryRoomDialog();
        CountTimeRunnable.stopTalkingOrMonitoring();
    }

    /**
     * TimerCallback 重置按键界面
     */
    @Override
    public void resetKeyboardEventStatus() {
        mKeyEventManager.resetKeyboardEventStatus();
    }

    /**
     * TimerCallback 拨打电话超时
     */
    @Override
    public void phoneCallWaitTimeOut() {
        DDLog.i("Launcher.clazz phoneCallWaitTimeOut !!!");
        mKeyEventDialogManager.dismissNormalDialog();
    }

    /**
     * TimerCallback 呼叫、监视，对讲界面更新回调
     *
     * @param isPhoneCalling 是否正在呼叫或者拨打电话
     * @param time           倒计时
     * @param textFlag       倒计时的类型：拨打电话/通话、监视
     */
    @Override
    public void countTalkingOrMonitoringTime(boolean isPhoneCalling, int time, int textFlag) {
        if (isPhoneCalling) {
            Message message = Message.obtain(mHandler, UPDATE_TALKING_MONITORING_TIME_WHAT);
            message.arg1 = time;
            message.arg2 = textFlag;
            mHandler.sendMessage(message);
        }
    }

    /**
     * TimerCallback 检查Android板和核心板通信状态的心跳包
     *
     * @param isOut 是否离线
     */
    @Override
    public void reportALConnectedState(boolean isOut) {
        DDLog.i("Launcher.clazz reportALConnectedState----->>>" + ";isOut:" + isOut
                + ",mNetStateCount:" + DeviceApplication.mNetStateCount);
        mIsALConnected = !isOut;
        DeviceApplication.mNetStateCount = 0;
        mLinuxState.setBackgroundResource(isOut ? R.drawable.net_statu_circle_disconnected
                : R.drawable.net_statu_circle_connected);
        if (isOut) {
            if (mSearchCast != null) {
                SocketThreadManager.startSocketThread(new Runnable() {

                    @Override
                    public void run() {
                        mSearchCast.sendMulticastSocket2Group();
                    }
                }, "reportALConnectedState");
            }
        }
    }

    /**
     * TimerCallback 平台开锁检查超时后提示
     */
    @Override
    public void unLockTip() {
        DDLog.i("Launcher.clazz--->>>unLockTip");
        mKeyEventManager.unlockRequest(APlatData.RESULT_FAILED,
                AppConfig.UNLOCK_TYPE_PLATFORM_CARD, "");
    }

    /**
     * TimerCallback 每5秒更新广告图片
     */
    @Override
    public void onADPagerChanged() {
        DDLog.i("Launcher.clazz--->>>onADPagerChanged");
//        if (mViewPager != null)
//            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
    }

    /**
     * 手机有网后的系统回调
     *
     * @param isConnected 是否有网
     */
    public void onNetConnect(boolean isConnected) {
        if (isConnected) {
            if (mSearchCast != null) {
                mSearchCast.startSearch();
            }
            BaseApplication.showToast(R.string.net_connected);
        } else {
            if (mSearchCast != null) {
                mSearchCast.stopSearch();
                mSearchCast.stopUnicastSearch();
            }
            BaseApplication.showToast(R.string.net_disconnected);
        }
        if (mAndroidState != null)
            mAndroidState.setBackgroundResource(isConnected ? R.drawable.net_statu_circle_connected
                    : R.drawable.net_statu_circle_disconnected);
    }

    /**
     * 核心板回调更新IP
     *
     * @param ip 核心板IP
     * @return state
     */
    @Override
    public int onPeerAddress(String ip) {
        DeviceApplication.mNetStateCount++;
        AppConfig.SERVER_HOST_IP = ip;
        DDLog.i("Launcher.clazz<<<>>>onPeerAddress ip:" + ip + ",NetStateCount:"
                + DeviceApplication.mNetStateCount);
        //规避定时器死亡
//        if (DeviceApplication.mNetStateCount > 5) {
//            DeviceApplication.mNetStateCount = 0;
//            initTimer();
//        }
        return 0;
    }

    /**
     * 核心板回调更新状态和版本号
     *
     * @param regState 核心板状态
     * @param version  核心板版本号
     * @return state
     */
    @Override
    public int onDrive(int regState, String version) {
        DDLog.i("Launcher.clazz<<<>>>onDrive regState:" + regState
                + ",version: " + version);
        DeviceApplication.mVersion = version;
        DeviceApplication.mRegStatus = regState;
        return 0;
    }

    //暂时不用
    @Override
    public int onUcpaasInfo(final String sid, final String pwd, final String appId) {
        return 0;
    }

    /**
     * 接收核心板发出停止打电话的回调
     *
     * @return state
     */
    @Override
    public int onStopPhoneCallRequest() {
        DDLog.i("Launcher.clazz onStopPhoneCallRequest !!!");
        mYTXPlayPhoneManager.hangUp();
        return 0;
    }

    /**
     * 呼叫住户后得知设备拨打电话余额不足的回调
     *
     * @param reason 0:余额不足；1:拨打过于频繁，稍后再试
     * @return state
     */
    @Override
    public int onDisablePhoneCallRequest(int reason) {
        if (reason == 0) {
            BaseApplication.showToast(getString(R.string.device_no_money));
        } else {
            BaseApplication.showToast(getString(R.string.frequently_phone_call));
        }
        return 0;
    }

    /**
     * 得到核心板信息后的回调
     */
    @Override
    public void onGetWifiParams(InfoNetParam infoNetParam) {
        DDLog.i("Launcher.clazz onGetWifiParams infoNetParam:" + infoNetParam);
        DongDongCenter.onGetWifiParams(infoNetParam);
    }

    /**
     * 设置核心板参数的请求回调
     */
    @Override
    public void onSetWifiParams(int result) {
        DDLog.i("Launcher.class onSetWifiParams result:" + result);
        DongDongCenter.onSetWifiParams(result);
    }

    /**
     * 设备第一次启动核心板发过来拨打电话的配置参数
     *
     * @param userId      登录云通讯帐号
     * @param accountSid  云通讯主帐号
     * @param appToken    应用Token
     * @param appKey      应用appId
     * @param vendorPhone 厂商固话
     * @return state
     */
    @Override
    public int onGetYunTongXunInfo(String userId, String accountSid,
                                   String appToken, String appKey, String vendorPhone) {
        if (!accountSid.equals(mAccountMessage.getAccountSID())
                || !appToken.equals(mAccountMessage.getAuthToken())
                || !appKey.equals(mAccountMessage.getAppID())) {
            SPUtils.setParam(getApplicationContext(),
                    SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_ACCOUNT_SID, accountSid);
            SPUtils.setParam(getApplicationContext(),
                    SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_AUTH_TOKEN, appToken);
            SPUtils.setParam(getApplicationContext(),
                    SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_APP_ID, appKey);
            SPUtils.setParam(getApplicationContext(),
                    SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_VENDOR_PHONE, vendorPhone);
            mAccountMessage.setAccountSID(accountSid);
            mAccountMessage.setAuthToken(appToken);
            mAccountMessage.setAppID(appKey);
            mAccountMessage.setVendorPhone(vendorPhone);
            mYTXPlayPhoneManager.setMessageInfo(mAccountMessage);
        }
        DDLog.i("Launcher.clazz --->>>onGetYunTongXunInfo AccountMessage  =" + mAccountMessage);
        //初始化打电话配置
        if (mAccountMessage.getEffect()) {
            mYTXPlayPhoneManager.startInitYTX();
        }
        return 0;
    }

    /**
     * 上传开门记录成功后，再判断
     *
     * @param dataType 核心板上传开门记录成功状态
     * @param result   核心板上传开门记录成功条数，时时数据count=0,本地数据大于0
     * @return state
     */
    @Override
    public int onUnlockTypeResult(int dataType, int result) {
        DDLog.i("Launcher.clazz onUnlockTypeResult dataType:" + dataType
                + ";result:" + result);
        if (dataType == APlatData.UNLOCK_TIME_DATA) {
            if (result == APlatData.RESULT_SUCCESS) {
                mKeyEventManager.getUnlockLog();
            }
        }
        return 0;
    }

    public void testDelete(View view) {
    }

    public void testQuery(View view) {
    }

    @Override
    public int onGetTimestampResult(int platformTime) {
        long timeL = platformTime * 1000L;
        Date nowDate = TimeZoneUtil.transformTime(new Date(timeL),
                TimeZone.getTimeZone("GMT+08"), TimeZone.getTimeZone("GMT+00"));
        boolean isToday = TimeZoneUtil.isToday(nowDate.getTime());
        if (!isToday) {
            boolean setTimeState = SystemClock.setCurrentTimeMillis(nowDate.getTime());
            if (mVideoView != null) mVideoView.resume();
            //初始化打电话配置
            if (mAccountMessage.getEffect() && !mYTXPlayPhoneManager.initializedYTX()) {
                mYTXPlayPhoneManager.startInitYTX();
            }
            DDLog.i("Launcher.clazz--->>>onGetTimestampResult is not today:"
                    + mAccountMessage + ",setTimeState:" + setTimeState);
        }
        String strTime = mDateFormat.get().format(nowDate);
        DDLog.i("Launcher.clazz--->>>onGetTimestampResult timeL: " + timeL
                + ",isToday:" + isToday + ";strTime:" + strTime + ";initializedYTX:"
                + mYTXPlayPhoneManager.initializedYTX() + ";nowDate:" + nowDate);
        if (mTvPlatformTime != null) {
            mTvPlatformTime.setText(strTime);
        }
        return 0;
    }

    /**
     * 根据不同的模式，建议设置的开关状态，可根据实际情况调整，仅供参考。
     *
     * @param isDebugMode 根据调试或发布条件，配置对应的MTA配置
     */
    private void initMTAConfig(boolean isDebugMode) {
        if (isDebugMode) { // 调试时建议设置的开关状态
            // 查看MTA日志及上报数据内容
            StatConfig.setDebugEnable(true);
            // 禁用MTA对app未处理异常的捕获，方便开发者调试时，及时获知详细错误信息。
            StatConfig.setAutoExceptionCaught(false);
        } else { // 发布时，建议设置的开关状态，请确保以下开关是否设置合理
            // 禁止MTA打印日志
            StatConfig.setDebugEnable(false);
            // 根据情况，决定是否开启MTA对app未处理异常的捕获
            StatConfig.setAutoExceptionCaught(true);
            // 选择默认的上报策略
            StatConfig.setStatSendStrategy(StatReportStrategy.APP_LAUNCH);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    ///////当按键驱动为Android原生时调用下面方法
    /////////////////////////////////////////////////////////////////////////////////////
    private long lastTimeMillis;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean shouldDispatch = false;
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            long currentTimeMillis = System.currentTimeMillis();
            if (Math.abs(currentTimeMillis - lastTimeMillis) < 300) {
                shouldDispatch = true;
            } else {
                int keyCode = event.getKeyCode();
                String number = "*";
                switch (keyCode) {
                    case KeyEvent.KEYCODE_0:
                    case KeyEvent.KEYCODE_1:
                    case KeyEvent.KEYCODE_2:
                    case KeyEvent.KEYCODE_3:
                    case KeyEvent.KEYCODE_4:
                    case KeyEvent.KEYCODE_5:
                    case KeyEvent.KEYCODE_6:
                    case KeyEvent.KEYCODE_7:
                    case KeyEvent.KEYCODE_8:
                    case KeyEvent.KEYCODE_9:
                        number = keyCode - 7 + "";
                        break;
                    case KeyEvent.KEYCODE_STAR:
                        number = "*";
                        break;
                    case KeyEvent.KEYCODE_POUND:
                        number = "#";
                        break;
                }
                DDLog.i("Launcher.clazz--->>>>>>>>>>>>>> dispatchKeyEvent number:" + number);
                if (DeviceApplication.DEVICE_WORKING_STATUS ==
                        DeviceApplication.DEVICE_WORKING && !number.equals("*")) {
                    return true;
                } else {
                    onUpdateNumberView(number);
                }
                lastTimeMillis = currentTimeMillis;
                shouldDispatch = true;
            }
        }
        return shouldDispatch;
    }
}