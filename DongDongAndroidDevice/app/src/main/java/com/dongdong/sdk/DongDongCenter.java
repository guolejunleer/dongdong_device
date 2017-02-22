package com.dongdong.sdk;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.dongdong.AppConfig;
import com.dongdong.api.ApiHttpClient;
import com.dongdong.base.BaseApplication;
import com.dongdong.db.BulletinOpe;
import com.dongdong.db.entry.BulletinBean;
import com.dongdong.interf.LauncherAndBackendSignalCallback;
import com.dongdong.interf.LauncherCallback;
import com.dongdong.prompt.CountTimeRunnable;
import com.dongdong.socket.normal.APlatData;
import com.dongdong.socket.normal.DSPacket;
import com.dongdong.socket.normal.DSParse;
import com.dongdong.socket.normal.InfoNetParam;
import com.dongdong.socket.normal.UdpClientSocket;
import com.dongdong.utils.DDLog;
import com.jr.door.Launcher;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import cz.msebera.android.httpclient.Header;

import static com.dongdong.api.ApiHttpClient.getDVNotices;

public class DongDongCenter {
    private static LauncherCallback mLauncherCallback;
    private static DongDongTransferCenter mTransferCenter;
    private static DSParse mCmdParse = new DSParse();
    private static DSParse mAudioParse = new DSParse();

    private static OrdinarySocketThread mOrdinarySocketThread;
    private static SoundSocketThread mSoundSocketThread;
    private static boolean mOrdinarySocketThreadFlag = true;
    private static boolean mSoundSocketThreadFlag = true;

    private static DongDongCenter mInstance = new DongDongCenter();

    private DongDongCenter() {
    }

    public static DongDongCenter getInstance() {
        return mInstance;
    }

    /**
     * 初始化sdk
     */
    public void initSDK(LauncherCallback launcherCallback) {
        mLauncherCallback = launcherCallback;
        mTransferCenter = new DongDongTransferCenter(launcherCallback);
        mOrdinarySocketThread = new OrdinarySocketThread("CMD_Thread");
        mSoundSocketThread = new SoundSocketThread("Sound_Thread");
        mOrdinarySocketThreadFlag = mSoundSocketThreadFlag = true;
        mOrdinarySocketThread.start();
        mSoundSocketThread.start();
    }

    /**
     * 销毁sdk
     */
    public void finishSDK() {
        mOrdinarySocketThreadFlag = mSoundSocketThreadFlag = false;
        mOrdinarySocketThread.interrupt();
        mSoundSocketThread.interrupt();
        mOrdinarySocketThread = null;
        mSoundSocketThread = null;
    }

    // 解决WifiSettings界面显示bug start
    public static LauncherAndBackendSignalCallback mLAndBSignalCallback;

    public static void setWifiSettingsActivity(LauncherAndBackendSignalCallback callback) {
        mLAndBSignalCallback = callback;
    }

    public static void onGetWifiParams(InfoNetParam netParam) {
        DDLog.d("DongDongCenter.class onGetWifiParams--->>>mLAndBSignalCallback："
                + mLAndBSignalCallback);
        if (mLAndBSignalCallback != null) {
            mLAndBSignalCallback.onGetNetResult(0, netParam);
        }
    }

    public static void onSetWifiParams(int result) {
        if (mLAndBSignalCallback != null) {
            mLAndBSignalCallback.onSetNetResult(0, result);
        }
    }
    // 解决WifiSettings界面显示bug end

