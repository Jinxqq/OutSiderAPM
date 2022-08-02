package me.wsj.apm.thread

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import me.wsj.apm.TAG
import me.wsj.core.ITracker
import me.wsj.core.utils.Looger
import me.wsj.core.utils.ProcessUtils
import java.util.regex.Pattern

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
                track()
                mHandler.postDelayed(this, MONITOR_INTERVAL)
            }
        }, MONITOR_START_DELAY)
    }

    var maxCnt = 0

    private fun track(location: String = "") {
        if (Thread.currentThread().name == "main") {
            return
        }
//        val cmd = "adb shell cat proc/${Process.myPid()}/status"
        val cmd = "cat proc/${Process.myPid()}/status"
        val result = ProcessUtils.shellExec(cmd)
        // (?<=Threads:\s).+\d(?=\n)
        if (result != null) {
            val pattern = Pattern.compile("(?<=Threads:\\s).+\\d(?=\\n)")
            val matcher = pattern.matcher(result)
            if (matcher.find()) {
                val cnt = matcher.group(0)
                maxCnt = cnt!!.toInt().coerceAtLeast(maxCnt)
                Looger.e(TAG, "all thread cnt: $cnt, max: $maxCnt $location")
            }
        }
//        Looger.e(TAG, "simple thread cnt: " + Thread.getAllStackTraces().size)
//        val allStackTraces = Thread.getAllStackTraces()
//        allStackTraces.keys.forEach {
//            Looger.d(TAG, it.name + " state:" + it.state)
//        }
    }

    override fun pauseTrack(application: Application?) {
        //To do sth.
    }

    companion object {
        private const val MONITOR_START_DELAY: Long = 1 * 1000
        private const val MONITOR_INTERVAL: Long = 60 * 1000

        @JvmStatic
        fun trackOnce(location: String) {
            instance.track(location)
        }

        val instance: ThreadTracker by lazy { ThreadTracker() }
    }
}