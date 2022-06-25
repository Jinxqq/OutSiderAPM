package me.wsj.apm.jank;

import android.os.Build;
import android.util.Log;
import android.view.Choreographer;
import android.view.SurfaceView;

public class FpsCheck {
    private long mStartFrameTime = 0;
    private long mStartFrameCount = 0;
    private static final long MONITOR_INTERVAL = 160L;
    private static final long MONITOR_INTERVAL_NANOS = MONITOR_INTERVAL*1000*1000;

    private void getFps(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
            return;
        }
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long timeNanos) {
                if(mStartFrameTime == 0){
                    mStartFrameTime = timeNanos;
                }

                float interval = (timeNanos - mStartFrameTime) / 1000000.0f;
                if(interval > MONITOR_INTERVAL){
                    double fps = (mStartFrameCount*1000L)/interval;
                    Log.e("tag","fps"+fps);
                    mStartFrameCount = 0;
                    mStartFrameTime = 0;
                }else{
                    ++mStartFrameCount;
                }
                Choreographer.getInstance().postFrameCallback(this);
            }
        });
    }

}
