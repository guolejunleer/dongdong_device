package com.dongdong.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerTaskManager {
    private static ScheduledExecutorService mScheduledExecutorService;

    static {
        // 线程池能按时间计划来执行任务，允许用户设定计划执行任务的时间，int类型的参数是设定
        // 线程池中线程的最小数目。当任务较多时，线程池可能会自动创建更多的工作线程来执行任务
        mScheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    private TimerTaskManager() {
    }


    /**
     * 添加新任务
     *
     * @param task 新任务
     */
    public static void addOneTask(Runnable task) {
        DDLog.i("TimerTaskManager.clazz--->>>ScheduledExecutorService:"
                + mScheduledExecutorService);
        if (mScheduledExecutorService == null) {
            mScheduledExecutorService = Executors.newScheduledThreadPool(1);
        }
        mScheduledExecutorService.scheduleWithFixedDelay(task, 0, 1000, TimeUnit.MILLISECONDS);

    }

    /**
     * 退出线程池
     */
    public static void exitExecutor() {
        if (mScheduledExecutorService != null)
            mScheduledExecutorService.shutdown();
        mScheduledExecutorService = null;
    }
}
