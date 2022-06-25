package me.wsj.traffic;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;

public class TrafficTracker extends Lifecycle {
    @Override
    public void addObserver(@NonNull LifecycleObserver observer) {
        
    }

    @Override
    public void removeObserver(@NonNull LifecycleObserver observer) {

    }

    @NonNull
    @Override
    public State getCurrentState() {
        return null;
    }
}
