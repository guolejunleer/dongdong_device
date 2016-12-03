package com.dongdong.socket.normal;

import com.dongdong.utils.DDLog;

/**
 * DeviceService常量定义
 */
public class APlatData {
    /**
     * 选择打印开关
     */
    public final static boolean DEBUGLOG_SWITCH = false;

    /**
     * 错误码
     */
    public final static int RESULT_SUCCESS = 0; // 成功
    public final static int RESULT_FAILED = 1; // 失败
    public final static int RESULT_NO_ROOM = 2;// 无房号
    public final static int RESULT_NO_USER = 3;// 无住户
    public final static int RESULT_NO_OFFLINE = 4;// 设备离线
    public final static int RESULT_ONLY_PHONE = 5;//设备在线，用户之前登录过，现处于注销状态，只能拨打电话

    public final static int RESULT_ERR_IP = 0x00000201; // IP无效
    public final static int RESULT_ERR_MASK = 0x00000202; // 掩码无效
    public final static int RESULT_ERR_GW = 0x00000203; // 网关无效

    /**
     * 网络类型
     */
    public final static int NETTYPE_WIRED = 1; // 使用有线网络
    public final static int NETTYPE_WIFI = 2; // 使用Wifi网络
    public final static int NETTYPE_3G = 4; // 使用3G网络
    public final static int NETTYPE_4G = 8; // 使用4G网络

    /**
     * 开门记录数据类型，时时的为用户主动开门，本地数据为用户之前开门记录
     */
    public final static int UNLOCK_LOCAL_DATA = 0;
    public final static int UNLOCK_TIME_DATA = 1;


    /**
     * 网口地址获取类型
     */
    public final static int NETADDRTYPE_DHCP = 1; // 动态获取
    public final static int NETADDRTYPE_STATIC = 2; // 静态配置

    /**
     * 设备状态
     */
    public final static int STATUS_ORIGINAL = 0; // 初始状态
    public final static int STATUS_CALLING = 1; // 呼叫中
    public final static int STATUS_CALL_SUCCESS = 2; // 呼叫成功后，请通话
    public final static int STATUS_CALL_END = 3; // 通话结束
    public final static int STATUS_MONITORING = 4; // 监视中
    public final static int STATUS_MONITOR_END = 5; // 监视结束

    /**
     * 播放类型
     */
    public final static int MEDIAMODE_VIDEO = 0x00000001; // 视频数据
    public final static int MEDIAMODE_AUDIO_CAPTURE = 0x00000002; // 音频输入
    public final static int MEDIAMODE_AUDIO_PLAY = 0x00000004; // 音频播放

    /**
     * 拨打电话状态码,协议有5种，这里写了4种
     */
    public final static int PHONE_CALL_PROCEEDING = 0;
    public final static int PHONE_CALL_ALERTING = 1;
    public final static int PHONE_CALL_ANSWERED = 2;
    public final static int PHONE_CALL_RELEASED = 3;
    public final static int PHONE_CALL_FAILED = 4;

    /**
     * 音视频分包最大长度
     */
    final static int MAX_MEDIAREQUEST_LENGTH = 4000;

    /**
     * 协议头长度
     */
    final static int PACKET_HEADER_LENGTH = 20;

    /**
     * 音频包gsm解码后长度
     */
    final static int AUDIO_DECODE_LENGTH = 320;

    /**
     * 房号长度
     */
    public final static int ROOM_NUMBER_LENGTH = 12;

    /**
     * 手机号码长度
     */
    public final static int PHONE_NUMBER_LENGTH = 32;

    /**
     * 卡号长度
     */
    public final static int CARD_NUMBER_LENGTH = 32;

    /**
     * 开门密码长度
     */
    public final static int UNLOCK_PWD_LENGTH = 12;

    /**
     * 平台时间长度
     */
    public final static int PLATFORM_TIME_LENGTH = 14;

    /**
     * MAC地址长度
     */
    public final static int MAC_ADDRESS_LENGTH = 6;

    /**
     * 平台访问密钥长度
     */
    public final static int PLATFORM_KEY_LENGTH = 17;

    /**
     * 协议命令id
     */
    final static short CMD_PLAY_REQUEST = 0x01;
    final static short CMD_PLAY_RESULT = 0x02;
    final static short CMD_STOP_REQUEST = 0x03;
    final static short CMD_STOP_RESULT = 0x04;
    final static short CMD_SENDMEDIA_REQUEST = 0x05;
    final static short CMD_SENDMEDIA_REQUEST_EX = 0x1d;
    final static short CMD_SENDMEDIA_RESULT = 0x06;
    final static short CMD_SETVOLUME_REQUEST = 0x07;
    final static short CMD_SETVIDEOMODE_REQUEST = 0x09;
    final static short CMD_SETVIDEOATTR_REQUEST = 0x0B;
    final static short CMD_UNLOCK_REQUEST = 0x0D;
    final static short CMD_GETNET_REQUEST = 0x10;
    final static short CMD_GETNET_RESULT = 0x11;
    final static short CMD_SETNET_REQUEST = 0x12;
    final static short CMD_SETNET_RESULT = 0x13;
    final static short CMD_HANGUP_REQUEST = 0x14;
    final static short CMD_DELCARD_REQUEST = 0x16;
    final static short CMD_TUNNEL_PUSH_REQUEST = 0x18;
    final static short CMD_TUNNEL_PUSH_RESULT = 0x19;
    final static short CMD_TUNNEL_CMD_REQUEST = 0x1A;
    final static short CMD_TUNNEL_CMD_RESULT = 0x1B;
    final static short CMD_UPLOAD_REQUEST = 0x1C;
    final static short CMD_CALL_REQUEST = 0xF1;
    final static short CMD_CALL_RESULT = 0xF2;
    final static short CMD_CARD_REQUEST = 0xF3;
    final static short CMD_CARD_RESULT = 0xF4;
    final static short CMD_PWD_REQUEST = 0xF5;
    final static short CMD_PWD_RESULT = 0xF6;

    final static short CMD_PHONECALL_REQUEST = 0xF7;
    final static short CMD_PHONECALL_RESULT = 0xF8;
    final static short CMD_STOPPHONECALL_REQUEST = 0xF9;

    final static short CMD_DISABLE_PHONE_CALL_REQUEST = 0xFA;// 新增电话欠费协议

    final static short CMD_UNLOCKTYPE_REQUEST = 0xFD;//开门记录协议
    final static short CMD_UNLOCKTYPE_RESULT = 0xFE;
    //同步时间协议
    final static short CMD_GET_TIMESTAMP_REQUEST = 0x0105;
    final static short CMD_GET_TIMESTAMP_RESULT = 0x0106;
    //获取房号卡号信息协议
    final static short CMD_GET_ROOMCARD_INFO_REQUEST = 0x107;
    final static short CMD_GET_ROOMCARD_INFO_RESULT = 0x108;

    /**
     * Debug打印
     */
    public static void debugLog(String debuglog) {
        if (DDLog.isDebug) {
            DDLog.i(debuglog);
        } else {
            System.out.println(debuglog);
        }
    }
}
