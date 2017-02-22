package com.dongdong.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.cloopen.rest.sdk.utils.LoggerUtil;
import com.dongdong.AppConfig;
import com.dongdong.DeviceApplication;
import com.dongdong.base.BaseApplication;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public class DeviceInfoUtils {

    public static float mDisplayDensity = 0.0F;
    private static Boolean mIsTablet;

    private DeviceInfoUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 设置设备ID
     *
     * @param context  上下文
     * @param deviceID 设备ID
     */
    public static void setDeviceID(Context context, String deviceID) {
        SPUtils.setParam(context, SPUtils.DD_CONFIG_SHARE_PREF_NAME,
                SPUtils.SP_KEY_DEVICE_ID, deviceID);
    }

    /**
     * 得到设备ID
     *
     * @param context 上下文
     * @return 设备ID
     */
    public static String getDeviceID(Context context) {
        String devieId = (String) SPUtils.getParam(context,
                SPUtils.DD_CONFIG_SHARE_PREF_NAME, SPUtils.SP_KEY_DEVICE_ID, "");
        DDLog.i("DeviceInfoUtils.clazz--->>>getDeviceID devieId:"
                + devieId);
        if (TextUtils.isEmpty(devieId)) {
            if (SDCardUtils.isSDCardEnable()) {
                try {
                    devieId = SDCardUtils.readData4SDCard(new File(SDCardUtils
                            .createDirOnSDCard(AppConfig.SDCARD_FILE),
                            AppConfig.SDCARD_FILE_NAME).getAbsolutePath()).split("#")[0];
                    DDLog.i("DeviceInfoUtils.clazz--->>> 222 getDeviceID devieId:"
                            + devieId);
                    SPUtils.setParam(context, SPUtils.DD_CONFIG_SHARE_PREF_NAME,
                            SPUtils.SP_KEY_DEVICE_ID, devieId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return devieId;
    }

    public static void setDeviceMode(Context context, int mode) {// 设置机器类型 门口机---围墙机
        SPUtils.setParam(context, SPUtils.DD_CONFIG_SHARE_PREF_NAME,
                SPUtils.SP_KEY_DEVICE_MODE, mode);
    }

    public static int getDeviceMode(Context context) {// 得到机器类型 门口机---围墙机
        int result = (int) SPUtils.getParam(context, SPUtils.DD_CONFIG_SHARE_PREF_NAME,
                SPUtils.SP_KEY_DEVICE_MODE, -1);
        if (result == -1) {
            if (TextUtils.isEmpty(getDeviceID(context)) || "0".equals(getDeviceID(context))) {
                result = DeviceApplication.DEVICE_MODE_UNIT;
            } else {
                if (SDCardUtils.isSDCardEnable()) {
                    try {
                        String deviceMode = SDCardUtils.readData4SDCard(new File(SDCardUtils
                                .createDirOnSDCard(AppConfig.SDCARD_FILE),
                                AppConfig.SDCARD_FILE_NAME).getAbsolutePath());
                        DDLog.i("DeviceInfoUtils.clazz--->>>getDeviceMode deviceMode:"
                                + deviceMode);
                        if (TextUtils.isEmpty(deviceMode)) {
                            result = DeviceApplication.DEVICE_MODE_UNIT;
                        } else {
                            result = Integer.parseInt(deviceMode.split("#")[1]);
                            SPUtils.setParam(context, SPUtils.DD_CONFIG_SHARE_PREF_NAME,
                                    SPUtils.SP_KEY_DEVICE_MODE, result);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static int getVersionCode() {
        int versionCode = 0;
        try {
            versionCode = BaseApplication.context().getPackageManager()
                    .getPackageInfo(BaseApplication.context().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException ex) {
            versionCode = 0;
        }
        return versionCode;
    }

    public static int getVersionCode(String packageName) {
        int versionCode = 0;
        try {
            versionCode = BaseApplication.context().getPackageManager()
                    .getPackageInfo(packageName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException ex) {
            versionCode = 0;
        }
        return versionCode;
    }

    public static float dpToPixel(float dp) {
        return dp * (getDisplayMetrics().densityDpi / 160F);
    }

    /**
     * dp转px
     *
     * @param context
     * @param dpVal
     * @return
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    /**
     * sp转px
     *
     * @param context
     * @param spVal
     * @return
     */
    public static int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxVal
     * @return
     */
    public static float px2dp(Context context, float pxVal) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }

    /**
     * px转sp
     *
     * @param context
     * @param pxVal
     * @return
     */
    public static float px2sp(Context context, float pxVal) {
        return (pxVal / context.getResources().getDisplayMetrics().scaledDensity);
    }


    /**
     * 决断是否为平板
     *
     * @return
     */
    public static boolean isTablet() {
        if (mIsTablet == null) {
            boolean flag;
            if ((0xf & BaseApplication.context().getResources().getConfiguration().screenLayout) >= 3)
                flag = true;
            else
                flag = false;
            mIsTablet = Boolean.valueOf(flag);
        }
        return mIsTablet.booleanValue();
    }

    /**
     * 获取屏幕密度
     *
     * @return
     */
    public static float getDensity() {
        if (mDisplayDensity == 0.0)
            mDisplayDensity = getDisplayMetrics().density;
        return mDisplayDensity;
    }

    /**
     * 获取显示规格
     *
     * @return
     */
    public static DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((WindowManager) BaseApplication.context().getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics;
    }

    public static float getScreenHeight() {
        return getDisplayMetrics().heightPixels;
    }

    public static float getScreenWidth() {
        return getDisplayMetrics().widthPixels;
    }

    public static void installAPK(Context context, File file) {
        if (file == null || !file.exists())
            return;
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static void hideSoftKeyboard(View view) {
        if (view == null)
            return;
        ((InputMethodManager) BaseApplication.context().getSystemService(
                Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                view.getWindowToken(), 0);
    }

    public static int getStatusBarHeight(Context context) {
        Class<?> clazz;

        Object obj;

        Field field;

        int x, sBar = 0;

        try {
            clazz = Class.forName("com.android.internal.R$dimen");
            obj = clazz.newInstance();
            field = clazz.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sBar = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return sBar;
    }

    public static boolean isInMainThread() {
        Looper myLooper = Looper.myLooper();
        Looper mainLooper = Looper.getMainLooper();
        return myLooper == mainLooper;
    }

}
