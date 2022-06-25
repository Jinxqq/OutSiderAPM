package me.wsj.traffic

import android.app.Activity
import android.app.Application
import me.wsj.core.ITracker
import me.wsj.traffic.Traffic
import me.wsj.traffic.TrafficListener
import android.app.Application.ActivityLifecycleCallbacks
import me.wsj.traffic.TrafficCheck
import android.net.TrafficStats
import android.os.Bundle
import android.os.Process
import me.wsj.core.extensions.noOpDelegate
import java.util.ArrayList
import java.util.HashMap

class TrafficCheck private constructor() : ITracker {
    private val mHashMap = HashMap<Activity, Traffic?>()
    private var mCurrentStats: Long = 0
    private val mStatsListeners: MutableList<TrafficListener> = ArrayList()

    fun addTackTrafficStatsListener(listener: TrafficListener) {
        mStatsListeners.add(listener)
    }

    fun removeTrackTrafficStatsListener(listener: TrafficListener) {
        mStatsListeners.remove(listener)
    }

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
            val item = Traffic()
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
            for (trafficListener in mStatsListeners) {
                trafficListener.getTrafficStats(item.activity, item.trafficCost)
                mHashMap.remove(activity)
            }
            item.activity = null
        }
    }

    override fun destroy(application: Application) {
        application.unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
    }

    override fun startTrack(application: Application) {
        application.registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
    }

    override fun pauseTrack(application: Application) {}

    companion object {
        @Volatile
        private var sInstance: TrafficCheck? = null
        private var sSequence = 0
        val instance: TrafficCheck?
            get() {
                if (sInstance == null) {
                    synchronized(TrafficCheck::class.java) {
                        if (sInstance == null) {
                            sInstance = TrafficCheck()
                        }
                    }
                }
                return sInstance
            }
    }
}