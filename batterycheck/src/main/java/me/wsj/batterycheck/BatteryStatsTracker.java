package me.wsj.batterycheck;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.wsj.core.ITracker;

import java.util.ArrayList;
import java.util.List;

public class BatteryStatsTracker implements ITracker,Application.ActivityLifecycleCallbacks{
    private static BatteryStatsTracker sInstance;
    private Handler mHandler;
    private String display;
    private int mStartPercent;
    private HandlerThread handlerThread;


    private BatteryStatsTracker() {
        handlerThread = new HandlerThread("BatteryStats",Thread.NORM_PRIORITY);
        mHandler = new Handler(handlerThread.getLooper());
    }

    public static BatteryStatsTracker getInstance() {
        if (sInstance == null) {
            synchronized (BatteryStatsTracker.class) {
                if (sInstance == null) {
                    sInstance = new BatteryStatsTracker();
                }
            }
        }
        return sInstance;
    }





    @Override
    public void destroy(Application application) {

    }

    @Override
    public void startTrack(Application application) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
    }

    @Override
    public void pauseTrack(Application application) {

    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = activity.getApplication().registerReceiver(null, filter);
                mStartPercent = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            }
        });
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mListeners.size() > 0) {
                    BatteryInfo batteryInfo = getBatteryInfo(activity.getApplication());
                    for (IBatteryListener listener : mListeners) {
                        listener.onBatteryCost(batteryInfo);
                    }
                }

            }
        });
    }

    private BatteryInfo getBatteryInfo(Application application) {
        if (TextUtils.isEmpty(display)) {
            display = "" + application.getResources().getDisplayMetrics().widthPixels + "*" + application.getResources().getDisplayMetrics().heightPixels;
        }
        BatteryInfo batteryInfo = new BatteryInfo();
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = application.registerReceiver(null, filter);
            int status = batteryStatus.getIntExtra("status", 0);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryInfo.charging = isCharging;
            batteryInfo.cost = isCharging ? 0 : mStartPercent - batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            batteryInfo.duration += (SystemClock.uptimeMillis() - LauncherHelpProvider.sStartUpTimeStamp) / 1000;
            batteryInfo.screenBrightness = getSystemScreenBrightnessValue(application);
            batteryInfo.display = display;
            batteryInfo.total = scale;
            Log.v("Battery", "total " + batteryInfo.total + " 用时间 " + batteryInfo.duration / 1000 + " 耗电  " + batteryInfo.cost);
        } catch (Exception e) {

        }

        return batteryInfo;
    }

    public int getSystemScreenBrightnessValue(Application application) {
        ContentResolver contentResolver = application.getContentResolver();
        int defVal = 125;
        return Settings.System.getInt(contentResolver,
                Settings.System.SCREEN_BRIGHTNESS, defVal);
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    private List<IBatteryListener> mListeners = new ArrayList<>();

    public void addBatteryListener(IBatteryListener listener) {
        mListeners.add(listener);
    }

    public void removeBatteryListener(IBatteryListener listener) {
        mListeners.remove(listener);
    }

    public interface IBatteryListener {
        void onBatteryCost(BatteryInfo batteryInfo);

    }
}
