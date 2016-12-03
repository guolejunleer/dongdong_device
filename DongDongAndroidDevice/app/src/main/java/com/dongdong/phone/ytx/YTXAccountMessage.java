package com.dongdong.phone.ytx;

import android.text.TextUtils;

import com.dongdong.AppConfig;
import com.dongdong.socket.beat.ALinuxData;
import com.dongdong.socket.beat.Packet;
import com.dongdong.sdk.SocketThreadManager;

import com.dongdong.socket.normal.UdpClientSocket;

/**
 * 拨打电话实体类
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class YTXAccountMessage {

    private String accountSID;
    private String authToken;
    private String appID;
    private String vendorPhone;

    public String getAccountSID() {
        return accountSID;
    }

    public void setAccountSID(String accountSID) {
        this.accountSID = accountSID;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getVendorPhone() {
        return vendorPhone;
    }

    public void setVendorPhone(String vendPhone) {
        this.vendorPhone = vendPhone;
    }

    public boolean getEffect() {
        if (TextUtils.isEmpty(accountSID) || TextUtils.isEmpty(authToken)
                || TextUtils.isEmpty(appID)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "appID :" + appID + "; accountSID:" + accountSID;
    }

    /**
     * 请求拨打电话平台配置参数请求
     */
    public static void getVOIPParamRequest(final int deviceId, final int voipType) {
        SocketThreadManager.startSocketThread(new Runnable() {

            @Override
            public void run() {
                UdpClientSocket client;
                String serverHost = AppConfig.SERVER_HOST_IP;
                int serverPort = ALinuxData.UNICAST_PORT;
                try {
                    client = new UdpClientSocket();
                    Packet pkt = new Packet();
                    byte[] callPkt = pkt.getVOIPParamRequest(0, deviceId, voipType);
                    if (callPkt == null) {
                        return;
                    }
                    client.send(serverHost, serverPort, callPkt, callPkt.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, "getVOIPParamRequest");
    }
}
