package me.wsj.apm.jank;

import android.view.Choreographer;

import me.wsj.apm.OutSiderKt;
import me.wsj.core.utils.Looger;

public class FpsCheck {
    private long mStartFrameTime = 0;
    private long mStartFrameCount = 0;
    private static final long MONITOR_INTERVAL = 160L;
    private static final long MONITOR_INTERVAL_NANOS = MONITOR_INTERVAL * 1000 * 1000;

    private void getFps() {
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long timeNanos) {
                if (mStartFrameTime == 0) {
                    mStartFrameTime = timeNanos;
                }

                float interval = (timeNanos - mStartFrameTime) / 1000000.0f;
                if (interval > MONITOR_INTERVAL) {
                    double fps = (mStartFrameCount * 1000L) / interval;
                    Looger.e(OutSiderKt.TAG, "fps" + fps);
                    mStartFrameCount = 0;
                    mStartFrameTime = 0;
                } else {
                    ++mStartFrameCount;
                }
                Choreographer.getInstance().postFrameCallback(this);
            }
        });
    }

}
