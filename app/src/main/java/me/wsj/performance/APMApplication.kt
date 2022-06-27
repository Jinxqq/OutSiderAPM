package me.wsj.performance

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import me.wsj.apm.OutSider
import me.wsj.apm.anr.AnrError
import me.wsj.apm.anr.AnrInterceptor
import me.wsj.apm.anr.AnrListener
import me.wsj.apm.anr.AnrMonitor
import me.wsj.apm.battery.BatteryStatsTracker
import me.wsj.apm.jank.BlockTracker
import me.wsj.apm.traffic.TrafficTracker
import me.wsj.performance.utils.Looger
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectOutputStream

class APMApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}