package me.wsj.apm.mem

import android.app.Activity
import android.app.Application
import me.wsj.core.ITracker
import android.os.HandlerThread
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import me.wsj.core.BaseTracker
import me.wsj.core.extensions.noOpDelegate
import java.lang.Exception
import java.util.*

class MemoryTracker private constructor() : BaseTracker<ITrackMemoryListener>(), ITracker {

    private val handlerThread = HandlerThread("OutSider-Memory", Thread.NORM_PRIORITY)

    private val mHandler: Handler

    private val mActivityStringWeakHashMap = WeakHashMap<Activity, String>()

    init {
        handlerThread.start()
        mHandler = Handler(handlerThread.looper)
    }

    private val lifecycleCallbacks: ActivityLifecycleCallbacks =
        object : ActivityLifecycleCallbacks by noOpDelegate() {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                ActivityStack.getInstance().push(activity)
            }

            override fun onActivityStopped(activity: Activity) {
                //  退后台，GC 找LeakActivity
                if (!ActivityStack.getInstance().isInBackGround) {
                    return
                }
                mHandler.postDelayed({ MemUtils.gcTragger() }, 1000)
                mHandler.postDelayed(Runnable {
                    try {
                        if (!ActivityStack.getInstance().isInBackGround) {
                            return@Runnable
                        }
                        //  分配大点内存促进GC
                        MemUtils.gcTragger()
                        SystemClock.sleep(100)
                        System.runFinalization()
                        val hashMap = HashMap<String, Int>()
                        for ((key) in mActivityStringWeakHashMap) {
                            val name = key.javaClass.simpleName
                            val value = hashMap[name]
                            if (value == null) {
                                hashMap[name] = 1
                            } else {
                                hashMap[name] = value + 1
                            }
                        }
                        if (listeners.size > 0) {
                            for ((key, value) in hashMap) {
                                for (listener in listeners) {
                                    listener.onLeakActivity(key, value)
                                }
                            }
                        }
                    } catch (ignored: Exception) {
                    }
                }, 10000)
            }

            override fun onActivityDestroyed(activity: Activity) {
                mActivityStringWeakHashMap[activity] = activity.javaClass.simpleName
                ActivityStack.getInstance().pop(activity)
            }
        }

    override fun destroy(application: Application) {
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        mHandler.removeCallbacksAndMessages(null)
        handlerThread.quit()
    }

    override fun startTrack(application: Application) {
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
        mHandler.postDelayed(object : Runnable {
            override fun run() {
                if (listeners.size > 0 && !ActivityStack.getInstance().isInBackGround) {
                    val trackMemoryInfo = MemUtils.collectMemoryInfo(application)
                    for (listener in listeners) {
                        listener.onCurrentMemoryCost(trackMemoryInfo)
                    }
                }
                mHandler.postDelayed(this, MONITOR_INTERVAL)
            }
        }, MONITOR_INTERVAL)
    }

    override fun pauseTrack(application: Application) {}


    companion object {
        private const val MONITOR_INTERVAL: Long = 30_1000

        val instance: MemoryTracker by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { MemoryTracker() }
    }
}