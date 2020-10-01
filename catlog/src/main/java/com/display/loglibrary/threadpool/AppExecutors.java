package com.display.loglibrary.threadpool;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author hanpei
 */
public class AppExecutors {
    /**
     * 文件操作线程池
     */
    private final ExecutorService diskIO;

    /**
     * 业务线程池
     */
    private final ExecutorService businessIO;
    /**
     * 网络操作线程池
     */
    private final ExecutorService networkIO;
    /**
     * 主线程切换
     */
    private final Executor mainThread;
    /**
     * 定时任务线程池
     */
    private final ScheduledExecutorService mScheduledExecutorService;

    private AppExecutors() {
        diskIO = new ThreadPoolExecutor(1,
                1,
                0L,
                TimeUnit.MICROSECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new MyThreadFactory("diskIO"));

        businessIO = new ThreadPoolExecutor(1,
                1,
                0L,
                TimeUnit.MICROSECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new MyThreadFactory("businessIO"));

        networkIO = new ThreadPoolExecutor(3,
                3,
                0L,
                TimeUnit.MICROSECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new MyThreadFactory("networkIO")
        );
        mainThread = new MainThreadExecutor();
        mScheduledExecutorService = new ScheduledThreadPoolExecutor(1, new MyThreadFactory("mScheduledExecutorService"));
    }

    private static class MainThreadExecutor implements Executor {

        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable runnable) {
            mainThreadHandler.post(runnable);
        }
    }

    static class Holder {
        static AppExecutors sAppExecutors = new AppExecutors();
    }

    public static AppExecutors getInstance() {
        return Holder.sAppExecutors;
    }

    public ExecutorService getDiskIo() {
        return diskIO;
    }

    public ExecutorService getBusinessIo() {
        return businessIO;
    }

    public ExecutorService getNetworkIo() {
        return networkIO;
    }

    public Executor getMainThread() {
        return mainThread;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return mScheduledExecutorService;
    }
}
