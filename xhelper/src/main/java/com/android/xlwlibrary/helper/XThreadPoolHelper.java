package com.android.xlwlibrary.helper;

import com.android.xlwlibrary.threadpool.ComparePriority;
import com.android.xlwlibrary.threadpool.ThreadPool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xu on 2019/12/10.
 */
public class XThreadPoolHelper {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();//CPU数量
    private static final int CPU_POOL_SIZE = CPU_COUNT + 1; //核心线程数
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;//非核心线程数
    private static final int KEEP_ALIVE = 3;
    //线程工厂，用来创建线程
    private static final ThreadFactory th = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        public Thread newThread(Runnable r) {
            return new Thread(r, "DownLoadThred#" + mCount.getAndIncrement());
        }
    };
    //线程池队列
    private static final LinkedBlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(128);
    /**
     * 下载中的异常
     */
    private static final RejectedExecutionHandler re = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            //不做任何处理，直接抛出异常
            throw new RejectedExecutionException("Task " + r.toString() + " rejected from " + executor.toString());
        }
    };
    //没有核心线程，最大线程数量的线程池
    public static final ThreadPoolExecutor maxThreadPool = new ThreadPoolExecutor(0,
            Integer.MAX_VALUE,
            60L,
            TimeUnit.SECONDS,
            new PriorityBlockingQueue<Runnable>(20, new ComparePriority()));

    //依据cpu 设定具有核心线程数量的线程池
    public static final ThreadPool pool = new ThreadPool(CPU_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE,
            TimeUnit.SECONDS,
            new PriorityBlockingQueue<Runnable>(20, new ComparePriority()),
            th,
            re);
}
