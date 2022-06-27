package me.wsj.apm.battery

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import me.wsj.apm.traffic.TrafficListener
import me.wsj.core.BaseTracker
import me.wsj.core.ITracker
import me.wsj.core.extensions.noOpDelegate

class BatteryStatsTracker private constructor() : BaseTracker<IBatteryListener>(), ITracker {
    private val mHandler: Handler
    private var display: String? = null
    private var mStartPercent = 0
    private val handlerThread: HandlerThread

    init {
        handlerThread = HandlerThread("OutSider-Battery", Thread.NORM_PRIORITY)
        handlerThread.start()
        mHandler = Handler(handlerThread.looper)
    }

    override fun startTrack(application: Application) {
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    override fun pauseTrack(application: Application) {
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    override fun destroy(application: Application) {
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
        handlerThread.quit()
    }

    private val activityLifecycleCallbacks: ActivityLifecycleCallbacks =
        object : ActivityLifecycleCallbacks by noOpDelegate() {
            override fun onActivityStarted(activity: Activity) {
                mHandler.post {
                    val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                    val batteryStatus = activity.application.registerReceiver(null, filter)
                    mStartPercent = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                }
            }

            override fun onActivityStopped(activity: Activity) {
                mHandler.post {
                    val batteryInfo =
                        getBatteryInfo(activity.application, activity.componentName.className)
                    Log.e(TAG, batteryInfo.toString())
                    if (listeners.size > 0) {
                        for (listener in listeners) {
                            listener.onBatteryCost(batteryInfo)
                        }
                    }
                }
            }
        }

    private fun getBatteryInfo(application: Application, activityName: String): BatteryInfo {
        if (TextUtils.isEmpty(display)) {
            display = application.resources.displayMetrics.widthPixels.toString() + "*" + application.resources.displayMetrics.heightPixels
        }
        val batteryInfo = BatteryInfo()
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = application.registerReceiver(null, filter)
            val status = batteryStatus!!.getIntExtra("status", 0)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            batteryInfo.charging = isCharging
            val coast: Float = if (isCharging) 0F else
                mStartPercent - batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1).toFloat()
            batteryInfo.cost = coast
            val duration =
                batteryInfo.duration + (SystemClock.uptimeMillis() - LauncherHelpProvider.sStartUpTimeStamp) / 1000
            batteryInfo.duration = duration
            batteryInfo.screenBrightness = getSystemScreenBrightnessValue(application).toFloat()
            batteryInfo.display = display
            batteryInfo.total = scale
            batteryInfo.activityName = activityName
//            Log.v("Battery", "total " + batteryInfo.total + " 用时间 " + batteryInfo.duration / 1000 + " 耗电  " + batteryInfo.cost);
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
        return batteryInfo
    }

    /**
     * 获取屏幕亮度
     */
    private fun getSystemScreenBrightnessValue(application: Application): Int {
        val contentResolver = application.contentResolver
        val defVal = 125
        return Settings.System.getInt(
            contentResolver,
            Settings.System.SCREEN_BRIGHTNESS, defVal
        )
    }

    companion object {
        val instance: BatteryStatsTracker by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { BatteryStatsTracker() }
    }
}