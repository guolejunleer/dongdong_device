package com.dongdong.phone.ytx;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import com.cloopen.rest.sdk.CCPRestSDK;
import com.dongdong.AppConfig;
import com.dongdong.DeviceApplication;
import com.dongdong.base.BaseApplication;
import com.dongdong.prompt.CountTimeRunnable;
import com.dongdong.prompt.KeyEventDialogManager;
import com.dongdong.sdk.SocketThreadManager;
import com.dongdong.socket.normal.APlatData;
import com.dongdong.socket.normal.DSPacket;
import com.dongdong.socket.normal.UdpClientSocket;
import com.dongdong.utils.DDLog;
import com.dongdong.utils.DeviceInfoUtils;
import com.dongdong.utils.SPUtils;
import com.jr.door.Launcher;
import com.jr.door.R;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECDevice.ECConnectState;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECInitParams;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPCallManager.CallType;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.VoIPCallUserInfo;
import com.yuntongxun.ecsdk.VoipMediaChangedInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * 拨打电话的管理者
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class YTXPlayPhone {

    private static final String SUCCESS_CODE = "000000";

    private Context mContext;
    private KeyEventDialogManager mKeyEventDialogManager;
    private String mRoomNum;
    private String mPhoneNum;

    private CCPRestSDK mRestAPI;
    private String mSubAccount;

    private String mCurrentCallId;
    private YTXAccountMessage mAccountMessage;

    private static YTXPlayPhone mInstance;

    private YTXPlayPhone() {
    }

    public static YTXPlayPhone getInstance() {
        if (mInstance == null) {
            synchronized (YTXPlayPhone.class) {
                if (mInstance == null)
                    mInstance = new YTXPlayPhone();
            }
        }
        return mInstance;
    }

    public void initYTXPlayPhone(Context context, KeyEventDialogManager keyEventDialogManager) {
        mContext = context;
        mKeyEventDialogManager = keyEventDialogManager;
    }

    public void setMessageInfo(YTXAccountMessage accountMessage) {
        mAccountMessage = accountMessage;
    }

    public boolean initializedYTX() {
        return ECDevice.isInitialized();
    }

    public void startInitYTX() {
        this.mSubAccount = (String) SPUtils.getParam(BaseApplication.context(),
                SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_DEVICE_ID, "");
        mRestAPI = new CCPRestSDK();
        // app.cloopen.com:8883
        // sandboxapp.cloopen.com
        // 初始化服务器地址和端口，格式如下，服务器地址不需要写https://
        mRestAPI.init("app.cloopen.com", "8883");
        mRestAPI.setAccount(mAccountMessage.getAccountSID(),
                mAccountMessage.getAuthToken());// 初始化主帐号和主帐号TOKEN
        mRestAPI.setAppId(mAccountMessage.getAppID());// 初始化应用ID
        // 先查询是否有对应的SubAccount存在,不存在就创建，最后联接服务器
        boolean initialized = initializedYTX();
        DDLog.i("YTXPlayPhone.clazz --->>>startInitYTX initialized  =" + initialized);
        if (!initialized) {
            querySubAccount();
        }
    }

    private void querySubAccount() {
        new QuerySubAccountAsyncTask().execute(mSubAccount);
    }

    public void hangUp() {
        if (mAccountMessage == null || !mAccountMessage.getEffect())
            return;
        ECVoIPCallManager callManager = ECDevice.getECVoIPCallManager();
        DDLog.i("YTXPlayPhone.clazz--->>> hangUp callManager  =" + callManager
                + "; mCurrentCallId:" + mCurrentCallId
                + "; DeviceApplication.DEVICE_WORKING_STATUS:"
                + DeviceApplication.DEVICE_WORKING_STATUS);
        if (callManager != null && !TextUtils.isEmpty(mCurrentCallId)) {
            callManager.releaseCall(mCurrentCallId);
        }
    }

    /**
     * 拨打电话
     *
     * @param roomNum  房间号
     * @param phoneNum 手机号
     */
    public void makeCall(String roomNum, String phoneNum) {
        if (TextUtils.isEmpty(roomNum) || TextUtils.isEmpty(phoneNum)) {
            BaseApplication.showToast(R.string.no_room_or_phone_num);
            mKeyEventDialogManager.dismissNormalDialog();
            return;
        }
        this.mRoomNum = roomNum;
        this.mPhoneNum = phoneNum;
        mKeyEventDialogManager.showNormalDialog(R.mipmap.calling_state);
        DeviceApplication.isYTXPhoneCall = true;
        DDLog.i("YTXPlayPhone.clazz  makeCall:  " + roomNum);
        try {
            // =======ytx makeCall start==========
            ECVoIPCallManager callManager = ECDevice.getECVoIPCallManager();
            DDLog.i("YTXPlayPhone.clazz--->>>makeCall callManager  =" + callManager);
            if (callManager != null) {
                // 来电显示
                // // 创建一个个人信息参数对象
                VoIPCallUserInfo mUserInfo = new VoIPCallUserInfo();
                mUserInfo.setNickName("DongDong");
                String vendorPhone = mAccountMessage.getVendorPhone();
                if (!TextUtils.isEmpty(vendorPhone)) {
                    mUserInfo.setPhoneNumber(vendorPhone);
                } else {
                    mUserInfo.setPhoneNumber("057158111836");
                }
                //调用VoIP设置接口注入VoIP呼叫透传参数
                ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
                setupManager.setVoIPCallUserInfo(mUserInfo);
                setYTXListener();
                mCurrentCallId = callManager.makeCall(CallType.DIRECT, phoneNum);
                BaseApplication.showToast("正在拨号"/* + roomNum*/);
                DDLog.i("YTXPlayPhone.clazz--->>>makeCall mCurrentCallId  ="
                        + mCurrentCallId + ";phoneNum = " + phoneNum + ",set 057158111836");

            } else {
                int deviceId = Integer.parseInt(DeviceInfoUtils.getDeviceID(
                        BaseApplication.context()));
                YTXAccountMessage.getVOIPParamRequest(deviceId, 2);
                mKeyEventDialogManager.dismissNormalDialog();
                mKeyEventDialogManager.setAdCurrVolume();
                CountTimeRunnable.stopTalkingOrMonitoring();
                String tip = BaseApplication.context().getString(
                        R.string.phone_call_error) + AppConfig.NO_ECVOIP_CALL_MANAGER_ERROR;
                BaseApplication.showToast(tip);
            }
        } catch (Exception e) {
            int deviceId = Integer.parseInt(DeviceInfoUtils.getDeviceID(
                    BaseApplication.context()));
            YTXAccountMessage.getVOIPParamRequest(deviceId, 2);
            mKeyEventDialogManager.dismissNormalDialog();
            mKeyEventDialogManager.setAdCurrVolume();
            CountTimeRunnable.stopTalkingOrMonitoring();
            String tip = BaseApplication.context().getString(
                    R.string.phone_call_error) + AppConfig.MAKE_CALL_ERROR;
            BaseApplication.showToast(tip);
            DDLog.i("YTXPlayPhone.clazz--->>> makeCall Exception =" + e);
            e.printStackTrace();
            // =======ytx makeCall end==========
        }
    }

    private class QuerySubAccountAsyncTask extends
            AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            HashMap<String, Object> result = mRestAPI
                    .querySubAccount(mSubAccount);
            if (SUCCESS_CODE.equals(result.get("statusCode"))) {
                // 正常返回输出data包体信息（map）
                DDLog.i("YTXPlayPhone.clazz--->>> QuerySubAccountAsyncTask result :" + result);
                @SuppressWarnings("unchecked")
                HashMap<String, Object> data = (HashMap<String, Object>) result.get("data");
                if (data == null) {
                    DDLog.i("YTXPlayPhone.clazz--->>>QuerySubAccountAsyncTask create account start .....");
                    return -1;
                }
                Set<String> keySet = data.keySet();
                for (String key : keySet) {
                    Object object = data.get(key);
                    if (key.equals("SubAccount")) {
                        @SuppressWarnings("unchecked")
                        ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) object;
                        HashMap<String, String> map = (HashMap<String, String>) list.get(0);
                        String voipAccount = map.get("voipAccount");
                        String voipPwd = map.get("voipPwd");
                        DDLog.i("YTXPlayPhone.clazz--->>> QuerySubAccountAsyncTask voipAccount:"
                                + voipAccount + ";voipPwd :" + voipPwd);

                        SPUtils.setParam(BaseApplication.context(),
                                SPUtils.DD_CONFIG_SHARE_PREF_NAME, "voipAccount", voipAccount);
                        SPUtils.setParam(BaseApplication.context(),
                                SPUtils.DD_CONFIG_SHARE_PREF_NAME, "voipPwd", voipPwd);
                        return 1;
                    }
                }
            } else {
                // 异常返回输出错误码和错误信息
                DDLog.i("YTXPlayPhone.clazz--->>>QuerySubAccountAsyncTask......... error code ="
                        + result.get("statusCode") + "; error info = " + result.get("statusMsg"));
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == -1) {
                createSubAccount();
            } else if (result == 1) {
                initSDKAndLoginYTX();
            }
        }

        private void createSubAccount() {
            new CreateSubAccountAsyncTask().execute("");
        }

    }

    private class CreateSubAccountAsyncTask extends
            AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {

            HashMap<String, Object> result = mRestAPI
                    .createSubAccount(mSubAccount);
            if (SUCCESS_CODE.equals(result.get("statusCode"))) {
                // 正常返回输出data包体信息（map）
                @SuppressWarnings("unchecked")
                HashMap<String, Object> data = (HashMap<String, Object>) result.get("data");
                DDLog.i("YTXPlayPhone.clazz--->>>createSubAccount result : " + result);
                if (data == null) {
                    DDLog.i("YTXPlayPhone.clazz--->>>createSubAccount failed!!!");
                    return -1;
                }
                Set<String> keySet = data.keySet();
                for (String key : keySet) {
                    Object object = data.get(key);
                    if (key.equals("SubAccount")) {
                        @SuppressWarnings("unchecked")
                        ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) object;
                        HashMap<String, String> subInfoMap = (HashMap<String, String>) list.get(0);
                        String voIpAccount = subInfoMap.get("voIpAccount");
                        String voIpPwd = subInfoMap.get("voIpPwd");

                        SPUtils.setParam(BaseApplication.context(),
                                SPUtils.DD_CONFIG_SHARE_PREF_NAME, "voIpAccount", voIpAccount);
                        SPUtils.setParam(BaseApplication.context(),
                                SPUtils.DD_CONFIG_SHARE_PREF_NAME, "voIpPwd", voIpPwd);
                        DDLog.i("YTXPlayPhone.clazz--->>>createSubAccount key " + key
                                + ";voIpAccount:" + voIpAccount + ";voIpPwd:" + voIpPwd);
                        return 1;
                    }

                }
            } else {
                // 异常返回输出错误码和错误信息
                int statusCode = (int) result.get("statusCode");
                DDLog.i("YTXPlayPhone.clazz--->>>createSubAccount..... error code ="
                        + statusCode + ",error is " + result.get("statusMsg"));
                if (statusCode == 111106) {
                    BaseApplication.showToast(R.string.please_set_sys_time);
                } else {
                    BaseApplication.showToast(R.string.init_phone_call_error);
                }
            }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == -1) {
                DDLog.i("YTXPlayPhone.clazz--->>>createSubAccount error !!!!");
            } else if (result == 1) {
                initSDKAndLoginYTX();
            }
        }
    }

    // init sdk
    private void initSDKAndLoginYTX() {
        // 第一步：初始化SDK
        // 判断SDK是否已经初始化，如果已经初始化则可以直接调用登陆接口
        // 没有初始化则先进行初始化SDK，然后调用登录接口注册SDK
        if (!initializedYTX()) {
            initSDK();
        }
    }

    private void initSDK() {
        ECDevice.initial(BaseApplication.context(),
                new ECDevice.InitListener() {
                    @Override
                    public void onInitialized() {
                        // SDK已经初始化成功
                        DDLog.i("YTXPlayPhone.clazz--->>>initSDK success!!!!!!!!!!!!! ");
                        // 第二步：设置注册参数、设置通知回调监听
                        // 构建注册所需要的参数信息
                        ECInitParams params = setYTXParams();
                        setYTXListener();
                        // 第三步：验证参数是否正确，注册SDK
                        if (params.validate()) {
                            // 判断注册参数是否正确
                            ECDevice.login(params);
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        // SDK 初始化失败,可能有如下原因造成
                        // 1、可能SDK已经处于初始化状态
                        // 2、SDK所声明必要的权限未在清单文件（AndroidManifest.xml）里配置、
                        // 或者未配置服务属性android:exported="false";
                        // 3、当前手机设备系统版本低于ECSDK所支持的最低版本（当前ECSDK支持
                        // Android Build.VERSION.SDK_INT 以及以上版本）
                        int phoneVersion = Build.VERSION.SDK_INT;
                        DDLog.i("YTXPlayPhone.clazz--->>> initSDK onError!!!!!!!exception:"
                                + exception + ",phoneVersion:" + phoneVersion);
                        exception.printStackTrace();
                    }
                });
    }

    private void setYTXListener() {
        // 获得SDKVoIP呼叫接口
        // 注册VoIP呼叫事件回调监听
        ECVoIPCallManager callInterface = ECDevice.getECVoIPCallManager();
        if (callInterface != null) {
            DDLog.i("YTXPlayPhone.clazz--->>>setYTXListener callInterface.setOnVoIPCallListener.");
            callInterface.setOnVoIPCallListener(onVoIPListener);
        }
    }

    private ECVoIPCallManager.OnVoIPListener onVoIPListener = new ECVoIPCallManager.OnVoIPListener() {

        @Override
        public void onSwitchCallMediaTypeRequest(String arg0, CallType arg1) {
        }

        @Override
        public void onSwitchCallMediaTypeResponse(String arg0, CallType arg1) {
        }

        @Override
        public void onVideoRatioChanged(VideoRatio arg0) {
        }

        @Override
        public void onCallEvents(ECVoIPCallManager.VoIPCall voIpCall) {
            // 处理呼叫事件回调
            if (voIpCall == null) {
                DDLog.i("YTXPlayPhone.clazz--->>>onCallEvents voIpCall is null");
                return;
            }
            // 根据不同的事件通知类型来处理不同的业务
            ECVoIPCallManager.ECCallState callState = voIpCall.callState;
            switch (callState) {
                case ECCALL_PROCEEDING:
                    // 正在连接服务器处理呼叫请求
                    DDLog.i("YTXPlayPhone.clazz--->>>onCallEvents  ECCALL_PROCEEDING....");
                    BaseApplication.showToast(R.string.phone_calling);
                    SocketThreadManager.startSocketThread(
                            new PhoneCallSocketRunnable(mRoomNum, mPhoneNum, 0,
                                    APlatData.PHONE_CALL_PROCEEDING), "ytx_proceeding");
                    break;
                case ECCALL_ALERTING:
                    // 呼叫到达对方客户端，对方正在振铃
                    DDLog.i("YTXPlayPhone.clazz--->>>onCallEvents  ECCALL_ALERTING....");
                    BaseApplication.showToast(R.string.other_side_ding);
                    SocketThreadManager.startSocketThread(
                            new PhoneCallSocketRunnable(mRoomNum, mPhoneNum, 0,
                                    APlatData.PHONE_CALL_ALERTING), "ytx_alerting");
                    break;
                case ECCALL_ANSWERED:
                    // 对方接听本次呼叫
                    DDLog.i("YTXPlayPhone.clazz--->>>onCallEvents  ECCALL_ANSWERED....");
                    CountTimeRunnable.startTalkingOrMonitoring(
                            AppConfig.MAX_TALKING_OR_MONITORING_TIME, AppConfig.DIALOG_TEXT_NORMAL);
                    mKeyEventDialogManager.showNormalDialog(R.mipmap.talking);
                    SocketThreadManager.startSocketThread(
                            new PhoneCallSocketRunnable(mRoomNum, mPhoneNum, 0,
                                    APlatData.PHONE_CALL_ANSWERED), "ytx_answered");
                    break;
                case ECCALL_RELEASED:
                    // 通话释放[完成一次呼叫]
                    mCurrentCallId = "";
                    DDLog.i("YTXPlayPhone.clazz--->>>onCallEvents  ECCALL_RELEASED....");
                    CountTimeRunnable.stopTalkingOrMonitoring();
                    BaseApplication.showToast(R.string.phone_call_end);
                    mKeyEventDialogManager.dismissNormalDialog();
                    mKeyEventDialogManager.setAdCurrVolume();
                    SocketThreadManager.startSocketThread(new PhoneCallSocketRunnable(mRoomNum,
                            mPhoneNum, 0, APlatData.PHONE_CALL_RELEASED), "ytx_released");
                    break;
                case ECCALL_FAILED:
                    // 本次呼叫失败，根据失败原因播放提示音
                    DDLog.i("YTXPlayPhone.clazz--->>>onCallEvents  ECCALL_FAILED..........."
                            + voIpCall.reason + ";callId:" + voIpCall.callId);
                    mCurrentCallId = "";
                    CountTimeRunnable.stopTalkingOrMonitoring();
                    String tip = BaseApplication.context().
                            getString(R.string.phone_call_failed) + voIpCall.reason;
                    BaseApplication.showToast(tip);
                    mKeyEventDialogManager.dismissNormalDialog();
                    mKeyEventDialogManager.setAdCurrVolume();
                    SocketThreadManager.startSocketThread(
                            new PhoneCallSocketRunnable(mRoomNum, mPhoneNum, 0,
                                    APlatData.PHONE_CALL_FAILED), "ytx_failed");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onMediaDestinationChanged(VoipMediaChangedInfo voipMediaChangedInfo) {
        }

        private long lastTime;

        @Override
        public void onDtmfReceived(String arg0, char number) {
            DDLog.i("YTXPlayPhone.clazz--->>>onDtmfReceived , arg0 " + arg0
                    + "; number:" + number);
            if (number == '#') {
                long abs = Math.abs(System.currentTimeMillis() - lastTime);
                if (abs > 2000) {
                    ((Launcher) mContext).getKeyboardEvents().unlockRequest(0,
                            AppConfig.UNLOCK_TYPE_CALL, mPhoneNum, mRoomNum);
                    lastTime = System.currentTimeMillis();
                }
            }
        }
    };

    private ECInitParams setYTXParams() {
        // 5.0.3的SDK初始参数的方法：ECInitParams params = new ECInitParams();
        // 5.1.*以上版本如下：
        ECInitParams params = ECInitParams.createParams();
        // 自定义登录方式：
        // 测试阶段Userid可以填写手机
        // params.setUserid(AppConfig.USER_ID);
        // params.setAppKey(AppConfig.APP_KEY);
        // params.setToken(AppConfig.AUTH_TOKEN);
        // 设置登陆验证模式（是否验证密码）NORMAL_AUTH-自定义方式
        // params.setAuthType(ECInitParams.LoginAuthType.NORMAL_AUTH);
        // 1代表用户名+密码登陆（可以强制上线，踢掉已经在线的设备）
        // 2代表自动重连注册（如果账号已经在其他设备登录则会提示异地登陆）
        // 3 LoginMode（强制上线：FORCE_LOGIN 默认登录：AUTO）
        // params.setDeviceMode(ECInitParams.LoginMode.FORCE_LOGIN);

        // voip账号+voip密码方式：
        String voipAccount = (String) SPUtils.getParam(BaseApplication.context(),
                SPUtils.DD_CONFIG_SHARE_PREF_NAME, "voipAccount", "");
        String voipPwd = (String) SPUtils.getParam(BaseApplication.context(),
                SPUtils.DD_CONFIG_SHARE_PREF_NAME, "voipPwd", "");
        DDLog.i("YTXPlayPhone.clazz--->>>setYTXParams login.... voipAccount:"
                + voipAccount + "; voipPwd:" + voipPwd);
        params.setUserid(voipAccount);
        params.setPwd(voipPwd);
        params.setAppKey(/* AppConfig.APP_KEY */mAccountMessage.getAppID());
        // 设置登陆验证模式（是否验证密码）PASSWORD_AUTH-密码登录方式
        params.setAuthType(ECInitParams.LoginAuthType.PASSWORD_AUTH);
        // 1代表用户名+密码登陆（可以强制上线，踢掉已经在线的设备）
        // 2代表自动重连注册（如果账号已经在其他设备登录则会提示异地登陆）
        // 3 LoginMode（强制上线：FORCE_LOGIN 默认登录：AUTO）
        params.setMode(ECInitParams.LoginMode.FORCE_LOGIN);

        // 如果是v5.1.8r开始版本建议使用
        ECDevice.setOnDeviceConnectListener(new ECDevice.OnECDeviceConnectListener() {

            @Override
            public void onConnect() {
                DDLog.i("YTXPlayPhone.clazz--->>>setYTXParams onConnect!!!");
            }

            @Override
            public void onConnectState(ECConnectState arg0, ECError arg1) {
                DDLog.i("YTXPlayPhone.clazz--->>>setYTXParams onConnectState arg0:"
                        + arg0 + "; arg1:" + arg1);
            }

            @Override
            public void onDisconnect(ECError arg0) {
                DDLog.i("YTXPlayPhone.clazz--->>>setYTXParams onDisconnect arg0:" + arg0);
            }
        });
        return params;
    }

    private static class PhoneCallSocketRunnable implements Runnable {
        private String roomNum = "";
        private String phoneNum = "";
        private int remainCount;
        private int result;

        public PhoneCallSocketRunnable(String roomNum, String phoneNum,
                                       int remainCount, int result) {
            this.roomNum = roomNum;
            this.phoneNum = phoneNum;
            this.remainCount = remainCount;
            this.result = result;
        }

        @Override
        public void run() {
            UdpClientSocket client;
            String serverHost = AppConfig.SERVER_HOST_IP;
            int serverPort = 45611;
            try {
                client = new UdpClientSocket();
                DSPacket packet = new DSPacket();
                byte[] callPkt = packet.phoneCallResult(0, roomNum,
                        phoneNum, result);
                if (callPkt == null) {
                    return;
                }
                client.send(serverHost, serverPort, callPkt, callPkt.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}