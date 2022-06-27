package me.wsj.apm.traffic

import android.app.Activity
import android.app.Application
import me.wsj.core.ITracker
import android.app.Application.ActivityLifecycleCallbacks
import android.net.TrafficStats
import android.os.Process
import android.util.Log
import me.wsj.core.BaseTracker
import me.wsj.core.extensions.noOpDelegate
import java.util.HashMap

class TrafficTracker private constructor() : BaseTracker<TrafficListener>(), ITracker {
    private val mHashMap = HashMap<Activity, TrafficInfo?>()
    private var mCurrentStats: Long = 0

    private val mActivityLifecycleCallbacks: ActivityLifecycleCallbacks =
        object : ActivityLifecycleCallbacks by noOpDelegate() {
            override fun onActivityStarted(activity: Activity) {
                markActivityStart(activity)
            }

            override fun onActivityPaused(activity: Activity) {
                markActivityPause(activity)
            }

            override fun onActivityDestroyed(activity: Activity) {
                markActivityDestroy(activity)
            }
        }

    fun markActivityStart(activity: Activity) {
        if (mHashMap[activity] == null) {
            val item = TrafficInfo()
            item.activity = activity
            item.sequence = sSequence++
            item.trafficCost = 0
            item.activityName = activity.javaClass.simpleName
            mHashMap[activity] = item
        }
        mCurrentStats = TrafficStats.getUidRxBytes(Process.myUid())
    }

    // 以pause为中断点
    fun markActivityPause(activity: Activity) {
        val item = mHashMap[activity]
        if (item != null) {
            item.trafficCost += TrafficStats.getUidRxBytes(Process.myUid()) - mCurrentStats
        }
    }

    // 防止泄露
    fun markActivityDestroy(activity: Activity) {
        val item = mHashMap[activity]
        if (item != null) {
            for (trafficListener in listeners) {
                trafficListener.getTrafficStats(item.activity, item.trafficCost)
                mHashMap.remove(activity)
            }
            item.activity = null
        }
    }

    override fun startTrack(application: Application) {
        application.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
    }

    override fun pauseTrack(application: Application) {}

    override fun destroy(application: Application) {
        application.unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
    }

    companion object {
        private var sSequence = 0

        val instance: TrafficTracker by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            TrafficTracker()
        }
        /*
        @Volatile
        private var sInstance: TrafficTracker? = null
        val instance: TrafficTracker?
            get() {
                if (sInstance == null) {
                    synchronized(TrafficTracker::class.java) {
                        if (sInstance == null) {
                            sInstance = TrafficTracker()
                        }
                    }
                }
                return sInstance
            }*/
    }
}