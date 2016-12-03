package com.dongdong.socket.beat;

import com.jr.door.Launcher;

public class Search {

    /**
     * 组播搜索
     */
    private MulticastHandle mMulHandle;
    private static Thread mMulcastThread;
    private boolean mSendScanResponse = true;
    private volatile boolean mMulticastRunFlag = false;

    /**
     * 单播搜索
     */
    private UnicastHandle mUniHandle;
    private static Thread mUnicastThread;
    private volatile boolean mUnicastRunFlag = false;
    public static int mThreadCount;

    /**
     * 设置回调指针
     */
    public void setPeerAddressCallback(PeerAddressCallback sink) {
        ALinuxData.debugLog("Search.clazz--->>>setPeerAddressCallback:" + sink);
        ALinuxData.mSink = sink;
    }

    /**
     * 发送搜索回应 当上层检测到网络改变时调用此接口
     */
    public void sendScanResponse() {
        ALinuxData.debugLog("Search.clazz--->>>sendScanResponse");
        if (mMulcastThread != null) {
            mSendScanResponse = true;
        }
    }

    /**
     * 发送搜索回应 当上层检测到网络改变时调用此接口
     */
    public void sendMulticastSocket2Group() {
        ALinuxData.debugLog("Search.clazz--->>>sendMulticastSocket2Group mMulHandle:"
                + mMulHandle);
        if (mMulHandle == null) return;

        try {
            Packet pkt = new Packet();
            byte[] bytes = pkt.scanReponese(0);
            if (bytes != null) {
                mMulHandle.sendtoGroup(bytes);
            }
        } catch (Exception e) {
            ALinuxData.debugLog("Search.clazz--->>>sendMulticastSocket2Group e:" + e);
            e.printStackTrace();
        }
    }

    public void startSearch() {
        // 重置参数
        resetParams();
        mSendScanResponse = true;
        mMulticastRunFlag = true;
        mMulHandle = new MulticastHandle();
        mMulcastThread = new MulticastThread();
        mMulcastThread.start();

        mUnicastRunFlag = true;
        mUniHandle = new UnicastHandle();
        mUnicastThread = new UnicastThread();
        mUnicastThread.start();// 本机网络，驱动网络
    }

    private void resetParams() {
        if (mMulHandle != null) {
            stopSearch();
        }

        if (mUniHandle != null) {
            stopUnicastSearch();
        }
        mUniHandle = null;
        mUnicastThread = null;
    }

    public void stopSearch() {
        try {
            mMulticastRunFlag = false;
            if (mMulHandle != null) {
                mMulHandle.leaveGroup();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopUnicastSearch() {
        try {
            mUnicastRunFlag = false;
            if (mUniHandle != null) {
                mUniHandle.closeSocket();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MulticastThread extends Thread {

        public MulticastThread() {
            super("MulticastThread:" + mThreadCount++);
        }

        public void run() {
            try {
                sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            try {
                mMulHandle.joinGroup(ALinuxData.MULTICAST_ADDR,
                        ALinuxData.MULTICAST_PORT);

                while (mMulticastRunFlag) {
                    if (mSendScanResponse) {
                        Packet pkt = new Packet();
                        byte[] bytes = pkt.scanReponese(0);
                        if (bytes != null) {
                            mMulHandle.sendtoGroup(bytes);
                        }
                        mSendScanResponse = false;
                    }
                    mMulHandle.recvfromGroup();
                }
                mMulHandle.leaveGroup();
                ALinuxData.debugLog("Search.clazz--->>>MulticastThread break!!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class UnicastThread extends Thread {

        public UnicastThread() {
            super("UnicastThread:" + mThreadCount++);
        }

        public void run() {
            try {
                ALinuxData.debugLog("Search.clazz--->>>UnicastThread begin recvfrom port:"
                        + ALinuxData.UNICAST_PORT);
                mUniHandle.initSocket(ALinuxData.UNICAST_PORT);

                while (mUnicastRunFlag) {
                    mUniHandle.recvfrom();
                }
                mUniHandle.closeSocket();
                ALinuxData.debugLog("Search.clazz--->>>UnicastThread break!!!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
