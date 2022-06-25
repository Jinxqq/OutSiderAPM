package me.wsj.performance

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import me.wsj.apm.anr.AnrError
import me.wsj.apm.anr.AnrInterceptor
import me.wsj.apm.anr.AnrListener
import me.wsj.apm.anr.AnrMonitor
import me.wsj.apm.jank.BlockTracker
import me.wsj.batterycheck.BatteryStatsTracker
import me.wsj.performance.utils.Looger
import me.wsj.traffic.TrafficCheck
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectOutputStream

class APMApplication : Application() {
    var duration = 4L
    val anrMonitor = AnrMonitor(3000)
    val silentAnrListener = object : AnrListener {
        override fun onAppNotResponding(error: AnrError) {
            Log.d("anr-log", "onAppNotResponding", error)
        }
    }

    override fun onCreate() {
        super.onCreate()
        anrMonitor.setIgnoreDebugger(true)
            .setReportAllThreads()
            .setAnrListener(object : AnrListener {
                override fun onAppNotResponding(error: AnrError) {
                    Log.i("tag","onAppNotResponding")
                    try {
                        ObjectOutputStream(ByteArrayOutputStream()).writeObject(error)
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                    Log.i("tag","Anr Error was successfully serialized")
                }
            }).setAnrInterceptor(object : AnrInterceptor {
                override fun intercept(duration: Long): Long {
                    val ret = this@APMApplication.duration - duration
                    if (ret > 0) {
                        Log.i("tag",
                            "Intercepted ANR that is too short ($duration ms), postponing for $ret ms."
                        )
                    }
                    return ret
                }
            })

        ProcessLifecycleOwner.get().lifecycle.addObserver(anrMonitor)

        //todo

        BatteryStatsTracker.getInstance().addBatteryListener {
            Looger.e("${it.toString()}")
        }
        BatteryStatsTracker.getInstance().startTrack(this)

        TrafficCheck.instance?.startTrack(this)

        BlockTracker.getInstance().startTrack(this)
//        TrafficTracker().addObserver(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        anrMonitor.onAppTerminate();
    }
}