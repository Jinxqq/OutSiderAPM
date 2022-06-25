package me.wsj.apm.jank;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Printer;

import me.wsj.core.AsyncThreadTask;
import me.wsj.core.ITracker;

/**
 * 卡顿模块Task
 *
 * @author ArgusAPM Team
 */
public class BlockTracker implements ITracker {
    private final String SUB_TAG = "BlockTask";

    private static volatile BlockTracker sInstance;

    private static final int blockMinTime = 4500;

    private HandlerThread mBlockThread = new HandlerThread("blockThread");
    private Handler mHandler;

    public static BlockTracker getInstance() {
        if (sInstance == null) {
            synchronized (BlockTracker.class) {
                if (sInstance == null) {
                    sInstance = new BlockTracker();
                }
            }
        }
        return sInstance;
    }

    private Runnable mBlockRunnable = new Runnable() {
        @Override
        public void run() {
            StringBuilder sb = new StringBuilder();
            StackTraceElement[] stackTrace = Looper.getMainLooper().getThread().getStackTrace();
            for (StackTraceElement s : stackTrace) {
                sb.append(s.toString() + "\n");
            }
            Log.d(SUB_TAG, sb.toString());
            saveBlockInfo(sb.toString());
        }
    };

    private void startMonitor() {
        mHandler.postDelayed(mBlockRunnable, blockMinTime);
    }

    private void removeMonitor() {
        mHandler.removeCallbacks(mBlockRunnable);
    }

    /**
     * 保存卡顿相关信息
     */
    private void saveBlockInfo(final String stack) {
        AsyncThreadTask.execute(new Runnable() {
            @Override
            public void run() {
                BlockInfo info = new BlockInfo();
                info.blockStack = stack;
                info.blockTime = blockMinTime;
                Log.e("BlockTracker", info.toString());
//                ITask task = Manager.getInstance().getTaskManager().getTask(ApmTask.TASK_BLOCK);
//                if (task != null) {
//                    task.save(info);
//                } else {
//                    Log.d(TAG, "Client", "BlockInfo task == null");
//                }
            }
        });
    }

    @Override
    public void destroy(Application application) {

    }

    @Override
    public void startTrack(Application application) {
        if (!mBlockThread.isAlive()) { //防止多次调用
            mBlockThread.start();
            mHandler = new Handler(mBlockThread.getLooper());
            Looper.getMainLooper().setMessageLogging(new Printer() {
                private static final String START = ">>>>> Dispatching";
                private static final String END = "<<<<< Finished";

                @Override
                public void println(String x) {
                    if (x.startsWith(START)) {
                        startMonitor();
                    }
                    if (x.startsWith(END)) {
                        removeMonitor();
                    }
                }
            });
        }
    }

    @Override
    public void pauseTrack(Application application) {

    }
}
