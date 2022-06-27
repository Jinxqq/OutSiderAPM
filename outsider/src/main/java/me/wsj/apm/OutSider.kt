package me.wsj.apm

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import me.wsj.apm.anr.AnrError
import me.wsj.apm.anr.AnrInterceptor
import me.wsj.apm.anr.AnrListener
import me.wsj.apm.anr.AnrMonitor
import me.wsj.apm.battery.BatteryStatsTracker
import me.wsj.apm.jank.BlockTracker
import me.wsj.apm.mem.ITrackMemoryListener
import me.wsj.apm.mem.MemoryInfo
import me.wsj.apm.mem.MemoryTracker
import me.wsj.apm.thread.ThreadTracker
import me.wsj.apm.traffic.TrafficListener
import me.wsj.apm.traffic.TrafficTracker

const val TAG = "OutSider"

class OutSider(val app: Application) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {//app is created
                onAppCreate()
            }
            Lifecycle.Event.ON_START -> {
                onAppStart()
            }
            Lifecycle.Event.ON_STOP -> {
            }
            Lifecycle.Event.ON_DESTROY -> {//no activities in stack
                onAppDestory()
                Log.e(TAG, "ON_DESTROY")
            }
        }
    }

    fun onAppCreate() {
        Log.e(TAG, "onAppCreate")
        // 线程
        ThreadTracker.instance.startTrack(app)

        // 内存
        MemoryTracker.instance.addTrackerListener(object : ITrackMemoryListener {
            override fun onLeakActivity(activity: String, count: Int) {
                Log.e(TAG, "onLeakActivity: " + activity + " $count")
            }

            override fun onCurrentMemoryCost(trackMemoryInfo: MemoryInfo?) {
                Log.e(TAG, "CurrentMemoryCost: " + trackMemoryInfo.toString())
            }
        })
        MemoryTracker.instance.startTrack(app)

        // 流量
        TrafficTracker.instance.addTrackerListener(object : TrafficListener {
            override fun getTrafficStats(activity: Activity?, value: Long) {
                Log.e(TAG, "$activity traffic cost: $value")
            }
        })
        TrafficTracker.instance.startTrack(app)

        // 电量
        BatteryStatsTracker.instance.startTrack(app)
        // 卡顿
        BlockTracker.instance.startTrack(app)
        // anr
        AnrMonitor.instance.setIgnoreDebugger(true)
            .setReportAllThreads()
            .setAnrListener(object : AnrListener {
                override fun onAppNotResponding(error: AnrError) {
                    Log.e(TAG, "onAppNotResponding: " + error.toString())
                }
            }).setAnrInterceptor(object : AnrInterceptor {
                override fun intercept(duration: Long): Long {
                    val ret = 4L - duration
                    if (ret > 0) {
                        Log.i(
                            TAG,
                            "Intercepted ANR that is too short ($duration ms), postponing for $ret ms."
                        )
                    }
                    return ret
                }
            })
        AnrMonitor.instance.startTrack(app)
    }

    fun onAppStart() {
        Log.e(TAG, "onAppStart")
    }

    fun onAppDestory() {
        Log.e(TAG, "onAppStop")
        // 线程
        ThreadTracker.instance.destroy(app)
        // 内存
        MemoryTracker.instance.destroy(app)
        // 流量
        TrafficTracker.instance.destroy(app)
        // 电量
        BatteryStatsTracker.instance.destroy(app)
        // 卡顿
        BlockTracker.instance.destroy(app)
        // anr
        AnrMonitor.instance.destroy(app)
    }

    companion object {
        /*val instance: OutSider by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            OutSider()
        }*/

        fun init(app: Application) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(OutSider(app))
        }
    }
}