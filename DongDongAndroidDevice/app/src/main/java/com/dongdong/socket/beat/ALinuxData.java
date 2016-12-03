/**
 * 常量定义
 */
package com.dongdong.socket.beat;

import com.dongdong.utils.DDLog;

public class ALinuxData {
    /**
     * 设备摄像头Id
     */
    public static int mCameraId = 0;

    /**
     * 回调指针
     */
    public static PeerAddressCallback mSink;

    /**
     * 版本号
     */
    public final static String PKT_VERSION = "1.0.0";

    /**
     * 选择打印开关
     */
    public final static boolean DEBUGLOG_SWITCH = false;

    /**
     * 错误码
     */
    public final static int RESULT_SUCCESS = 0;
    public final static int RESULT_FAILED = 1;

    /**
     * 组播地址端口
     */
    public final static String MULTICAST_ADDR = "236.6.8.3";
    public final static int MULTICAST_PORT = 9525;
    // final static String MULTICAST_ADDR = "238.9.9.1";
    // final static int MULTICAST_PORT = 8302;

    /**
     * 单播端口
     */
    public final static int UNICAST_PORT = 9526;

    /**
     * 协议头长度
     */
    public final static int PACKET_HEADER_LENGTH = 20;

    /**
     * 版本号长度
     */
    public final static int VERSION_LENGTH = 8;

    /**
     * 协议命令id
     */
    public final static short CMD_SCAN_REQUEST = 0x0001;
    public final static short CMD_SCAN_RESPONSE = 0x0002;
    public final static short CMD_KEEPALIVE_REQUEST = 0x0003;
    public final static short CMD_KEEPALIVE_RESPONSE = 0x0004;
    public final static short CMD_GETPARAM_REQUEST = 0x0005;
    public final static short CMD_GETPARAM_RESPONSE = 0x0006;

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
