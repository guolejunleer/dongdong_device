package com.dongdong;

import android.os.Environment;

import java.io.File;

/**
 * 应用程序配置类
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class AppConfig {

    // 默认存放文件下载的路径
    public final static String DEFAULT_SAVE_FILE_PATH = Environment
            .getExternalStorageDirectory() + File.separator + "DongDongDevice"
            + File.separator + "download" + File.separator;

    public static final String SDCARD_FILE = "DongDong";
    public static final String SDCARD_FILE_NAME = "device.txt";

    public static final String BUNDLE_CARD_NUM_KEY = "cardNum";
    public static final String BUNDLE_SUPER_ADMIN_KEY = "super_admin_key";
    public static final String UNIT_PWD_ENTER_KEY = "9999";
    public static final String WALL_PWD_ENTER_KEY = "99999999";
    public static final String ADMIN_PWD = "8888";
    public static final String SUPER_ADMIN_PWD = "9779";

    public static String SERVER_HOST_IP = "192.168.68.6";

    /**
     * 通话或者监视最长时间，单位秒
     */
    public static final int MAX_TALKING_OR_MONITORING_TIME = 55;

    /**
     * 通话或者监视最长时间，单位秒
     */
    public static final int MAX_QUERY_TIME = 30;

    /**
     * 一次上传开门记录最大条数
     */
    public static final int MAX_UPLOAD_UNLOCK_COUNT = 20;

    /**
     * 一次上传开门记录最大条数
     */
    public static final int MAX_CHECK_ROOM_INFO_COUNT = 20;

    /**
     * 开门记录的开门类型
     */
    public final static int UNLOCK_TYPE_APP = 1;// 手机app点击开门
    public final static int UNLOCK_TYPE_LOCAL_CARD = 2;// 本地查询卡号开门
    public final static int UNLOCK_TYPE_WIFI = 3;// wifi开门
    public final static int UNLOCK_TYPE_TEMP_PASSWORD = 4;// 设备密码开门
    public final static int UNLOCK_TYPE_PASSWORD = 5;// 设备密码开门
    public final static int UNLOCK_TYPE_PLATFORM_CARD = 6;// 平台查询卡号开门
    public final static int UNLOCK_TYPE_BLUETOOTH = 7;// 蓝牙开门
    public final static int UNLOCK_TYPE_CALL = 10;// 手机app接通访客来电后按拨号键开门

    /**
     * 设备拨打电话的平台类型
     */
    public final static int DAIL_TYPE_YUNZHIXUN = 1;
    public final static int DAIL_TYPE_YUNTONGXUN = 2;

    /**
     * 界面对话框文字显示类型
     */
    public final static int DIALOG_TEXT_NORMAL = 0;
    public final static int DIALOG_TEXT_DIAL = 1;

    //拨打电话厂商配置文件
    public static final String USER_ID = "lsm0506@126.com";
    public static final String ACCOUNT_SID = "8aaf07085581a83101558242f5ab0115";
    public static final String AUTH_TOKEN = "886cbdfb55bb409eb2ab443b7da78529";
    public static final String APP_KEY = "8a216da85582647801558fea32e60521";


    /**
     * 错误码
     */
    public static final int NO_ECVOIP_CALL_MANAGER_ERROR = 10000;//拨打电话manager为null
    public static final int MAKE_CALL_ERROR = 10001;//拨打电话异常
    public static final int PARAMS_NO_EFFECT_ERROR = 10002;//拨打电话参数无效
    public static final int QUERY_CARD_ERROR = 10003;//查询卡号失败
    public static final int PLAY_VIDEO_ERROR = 10004;//播放视频错误
    public static final int CALL_ROOM_UNCOMMINU_ERROR = 10005;//无法通信，呼叫房号失败
    public static final int CALL_ROOM_NO_DATA_ERROR = 10006;//无法通信，呼叫房号失败
}
