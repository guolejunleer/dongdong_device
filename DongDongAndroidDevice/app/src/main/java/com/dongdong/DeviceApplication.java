package com.dongdong;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

import com.dongdong.api.ApiHttpClient;
import com.dongdong.base.BaseApplication;
import com.dongdong.bean.LocalCardBean;
import com.dongdong.bean.RoomInfoBean;
import com.dongdong.db.LocalCardOpe;
import com.dongdong.db.DBManager;
import com.dongdong.db.RoomCardOpe;
import com.dongdong.db.entry.CardBean;
import com.dongdong.db.entry.RoomCardBean;
import com.dongdong.prompt.KeyEventManager;
import com.dongdong.socket.normal.APlatData;
import com.dongdong.utils.DDLog;
import com.jr.door.R;
import com.jr.gs.JRService;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 应用全局Application,注册按键广播，接收按键事件
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class DeviceApplication extends BaseApplication {

    public static CopyOnWriteArraySet<Integer> mRoomIDSet = new CopyOnWriteArraySet();
    public static CopyOnWriteArrayList<RoomInfoBean> mVerifyRoomList = new CopyOnWriteArrayList();

    public static boolean isSystemSettingStatus = false;// true:后台系统状态,false:前台应用状态
    public static boolean isCallStatus = false;// 是否在呼叫状态
    public static boolean isYTXPhoneCall = false;// 是否云通讯在打电话状态
    public static int mNetStateCount = 0;//检测android板和linux网卡状态

    /**
     * 设备状态，正常:无任何操作，工作:拨打电话等
     */
    public static final int DEVICE_FREE = 0;
    public static final int DEVICE_WORKING = 1;
    public static int DEVICE_WORKING_STATUS = DEVICE_FREE;

    /**
     * 设备模式，0:围墙机,1:单元机
     */
    public static final int DEVICE_MODE_WALL = 0;
    public static final int DEVICE_MODE_UNIT = 1;

    public static String mVersion;
    public static int mRegStatus = 0;

    private KeyboardEventsBroadcastReceiver mKeyEventsReceiver;
    private static List<OnKeyboardEventsChangeListener> mListeners = new ArrayList<>();

    public interface OnKeyboardEventsChangeListener {

        void onUpdateNumberView(String number);

        void onLocalCardUnlock(int unlockType, String cardNum);

        void onSendCardUnlock(String cardNum);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, JRService.class));
        IntentFilter intentFilter = new IntentFilter("com.jr.gs.server");
        mKeyEventsReceiver = new KeyboardEventsBroadcastReceiver();
        registerReceiver(mKeyEventsReceiver, intentFilter);
        DBManager.getInstance(this);
        init();
        DDLog.i("DeviceApplication.clazz--->>>onCreate!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    private void init() {
        // 初始化网络请求
        AsyncHttpClient client = new AsyncHttpClient();
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        client.setCookieStore(myCookieStore);
        ApiHttpClient.setHttpClient(client);
        // ApiHttpClient.setCookie(ApiHttpClient.getCookie(this));

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //unregisterReceiver(mKeyEventsReceiver);
    }

    public synchronized void addOnKeyboardEventsChangeListener(
            OnKeyboardEventsChangeListener listener) {
        if (!mListeners.contains(listener))
            mListeners.add(listener);
    }

    public synchronized void removeOnKeyboardEventsChangeListener(
            OnKeyboardEventsChangeListener listener) {
        if (mListeners.contains(listener))
            mListeners.remove(listener);
    }

    private static class KeyboardEventsBroadcastReceiver extends
            BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.jr.gs.server".equals(action)) {
                Bundle myBundle = intent.getExtras();
                if (intent.hasExtra("key")) {
                    // 按键值
                    String key = myBundle.getString("key");
                    String number = KeyEventManager.getNumber(key);
                    APlatData.debugLog("DeviceApplication.clazz number:" + number
                            + ",DEVICE_WORKING_STATUS:" + DEVICE_WORKING_STATUS);
                    if (TextUtils.isEmpty(number) || (!number.equals("*")
                            && DEVICE_WORKING_STATUS == DEVICE_WORKING)) {
                        //工作状态要屏蔽按键
                        return;
                    }
                    for (int i = 0; i < mListeners.size(); i++) {
                        mListeners.get(i).onUpdateNumberView(number);
                        //mListeners.get(i).onChangKeyValue(number);
                    }
                } else if (intent.hasExtra(AppConfig.BUNDLE_CARD_NUM_KEY)) {
                    // 读到卡号
                    String cardNum = myBundle.getString(AppConfig.BUNDLE_CARD_NUM_KEY, "")
                            .toLowerCase();// 转小写
                    DDLog.i("DeviceApplication.clazz----------->>>cardNum:" + cardNum);
                    if (isEffective()) {// 两次刷卡大于1秒间隔才算有效刷卡
                        if (LocalCardBean.getInstance().isSettingStatus()) {
                            LocalCardBean.getInstance().findCard(cardNum);//注册卡号
                            APlatData.debugLog("DeviceApplication.clazz -->>> findCard");
                            return;
                        }
                        try {
                            List<CardBean> cardBeans = LocalCardOpe.queryDatasByCardNum(
                                    BaseApplication.context(), cardNum);
                            List<RoomCardBean> roomCardBeans = RoomCardOpe.queryDataByCardNum(
                                    BaseApplication.context(), cardNum);
                            APlatData.debugLog("DeviceApplication.clazz ---->>>cardBean :" + cardBeans);
                            if (cardBeans != null) {
                                for (int i = 0; i < mListeners.size(); i++) {
                                    // 本地数据库直接开门
                                    mListeners.get(i).onLocalCardUnlock(
                                            AppConfig.UNLOCK_TYPE_LOCAL_CARD, cardNum);
                                    APlatData.debugLog("DeviceApplication.clazz --local>>>cardNum :" + cardNum);
                                }
                            }
                           else if (roomCardBeans != null) {
                                for (int i = 0; i < mListeners.size(); i++) {
                                    // 本地数据库直接开门
                                    mListeners.get(i).onLocalCardUnlock(
                                            AppConfig.UNLOCK_TYPE_PLATFORM_CARD, cardNum);
                                    APlatData.debugLog("DeviceApplication.clazz --local>>>cardNum :" + cardNum);
                                }
                            } else {
                                for (int i = 0; i < mListeners.size(); i++) {
                                    // 平台去查询
                                    mListeners.get(i).onSendCardUnlock(cardNum);
                                    APlatData.debugLog("DeviceApplication.clazz --net>>>cardNum :" + cardNum);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            APlatData.debugLog("DeviceApplication.clazz Exception :" + e);
                            String tip = BaseApplication.context().getString(R.string.error_tip)
                                    + AppConfig.QUERY_CARD_ERROR;
                            BaseApplication.showToast(tip);
                        }
                    }
                }
            }
        }
    }

    private static long oldCardTime = 0;

    private static boolean isEffective() {
        long newCardTime = System.currentTimeMillis();
        if (Math.abs(newCardTime - oldCardTime) > 800) {
            oldCardTime = newCardTime;
            return true;
        }
        return false;
    }
}
