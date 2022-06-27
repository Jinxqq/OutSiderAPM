package me.wsj.apm.mem;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityStack {
    private static volatile ActivityStack sInstance = null;
    private List<Activity> mActivities = new ArrayList<>();
    private volatile int mCurrentSate;

    private ActivityStack() {
    }

    public static ActivityStack getInstance() {
        if (sInstance == null) {
            synchronized (ActivityStack.class) {
                if (sInstance == null) {
                    sInstance = new ActivityStack();
                }
            }
        }
        return sInstance;
    }

    public void push(Activity activity) {
        mActivities.add(0, activity);
    }


    public int getSize() {
        return mActivities.size();
    }

    public void pop(Activity activity) {
        mActivities.remove(activity);
    }

    public void markStart() {
        mCurrentSate++;
    }

    public void markStop() {
        mCurrentSate--;
    }

    public Activity getTopActivity() {

        return mActivities.size() > 0 ? mActivities.get(0) : null;
    }

    public Activity getBottomActivity() {
        return mActivities.size() > 0 ? mActivities.get(mActivities.size() - 1) : null;
    }

    public boolean isInBackGround() {
        return mCurrentSate == 0;
    }
}
