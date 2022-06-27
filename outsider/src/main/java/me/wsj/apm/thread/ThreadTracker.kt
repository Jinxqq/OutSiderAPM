package me.wsj.apm.thread

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import me.wsj.apm.TAG
import me.wsj.core.ITracker

class ThreadTracker : ITracker {

    private val handlerThread = HandlerThread("OutSider-Thread", Thread.NORM_PRIORITY)

    private val mHandler: Handler

    init {
        handlerThread.start()
        mHandler = Handler(handlerThread.looper)
    }

    override fun destroy(application: Application?) {
        mHandler.removeCallbacksAndMessages(null)
        handlerThread.quit()
    }

    override fun startTrack(application: Application?) {
        mHandler.postDelayed(object : Runnable {
            override fun run() {
                Log.e(TAG, "current thread cnt: " + Thread.getAllStackTraces().size)
                val allStackTraces = Thread.getAllStackTraces()
                allStackTraces.keys.forEach {
                    Log.d(TAG, it.name + " state:" + it.state)
                }
                mHandler.postDelayed(this, MONITOR_INTERVAL)
            }
        }, MONITOR_START_DELAY)
    }

    override fun pauseTrack(application: Application?) {
        //To do sth.
    }

    companion object {
        private const val MONITOR_START_DELAY: Long = 1 * 1000
        private const val MONITOR_INTERVAL: Long = 20 * 1000

        val instance: ThreadTracker by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { ThreadTracker() }
    }
}