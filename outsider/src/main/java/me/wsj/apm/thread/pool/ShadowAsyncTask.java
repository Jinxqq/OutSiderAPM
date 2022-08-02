package me.wsj.apm.thread.pool;

import java.util.concurrent.ThreadPoolExecutor;

import me.wsj.core.utils.Looger;

/**
 * 设置allowCoreThreadTimeOut(true)
 * 在Application 类的 <clinit>() 中调用
 */
public class ShadowAsyncTask {

    /**
     * Optimize the thread pool executor of AsyncTask with {@code allowCoreThreadTimeOut = true}
     */
    public static void optimizeAsyncTaskExecutor() {
        try {
            final ThreadPoolExecutor executor = ((ThreadPoolExecutor) android.os.AsyncTask.THREAD_POOL_EXECUTOR);
            executor.allowCoreThreadTimeOut(true);
            Looger.i("Optimize AsyncTask executor success！");
        } catch (final Throwable t) {
            Looger.i("Optimize AsyncTask executor error: allowCoreThreadTimeOut = true" + t);
        }
    }
}