package me.wsj.traffic;

import android.app.Activity;
import android.app.Application;
import android.net.TrafficStats;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.wsj.core.ITracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TrafficCheck implements ITracker,Application.ActivityLifecycleCallbacks {

    private static volatile TrafficCheck sInstance = null;
    private HashMap<Activity, Traffic> mHashMap = new HashMap<>();
    private long mCurrentStats;
    private static int sSequence;
    private List<TrafficListener> mStatsListeners = new ArrayList<>();


    public void addTackTrafficStatsListener(TrafficListener listener) {
        mStatsListeners.add(listener);
    }

    public void removeTrackTrafficStatsListener(TrafficListener listener) {

        mStatsListeners.remove(listener);
    }

    private TrafficCheck() {
    }

    public static TrafficCheck getInstance() {
        if (sInstance == null) {
            synchronized (TrafficCheck.class) {
                if (sInstance == null) {
                    sInstance = new TrafficCheck();
                }
            }
        }
        return sInstance;
    }

    public void markActivityStart(Activity activity) {
        if (mHashMap.get(activity) == null) {
            Traffic item = new Traffic();
            item.activity = activity;
            item.sequence = sSequence++;
            item.trafficCost = 0;
            item.activityName = activity.getClass().getSimpleName();
            mHashMap.put(activity, item);
        }
        mCurrentStats = TrafficStats.getUidRxBytes(android.os.Process.myUid());
    }

    //  以pause为中断点
    public void markActivityPause(Activity activity) {
        Traffic item = mHashMap.get(activity);
        if (item != null) {
            item.trafficCost += TrafficStats.getUidRxBytes(android.os.Process.myUid()) - mCurrentStats;
        }
    }

    //   防止泄露
    public void markActivityDestroy(Activity activity) {
        Traffic item = mHashMap.get(activity);
        if (item != null) {
            for (TrafficListener trafficListener : mStatsListeners) {
                trafficListener.getTrafficStats(item.activity, item.trafficCost);
                mHashMap.remove(activity);
            }
            item.activity = null;
        }
    }


    @Override
    public void destroy(Application application) {

    }

    @Override
    public void startTrack(Application application) {
        //todo add 接口

    }

    @Override
    public void pauseTrack(Application application) {
        //todo remove 接口
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        markActivityStart(activity);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        markActivityPause(activity);
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        markActivityDestroy(activity);
    }
}