    /**
     * 呼叫房号
     *
     * @param number 房号
     */
    public static void queryRoomNumber(final String number) {
        SocketThreadManager.startSocketThread(new Runnable() {
            public void run() {
                UdpClientSocket client;
                String serverHost = AppConfig.SERVER_HOST_IP;
                int serverPort = 45611;
                try {
                    client = new UdpClientSocket();
                    DSPacket packet = new DSPacket();
                    byte[] callPkt = packet.callRequest(0, number);// 要加一个判断，有可能为null
                    if (callPkt == null) {
                        return;
                    }
                    client.send(serverHost, serverPort, callPkt, callPkt.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, "callRoomNumber");
    }

    /**
     * 挂断
     *
     * @param reason 挂断的原因：1=用户强制挂断 2=软件超时挂断
     */
    public static void handUp(final int reason) {
        SocketThreadManager.startSocketThread(new Runnable() {

            @Override
            public void run() {
                UdpClientSocket client;
                String serverHost = AppConfig.SERVER_HOST_IP;
                int serverPort = 45611;
                try {
                    client = new UdpClientSocket();
                    DSPacket packet = new DSPacket();
                    byte[] callPkt = packet.handUpRequest(0, reason);

                    client.send(serverHost, serverPort, callPkt, callPkt.length);
                    mCmdParse.setDSStatus(APlatData.STATUS_CALL_END);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "handUp");
    }

    /**
     * 检查刷卡卡号请求
     *
     * @param cardNum 卡号
     */
    public static void validCardRequest(final String cardNum) {
        SocketThreadManager.startSocketThread(new Runnable() {

            @Override
            public void run() {
                UdpClientSocket client;
                String serverHost = AppConfig.SERVER_HOST_IP;
                int serverPort = 45611;
                try {
                    client = new UdpClientSocket();
                    DSPacket packet = new DSPacket();
                    byte[] callPkt = packet.isValidCardRequest(0, cardNum);
                    if (callPkt == null) {
                        return;
                    }

                    client.send(serverHost, serverPort, callPkt, callPkt.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, "validCardRequest");
    }

    /**
     * 请求网络信息
     */
    public static void getNetRequest() {
        SocketThreadManager.startSocketThread(new Runnable() {

            @Override
            public void run() {
                UdpClientSocket client;
                String serverHost = AppConfig.SERVER_HOST_IP;
                int serverPort = 45611;
                try {
                    client = new UdpClientSocket();
                    DSPacket packet = new DSPacket();
                    byte[] callPkt = packet.getNetRequest(0);
                    if (callPkt == null) {
                        return;
                    }

                    client.send(serverHost, serverPort, callPkt, callPkt.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "getNetRequest");
    }

    /**
     * 设置网络信息
     */
    public static void setNetRequest(final int cmdFlag, final InfoNetParam infoNetParam) {
        SocketThreadManager.startSocketThread(new Runnable() {

            @Override
            public void run() {
                UdpClientSocket client;
                String serverHost = AppConfig.SERVER_HOST_IP;
                int serverPort = 45611;
                try {
                    client = new UdpClientSocket();
                    DSPacket packet = new DSPacket();
                    byte[] callPkt = packet.setNetRequest(cmdFlag, infoNetParam);
                    if (callPkt == null) {
                        return;
                    }
                    client.send(serverHost, serverPort, callPkt, callPkt.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "setNetRequest");
    }

    /**
     * 检查开门密码请求
     *
     * @param roomNum 房号
     * @param pwd     开门密码
     */
    public static void validPasswordRequest(final String roomNum, final String pwd) {
        SocketThreadManager.startSocketThread(new Runnable() {

            @Override
            public void run() {
                UdpClientSocket client;
                String serverHost = AppConfig.SERVER_HOST_IP;
                int serverPort = 45611;
                try {
                    client = new UdpClientSocket();
                    DSPacket packet = new DSPacket();
                    byte[] callPkt = packet.isValidPasswordRequest(0,
                            roomNum, pwd);
                    if (callPkt == null) {
                        return;
                    }
                    client.send(serverHost, serverPort, callPkt, callPkt.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, "validPasswordRequest");
    }

    public static void getTimeStamp(final int cmdFlag) {
        SocketThreadManager.startSocketThread(new Runnable() {

            @Override
            public void run() {
                UdpClientSocket client;
                String serverHost = AppConfig.SERVER_HOST_IP;
                int serverPort = 45611;
                try {
                    client = new UdpClientSocket();
                    DSPacket packet = new DSPacket();
                    byte[] callPkt = packet.getTimestamp(cmdFlag);
                    if (callPkt == null) {
                        return;
                    }
                    client.send(serverHost, serverPort, callPkt, callPkt.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, "validPasswordRequest");
    }

    public static void getRoomCardInfo(final int cmdFlag, CopyOnWriteArraySet<Integer> roomIDSet) {
        DDLog.i("DongDongCenter.clazz--->>>getRoomCardInfo:" + roomIDSet);
        CountTimeRunnable.mUpdateRoomCardCount = 0;//重置定时器计数
        UdpClientSocket client;
        String serverHost = AppConfig.SERVER_HOST_IP;
        int serverPort = 45611;
        try {
            client = new UdpClientSocket();
            DSPacket pkt = new DSPacket();
            byte[] callPkt = pkt.getRoomCardInfoRequest(cmdFlag, roomIDSet);
            if (callPkt != null) {
                client.send(serverHost, serverPort, callPkt, callPkt.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取物业公告
     *
     * @param deviceId
     */
    public static void getBulletinFromNet(final int deviceId) {
        ((Launcher) mLauncherCallback).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //55服务器
                //String url = "http://192.168.68.55/web/wuye_api/apiserver/2.0/";
                //新服务器
                String url = "http://wuye.dd121.com/dd/wuye_api_d/2.0/";
                RequestParams params = getDVNotices(url, deviceId, 0, 10);
                ApiHttpClient.postDirect(url, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            String jsonInitData = new JSONObject(new String(responseBody)).getString
                                    ("response_params");
                            String jsonData = new JSONObject(jsonInitData).getString("villagenotices");
                            Log.e("GT", "DongDongCenter.clazz-->getBulletinFromNet()-->jsonData:" + jsonData);
                            if (jsonData.equals("[]")) {
                                return;
                            }
                            //1.处理从网络中获取的公告
                            List<BulletinBean> localData = BulletinOpe.queryAll(BaseApplication.context());
                            List<BulletinBean> netData = JSON.parseArray(jsonData, BulletinBean.class);

                            for (BulletinBean netBean : netData) {
                                boolean isSame = false;
                                //1.1将本地数据库中的数据与平台返回数据对比
                                for (BulletinBean localBean : localData) {
                                    if (localBean.getCreated().equals(netBean.getCreated()))
                                        isSame = true;
                                }
                                //1.2不相同就添加到本地并且更新界面数据
                                if (!isSame) {
                                    BulletinOpe.insert(BaseApplication.context(), netBean);
                                }
                            }
                            //2.回调Launcher，更新界面
                            if (mLauncherCallback != null)
                                Log.e("GT", "DongDongCenter.clazz-->mLauncherCallback:" + mLauncherCallback);
                            mLauncherCallback.onGetBulletinFromNet(BulletinOpe.queryAll(BaseApplication.context()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e("GT", "Launcher.clazz-->getBulletinFromNet()-->onFailure");
                    }
                });
            }
        });
    }


    // ///////////////////////////////////////////////////////////////////////////////////
    private class OrdinarySocketThread extends Thread {

        OrdinarySocketThread(String ddCmdThread) {
            super(ddCmdThread);
        }

        @Override
        public void run() {
            try {
                initSocket();
            } catch (IOException e) {
                DDLog.e("DongDongCenter.class initSocketThread failed!!!");
                e.printStackTrace();
            }
        }
    }

    private class SoundSocketThread extends Thread {

        SoundSocketThread(String ddSoundThread) {
            super(ddSoundThread);
        }

        @Override
        public void run() {
            try {
                initSoundSocket();
            } catch (IOException e) {
                DDLog.e("DongDongCenter.class initSoundSocketThread failed!!!");
                e.printStackTrace();
            }
        }
    }

    private void initSocket() throws IOException {
        DDLog.e("DongDongCenter.class initCMDSocket--->>>45601");
        DatagramSocket server = new DatagramSocket(45601);
        // 接收
        byte[] recvBuf = new byte[1400];
        DatagramPacket recPacket = new DatagramPacket(recvBuf, recvBuf.length);
        mCmdParse.setDSCallback(mTransferCenter);
        while (mOrdinarySocketThreadFlag) {
            server.receive(recPacket);
            mCmdParse.parseReceiveData(recvBuf, recPacket.getLength());
        }
        server.close();
    }

    private void initSoundSocket() throws IOException {
        DDLog.e("DongDongCenter.class initSoundSocket--->>>45603");
        DatagramSocket server = new DatagramSocket(45603);
        // 接收
        byte[] recvBuf = new byte[1400];
        DatagramPacket recPacket = new DatagramPacket(recvBuf, recvBuf.length);
        mAudioParse.setDSCallback(mTransferCenter);
        while (mSoundSocketThreadFlag) {
            server.receive(recPacket);
            mAudioParse.parseReceiveData(recvBuf, recPacket.getLength());
        }
        server.close();
    }
    // ///////////////////////////////////////////////////////////////////////////////////
}
