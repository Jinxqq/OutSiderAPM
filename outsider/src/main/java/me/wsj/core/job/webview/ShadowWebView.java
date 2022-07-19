package me.wsj.core.job.webview;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.util.Log;
import android.webkit.WebView;

import me.wsj.core.utils.Looger;
import me.wsj.core.utils.ReflectionUtils;

public class ShadowWebView {
    public static void preloadWebView(final Application app) {
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                Looper.myQueue().addIdleHandler(() -> {
                    startChromiumEngine(app);
                    return false;
                });
            } catch (final Throwable t) {
                Looger.e("Oops!" + t.toString());
            }
        });
    }

    private static void startChromiumEngine(final Context context) {
        try {
            final long t0 = SystemClock.uptimeMillis();
            final Object provider = ReflectionUtils.invokeStaticMethod(Class.forName("android.webkit.WebViewFactory"), "getProvider");
            ReflectionUtils.invokeMethod(provider, "startYourEngines", new Class[]{boolean.class}, new Object[]{true});
            Log.i(TAG, "Start chromium engine complete: " + (SystemClock.uptimeMillis() - t0) + " ms");
            if (Build.VERSION.SDK_INT >= 28) {
                String processName = Application.getProcessName();
                String packageName = context.getPackageName();
                if (!packageName.equals(processName)) {
                    WebView.setDataDirectorySuffix(processName);
                }
            }
        } catch (final Throwable t) {
            Log.e(TAG, "Start chromium engine error", t);
        }
    }
}
