package com.dongdong.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.dongdong.ui.dialog.CommonDialog;
import com.jr.door.R;

/**
 * 跟网络相关的工具类
 */
public class NetUtils {
    private NetUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 判断网络是否连接
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {

        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null != connectivity) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (null != info && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否是wifi连接
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;

    }

    /**
     * 打开网络设置界面
     */
    public static void openSetting(Activity activity) {
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings",
                "com.android.settings.WirelessSettings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        activity.startActivityForResult(intent, 0);
    }

    public static void withoutNetworkAlert(final Activity activity) {
        CommonDialog commonDialog = new CommonDialog(activity);
        commonDialog.setTitle(R.string.tip_title);
        commonDialog.setMessage(R.string.net_tip_content);
        commonDialog.setPositiveButton(R.string.button_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                dialog.dismiss();
            }
        });
        commonDialog.setNegativeButton(R.string.cancel, null);
        commonDialog.setCancelable(true);
        commonDialog.show();
    }

    public static void withoutDeviceIdAlert(final Activity activity) {
        CommonDialog commonDialog = new CommonDialog(activity);
        commonDialog.setTitle(R.string.tip_title);
        commonDialog.setMessage(R.string.device_num_no_function);
        commonDialog.setPositiveButton(R.string.button_sure,null);
        commonDialog.setNegativeButton(R.string.cancel, null);
        commonDialog.setCancelable(false);
        commonDialog.show();
    }
}
