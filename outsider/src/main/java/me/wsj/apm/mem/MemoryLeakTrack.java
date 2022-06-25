package me.wsj.apm.mem;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.wsj.core.ActivityStack;
import me.wsj.core.ITracker;
import me.wsj.core.utils.ProcessUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class MemoryLeakTrack implements ITracker, Application.ActivityLifecycleCallbacks {

    private static volatile MemoryLeakTrack sInstance = null;
    private HandlerThread handlerThread = new HandlerThread("BatteryStats",Thread.NORM_PRIORITY);;


    private MemoryLeakTrack() {
    }

    public static MemoryLeakTrack getInstance() {
        if (sInstance == null) {
            synchronized (MemoryLeakTrack.class) {
                if (sInstance == null) {
                    sInstance = new MemoryLeakTrack();
                }
            }
        }
        return sInstance;
    }

    private Handler mHandler = new Handler(handlerThread.getLooper());
    private WeakHashMap<Activity, String> mActivityStringWeakHashMap = new WeakHashMap<>();

    @Override
    public void destroy(final Application application) {
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void startTrack(final Application application) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mMemoryListeners.size() > 0 && !ActivityStack.getInstance().isInBackGround()) {
                    MemoryInfo trackMemoryInfo = collectMemoryInfo(application);
                    for (ITrackMemoryListener listener : mMemoryListeners) {
                        listener.onCurrentMemoryCost(trackMemoryInfo);
                    }
                }
                mHandler.postDelayed(this, 30 * 1000);
            }
        }, 30 * 1000);
    }

    @Override
    public void pauseTrack(Application application) {

    }

    private Set<ITrackMemoryListener> mMemoryListeners = new HashSet<>();

    public void addOnMemoryLeakListener(ITrackMemoryListener leakListener) {
        mMemoryListeners.add(leakListener);
    }

    public void removeOnMemoryLeakListener(ITrackMemoryListener leakListener) {
        mMemoryListeners.remove(leakListener);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        //  退后台，GC 找LeakActivity
        if (!ActivityStack.getInstance().isInBackGround()) {
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mallocBigMem();
                Runtime.getRuntime().gc();
            }
        }, 1000);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!ActivityStack.getInstance().isInBackGround()) {
                        return;
                    }
                    //  分配大点内存促进GC
                    mallocBigMem();
                    Runtime.getRuntime().gc();
                    SystemClock.sleep(100);
                    System.runFinalization();
                    HashMap<String, Integer> hashMap = new HashMap<>();
                    for (Map.Entry<Activity, String> activityStringEntry : mActivityStringWeakHashMap.entrySet()) {
                        String name = activityStringEntry.getKey().getClass().getSimpleName();
                        Integer value = hashMap.get(name);
                        if (value == null) {
                            hashMap.put(name, 1);
                        } else {
                            hashMap.put(name, value + 1);
                        }
                    }
                    if (mMemoryListeners.size() > 0) {
                        for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
                            for (ITrackMemoryListener listener : mMemoryListeners) {
                                listener.onLeakActivity(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }, 10000);
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        mActivityStringWeakHashMap.put(activity, activity.getClass().getSimpleName());
    }

    public interface ITrackMemoryListener {

        void onLeakActivity(String activity, int count);

        void onCurrentMemoryCost(MemoryInfo trackMemoryInfo);
    }

    private static String display;

    private MemoryInfo collectMemoryInfo(Application application) {

        if (TextUtils.isEmpty(display)) {
            display = "" + application.getResources().getDisplayMetrics().widthPixels + "*" + application.getResources().getDisplayMetrics().heightPixels;
        }
        ActivityManager activityManager = (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);
        // 系统内存
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        MemoryInfo.SystemMemory systemMemory = new MemoryInfo.SystemMemory();
        systemMemory.availMem = memoryInfo.availMem >> 20;
        systemMemory.totalMem = memoryInfo.totalMem >> 20;
        systemMemory.lowMemory = memoryInfo.lowMemory;
        systemMemory.threshold = memoryInfo.threshold >> 20;

        //java内存
        Runtime rt = Runtime.getRuntime();

        //进程Native内存

        MemoryInfo.AppMemory appMemory = new MemoryInfo.AppMemory();
        Debug.MemoryInfo debugMemoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(debugMemoryInfo);
        appMemory.nativePss = debugMemoryInfo.nativePss >> 10;
        appMemory.dalvikPss = debugMemoryInfo.dalvikPss >> 10;
        appMemory.totalPss = debugMemoryInfo.getTotalPss() >> 10;
        appMemory.mMemoryInfo = debugMemoryInfo;

        MemoryInfo trackMemoryInfo = new MemoryInfo();
        trackMemoryInfo.systemMemoryInfo = systemMemory;
        trackMemoryInfo.appMemory = appMemory;



        trackMemoryInfo.procName = ProcessUtils.getCurrentProcessName();
        trackMemoryInfo.display = display;
        trackMemoryInfo.activityCount = ActivityStack.getInstance().getSize();
        return trackMemoryInfo;
    }

    private void mallocBigMem() {
        byte[] leakHelpBytes = new byte[4 * 1024 * 1024];
        for (int i = 0; i < leakHelpBytes.length; i += 1024) {
            leakHelpBytes[i] = 1;
        }
    }
}