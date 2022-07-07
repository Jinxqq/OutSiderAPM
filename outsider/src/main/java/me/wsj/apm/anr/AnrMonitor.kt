package me.wsj.apm.anr

import android.app.Application
import android.os.Debug
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import me.wsj.core.ITracker
import me.wsj.core.utils.Looger

class AnrMonitor(private val timeoutInterval: Long = DEFAULT_ANR_TIMEOUT) : ITracker {
    private val mainHandler: Handler = Handler(Looper.getMainLooper())
    private var handlerThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    init {
        handlerThread = object : HandlerThread(TAG, Thread.NORM_PRIORITY) {
            override fun onLooperPrepared() {
                super.onLooperPrepared()
                backgroundHandler = Handler(handlerThread!!.looper)
            }
        }
    }

    private var mAnrInterceptor: AnrInterceptor? = DEFAULT_ANR_INTERCEPTOR
    private var mAnrListener: AnrListener? = DEFAULT_ANR_LISTENER

    private var mPrefix: String? = ""
    private var mLogThreadWithoutStackTrace = false
    private var mIgnoreDebugger = false

    @Volatile
    private var mTick = 0L

    @Volatile
    private var mReported = false

    @Volatile
    private var mInterval: Long = timeoutInterval

    private val mTicker = Runnable {
        mTick = 0
        mReported = false
    }

    private val mAnrCollector = Runnable {
        // If the main thread has not handled mTicker, it is blocked. ANR.
        if (mTick != 0L && !mReported) {
            //noinspection ConstantConditions
            if (!mIgnoreDebugger && (Debug.isDebuggerConnected() || Debug.waitingForDebugger())) {
                Looger.i(TAG,"An ANR was detected but ignored because the debugger is connected (you can prevent this with setIgnoreDebugger(true))")
                mReported = true
                delayToTryCollectingAnr()
                return@Runnable
            }
            mInterval = mAnrInterceptor?.intercept(mTick) ?: 0
            if (mInterval > 0) {
                delayToTryCollectingAnr()
                return@Runnable
            }
            val anrError = if (mPrefix == null) {
                AnrError.newMainInstance(mTick)
            } else {
                AnrError.newInstance(mTick, mPrefix!!, mLogThreadWithoutStackTrace)
            }
            mAnrListener?.onAppNotResponding(anrError)
            mInterval = timeoutInterval
            mReported = true
        }
        delayToTryCollectingAnr()
    }

    fun setAnrListener(anrListener: AnrListener?): AnrMonitor {
        mAnrListener = anrListener ?: DEFAULT_ANR_LISTENER
        Looger.i(TAG, "setAnrListener-----")
        return this
    }

    fun setAnrInterceptor(anrInterceptor: AnrInterceptor?): AnrMonitor {
        mAnrInterceptor = anrInterceptor ?: DEFAULT_ANR_INTERCEPTOR
        return this
    }

    /**
     * Set the prefix that a thread's name must have for the thread to be reported.
     * Note that the main thread is always reported.
     * Default "".
     *
     * @param prefix The thread name's prefix for a thread to be reported.
     * @return itself for chaining.
     */
    fun setReportThreadNamePrefix(prefix: String): AnrMonitor {
        mPrefix = prefix
        return this
    }

    /**
     * Set that only the main thread will be reported.
     *
     * @return itself for chaining.
     */
    fun setReportMainThreadOnly(): AnrMonitor {
        mPrefix = null
        return this
    }

    /**
     * Set that all threads will be reported (default behaviour).
     *
     * @return itself for chaining.
     */
    fun setReportAllThreads(): AnrMonitor {
        mPrefix = ""
        return this
    }

    /**
     * Set that all running threads will be reported,
     * even those from which no stack trace could be extracted.
     * Default false.
     *
     * @param logThreadsWithoutStackTrace Whether or not all running threads should be reported
     * @return itself for chaining.
     */
    fun setLogThreadWithoutStackTrace(logThreadsWithoutStackTrace: Boolean): AnrMonitor {
        mLogThreadWithoutStackTrace = logThreadsWithoutStackTrace
        return this
    }

    /**
     * Set whether to ignore the debugger when detecting ANRs.
     * When ignoring the debugger, ANRWatchdog will detect ANRs even if the debugger is connected.
     * By default, it does not, to avoid interpreting debugging pauses as ANRs.
     * Default false.
     *
     * @param ignoreDebugger Whether to ignore the debugger.
     * @return itself for chaining.
     */
    fun setIgnoreDebugger(ignoreDebugger: Boolean): AnrMonitor {
        mIgnoreDebugger = ignoreDebugger
        return this
    }

    @Synchronized
    private fun delayToTryCollectingAnr() {
        val needPost = mTick == 0L
        mTick += mInterval
        if (needPost) {
            mainHandler.post(mTicker)
        }
        backgroundHandler?.postDelayed(mAnrCollector, ANR_COLLECTING_INTERVAL)
    }

    companion object {
        const val DEFAULT_ANR_TIMEOUT = 5000L
        const val ANR_COLLECTING_INTERVAL = 2000L
        private const val TAG = "OutSider-ANR"

        private val DEFAULT_ANR_LISTENER = object : AnrListener {
            override fun onAppNotResponding(error: AnrError) {
                throw error
            }
        }

        private val DEFAULT_ANR_INTERCEPTOR = object : AnrInterceptor {
            override fun intercept(duration: Long): Long {
                return 0
            }
        }

        val instance: AnrMonitor by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            AnrMonitor()
        }
    }

    override fun destroy(application: Application?) {
        backgroundHandler?.removeCallbacksAndMessages(null)
        mainHandler.removeCallbacksAndMessages(null)

        handlerThread?.quitSafely()
        handlerThread = null
    }

    override fun startTrack(application: Application?) {
        handlerThread?.start()
        delayToTryCollectingAnr()
    }

    override fun pauseTrack(application: Application?) {
        //To do sth.
    }
}