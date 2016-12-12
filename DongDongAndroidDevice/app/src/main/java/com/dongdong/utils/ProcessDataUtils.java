package com.dongdong.utils;


import android.content.Context;

import com.jr.door.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ProcessDataUtils {

    private final static ThreadLocal<SimpleDateFormat> mDateFormat = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            //yyyy-MM-dd HH:mm:ss
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }
    };

    public static String getUnlockNameByType(int type, Context context) {
        String typename = null;
        switch (type) {
            case 1:
                typename = context.getString(R.string.App);
                break;
            case 2:
                typename = context.getString(R.string.card_local);
                break;
            case 3:
                typename = context.getString(R.string.WIFI);
                break;
            case 4:
                typename = context.getString(R.string.Temporary_Password);
                break;
            case 5:
                typename = context.getString(R.string.Household_Password);
                break;
            case 6:
                typename = context.getString(R.string.card_cloud);
                break;
            case 7:
                typename = context.getString(R.string.bluetooth);
                break;
            case 10:
                typename = context.getString(R.string.phone);
                break;
            default:
                break;
        }
        return typename;
    }


    public static int getUnlockTypeByName(String type, Context context) {
        int unLockType = 0;
        if (type.equals(context.getString(R.string.App))) {
            unLockType = 1;
        }
        if (type.equals(context.getString(R.string.card_local))) {
            unLockType = 2;
        }
        if (type.equals(context.getString(R.string.WIFI))) {
            unLockType = 3;
        }
        if (type.equals(context.getString(R.string.Temporary_Password))) {
            unLockType = 4;
        }
        if (type.equals(context.getString(R.string.Household_Password))) {
            unLockType = 5;
        }
        if (type.equals(context.getString(R.string.card_cloud))) {
            unLockType = 6;
        }
        if (type.equals(context.getString(R.string.bluetooth))) {
            unLockType = 7;
        }
        if (type.equals(context.getString(R.string.phone))) {
            unLockType = 10;
        }
        return unLockType;
    }

    public static int getUnlockState(String upLoad, Context context) {
        int unLockUpLoad;
        if (upLoad.equals(context.getString(R.string.is_upload))) {
            unLockUpLoad = 0;
        } else if (upLoad.equals(context.getString(R.string.is_not_upload))) {
            unLockUpLoad = 1;
        } else {
            unLockUpLoad = 2;
        }
        return unLockUpLoad;
    }

    public static String getUnLockTime(int time) {
        return mDateFormat.get().format(TimeZoneUtil.transformTime(new Date(time * 1000L),
                TimeZone.getTimeZone("GMT"), TimeZone.getTimeZone("GMT+08")));
    }
}
