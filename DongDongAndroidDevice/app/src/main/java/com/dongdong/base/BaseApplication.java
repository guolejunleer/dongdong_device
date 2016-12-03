package com.dongdong.base;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jr.door.R;

/**
 * 应用全局Application的基类，显示自定义吐司也在这
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class BaseApplication extends Application {
    static Context mContext;
    static Resources mResource;
    private static String mLastToast = "";
    private static long mLastToastTime;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mResource = mContext.getResources();
    }

    public static synchronized BaseApplication context() {
        return (BaseApplication) mContext;
    }

    public static Resources resources() {
        return mResource;
    }

    public static void showToast(int message) {
        showToast(message, Toast.LENGTH_LONG, 0);
    }

    public static void showToast(String message) {
        showToast(message, Toast.LENGTH_LONG, 0, Gravity.BOTTOM);
    }

    public static void showToast(int message, int icon) {
        showToast(message, Toast.LENGTH_LONG, icon);
    }

    public static void showToast(String message, int icon) {
        showToast(message, Toast.LENGTH_LONG, icon, Gravity.BOTTOM);
    }

    public static void showToastShort(int message) {
        showToast(message, Toast.LENGTH_SHORT, 0);
    }

    public static void showToastShort(String message) {
        showToast(message, Toast.LENGTH_SHORT, 0, Gravity.BOTTOM);
    }

    public static void showToastShort(int message, Object... args) {
        showToast(message, Toast.LENGTH_SHORT, 0, Gravity.BOTTOM, args);
    }

    public static void showToast(int message, int duration, int icon) {
        showToast(message, duration, icon, Gravity.BOTTOM);
    }

    public static void showToast(int message, int duration, int icon,
                                 int gravity) {
        showToast(context().getString(message), duration, icon, gravity);
    }

    public static void showToast(int message, int duration, int icon,
                                 int gravity, Object... args) {
        showToast(context().getString(message, args), duration, icon, gravity);
    }

    public static void showToast(String message, int duration, int icon,
                                 int gravity) {
        if (message != null && !message.equalsIgnoreCase("")) {
            long time = System.currentTimeMillis();
            if (!message.equalsIgnoreCase(mLastToast) || Math.abs(time - mLastToastTime) > 2000) {
                View view = LayoutInflater.from(context()).inflate(R.layout.view_toast, null);
                ((TextView) view.findViewById(R.id.title_tv)).setText(message);
                if (icon != 0) {
                    ImageView iv = ((ImageView) view.findViewById(R.id.icon_iv));
                    iv.setImageResource(icon);
                    iv.setVisibility(View.VISIBLE);
                }
                Toast toast = new Toast(context());
                toast.setView(view);
                if (gravity == Gravity.CENTER) {
                    toast.setGravity(gravity, 0, 0);
                } else {
                    toast.setGravity(gravity, 0, 35);
                }

                toast.setDuration(duration);
                toast.show();
                mLastToast = message;
                mLastToastTime = System.currentTimeMillis();
            }
        }
    }
}
