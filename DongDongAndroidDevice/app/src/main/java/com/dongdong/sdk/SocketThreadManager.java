package com.dongdong.sdk;

public class SocketThreadManager {

    private SocketThreadManager() {

    }

    public static void startSocketThread(Runnable runnable, String threadName) {
        new CommonThread(runnable, threadName).start();
    }

    private static class CommonThread extends Thread {
        CommonThread(Runnable runnable, String threadName) {
            super(runnable, threadName);
        }
    }
}
