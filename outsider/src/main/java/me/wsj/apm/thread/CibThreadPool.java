package me.wsj.apm.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by shiju.wang on 2022/3/7
 */
public class CibThreadPool {
    /*
     * Number of cores to decide the number of threads
     */
    private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    /*
     * set thread keep alive time
     */
    private static final int KEEP_ALIVE_TIME = 30;

    /*
     * thread pool executor for io tasks
     * io密集型任务
     */
    private final ThreadPoolExecutor mIoTasks;

    /*
     * thread pool executor for default tasks
     * cpu密集型任务
     */
    private final ThreadPoolExecutor mDefaultTasks;

    /*
     * thread pool ececutor for main thread tasks
     */
    private final Executor mMainThreadExecutor;

    private static volatile CibThreadPool sInstance;

    public static CibThreadPool getInstance() {
        if (sInstance == null) {
            synchronized (CibThreadPool.class) {
                if (sInstance == null) {
                    sInstance = new CibThreadPool();
                }
            }
        }
        return sInstance;
    }

    private CibThreadPool() {
//        ThreadFactory backgroundPriorityThreadFactory = new PriorityThreadFactory(Process.THREAD_PRIORITY_DEFAULT);
//        Looger.e("核心线程数量： " + NUMBER_OF_CORES);

        mIoTasks = new ThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new SynchronousQueue<>()
        );

        mDefaultTasks = new ThreadPoolExecutor(
                NUMBER_OF_CORES + 1,
                NUMBER_OF_CORES * 2,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(256)
        );
        mMainThreadExecutor = new MainThreadExecutor();
    }

    public Executor getIoTasks() {
        return mIoTasks;
    }

    public Executor getDefaultTasks() {
        return mDefaultTasks;
    }

    //在主线程中执行任务
    public Executor getUITask() {
        return mMainThreadExecutor;
    }

}
