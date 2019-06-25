package com.firefly.filepicker.commom;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by rany on 18-1-19.
 */

public class ScanThreadPoolManager {
    private static ScanThreadPoolManager sInstance = null;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    private final ThreadPoolExecutor mExecutorService;
    private final BlockingQueue<Runnable> mTaskQueue;

    // The class is used as a singleton
    static {
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        sInstance = new ScanThreadPoolManager();
    }

    private ScanThreadPoolManager() {
        // initialize a queue for the thread pool. New tasks will be added to this queue
        mTaskQueue = new SynchronousQueue<>();
        mExecutorService = new ThreadPoolExecutor(
                NUMBER_OF_CORES,
                NUMBER_OF_CORES,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mTaskQueue);
    }

    public static ScanThreadPoolManager getsInstance() {
        return sInstance;
    }

    public void execute(Runnable runnable) {
        mExecutorService.execute(runnable);
    }

    public void cancel() {
        mExecutorService.shutdownNow();
    }

    public ExecutorService getExecutorService() {
        return mExecutorService;
    }
}
