package com.dongdong.socket.beat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UnicastHandle {
    private DatagramSocket mUnisocket;
    private int mPort = 0;
    private Parser mParser;

    public UnicastHandle() {
    }

    /**
     * @throws Exception
     */
    public void initSocket(int port) throws Exception {
        if (mUnisocket != null) {
            return;
        }
        try {
            this.mPort = port;
            mUnisocket = new DatagramSocket(port);
            ALinuxData.debugLog("UnicastHandle.class --->>>initSocket bind "
                    + port + " successful");
            mParser = new Parser();
        } catch (Exception e) {
            ALinuxData.debugLog("UnicastHandle.class --->>>unicast initSocket bind "
                    + port + " failed!");
            e.printStackTrace();
        }
    }

    /**
     * @throws Exception
     */
    public void closeSocket() throws Exception {
        if (mUnisocket == null) {
            return;
        }
        try {
            mUnisocket.close();
            mUnisocket = null;
            ALinuxData.debugLog("UnicastHandle.class --->>>unbind " + mPort
                    + " successful");
        } catch (Exception e) {
            ALinuxData.debugLog("UnicastHandle.class --->>>unbind " + mPort
                    + " failed!");
            e.printStackTrace();
        }
    }

    /**
     * 发送消息
     *
     * @throws Exception
     */
    public void sendTo(byte[] message, String ip) throws Exception {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            DatagramPacket dp = new DatagramPacket(message, message.length,
                    addr, mPort);
            mUnisocket.send(dp);
            ALinuxData.debugLog("UnicastHandle.class-->>>sendTo mPort: " + mPort);
        } catch (Exception e) {
            e.printStackTrace();
            ALinuxData.debugLog("UnicastHandle.class-->>> sendTo had error: "
                    + e + ";ip:" + ip + "; mPort:" + mPort);
        }
    }

    /**
     * 接收消息
     *
     * @throws Exception
     */
    public void recvfrom() throws Exception {
        try {
            byte[] buffer = new byte[1400];
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            mUnisocket.receive(dp);
            mParser.parseReceiveData(dp);
        } catch (Exception e) {
            e.printStackTrace();
            ALinuxData.debugLog("UnicastHanle.clazz--->>> recvfrom error " + e);
        }

    }
}
