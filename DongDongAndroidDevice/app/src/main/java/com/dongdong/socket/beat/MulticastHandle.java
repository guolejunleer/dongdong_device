package com.dongdong.socket.beat;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastHandle {
    private MulticastSocket mMulsocket;
    private InetAddress mInetAddr;
    private int port = 0;
    private Parser mParser;

    public MulticastHandle() {
    }

    /**
     * 加入组播
     *
     * @throws Exception
     */
    public void joinGroup(String addr, int port) throws Exception {
        if (mMulsocket != null) {
            return;
        }
        try {
            mInetAddr = InetAddress.getByName(addr);
            this.port = port;
            mMulsocket = new MulticastSocket(port);
            mMulsocket.joinGroup(mInetAddr);
            mParser = new Parser();
            ALinuxData.debugLog("MulticastHandle.clazz--->>>joinGroup successful "
                    + mInetAddr + ":" + port);
        } catch (Exception e) {
            ALinuxData.debugLog("MulticastHandle.clazz--->>joinGroup failed!!!!" +
                    " we will restart joningGroup..." + mInetAddr + ":" + port);
            e.printStackTrace();
        }
    }

    /**
     * 离开组播
     *
     * @throws Exception
     */
    public void leaveGroup() throws Exception {
        if (mMulsocket == null) {
            return;
        }
        try {
            mMulsocket.leaveGroup(mInetAddr);
            mMulsocket.close();
            mMulsocket = null;
            ALinuxData.debugLog("MulticastHandle.clazz-->>>leaveGroup successful "
                    + mInetAddr + ":" + port);
        } catch (Exception e) {
            ALinuxData.debugLog("MulticastHandle.clazz--->>>leaveGroup failed! "
                    + mInetAddr + ":" + port);
            e.printStackTrace();
        }
    }

    /**
     * 发送消息到组播组
     *
     * @throws Exception
     */
    public void sendtoGroup(byte[] message) throws Exception {
        try {
            ALinuxData.debugLog("Multicasthandle.class--->>>sendtoGroup  mInetAddr:"
                    + mInetAddr + "; port:" + port);
            DatagramPacket dp = new DatagramPacket(message, message.length,
                    mInetAddr, port);
            mMulsocket.send(dp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收消息从组播组
     *
     * @throws Exception
     */
    public void recvfromGroup() throws Exception {
        try {
            byte[] buffer = new byte[1400];
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            mMulsocket.receive(dp);
            mParser.parseReceiveData(dp);
            ALinuxData.debugLog("********MulticastHandle.clazz--->>>recvfrom ip:"
                    + dp.getAddress().getHostAddress() + " ;InetAddress: " + dp.getAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
