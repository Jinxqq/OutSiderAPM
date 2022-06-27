package me.wsj.apm.jank

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.Printer
import me.wsj.apm.TAG
import me.wsj.core.ITracker
import me.wsj.core.job.AsyncThreadTask

/**
 * 卡顿模块Task
 *
 * @author OutSiderAPM
 */

class BlockTracker : ITracker {
    private val SUB_TAG = "BlockTask"
    private val mBlockThread = HandlerThread("OutSider-Block")
    private var mHandler: Handler? = null
    private val mBlockRunnable = Runnable {
        val sb = StringBuilder()
        val stackTrace = Looper.getMainLooper().thread.stackTrace
        for (s in stackTrace) {
            sb.append("$s".trimIndent()).append("\n")
        }
        Log.d(TAG, sb.toString())
        saveBlockInfo(sb.toString())
    }

    private fun startMonitor() {
        mHandler!!.postDelayed(mBlockRunnable, blockMinTime.toLong())
    }

    private fun removeMonitor() {
        mHandler!!.removeCallbacks(mBlockRunnable)
    }

    /**
     * 保存卡顿相关信息
     */
    private fun saveBlockInfo(stack: String) {
        AsyncThreadTask.execute {
            val info = BlockInfo()
            info.blockStack = stack
            info.blockTime = blockMinTime
            Log.e(TAG, info.toString())
//                ITask task = Manager.getInstance().getTaskManager().getTask(ApmTask.TASK_BLOCK);
//                if (task != null) {
//                    task.save(info);
//                } else {
//                    Log.d(TAG, "Client", "BlockInfo task == null");
//                }
        }
    }

    override fun destroy(application: Application) {}

    override fun startTrack(application: Application) {
        if (!mBlockThread.isAlive) { //防止多次调用
            mBlockThread.start()
            mHandler = Handler(mBlockThread.looper)
            Looper.getMainLooper().setMessageLogging(object : Printer {
                override fun println(x: String) {
                    if (x.startsWith(START)) {
                        startMonitor()
                    } else if (x.startsWith(END)) {
                        removeMonitor()
                    }
                }
            })
        }
    }

    override fun pauseTrack(application: Application) {}

    companion object {
        private const val START = ">>>>> Dispatching"
        private const val END = "<<<<< Finished"
        private const val blockMinTime = 4500

        val instance: BlockTracker by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { BlockTracker() }
    }
}