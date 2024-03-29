package me.wsj.instrument.activitythread;

import android.content.res.Resources;
import android.os.Build;
import android.os.DeadSystemException;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.AndroidRuntimeException;
import android.view.WindowManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import me.wsj.core.utils.Looger;
import me.wsj.core.utils.ReflectionUtils;
import me.wsj.instrument.Intrinsics;

/**
 * Callback used to catch ActivityThread exception caused by system.
 *
 * @author neighbWang
 */
class ActivityThreadCallback implements Handler.Callback {

    private static final String LOADED_APK_GET_ASSETS = "android.app.LoadedApk.getAssets";

    private static final String ASSET_MANAGER_GET_RESOURCE_VALUE = "android.content.res.AssetManager.getResourceValue";

    private static final String[] SYSTEM_PACKAGE_PREFIXES = {
            "java.",
            "android.",
            "androidx.",
            "dalvik.",
            "com.android.",
    };

    private final Handler mHandler;

    private final Handler.Callback mDelegate;

    private final Set<String> mIgnorePackages;

    /**
     * @param ignorePackages packages to ignore
     */
    public ActivityThreadCallback(final String[] ignorePackages) {
        final Set<String> packages = new HashSet<>(Arrays.asList(SYSTEM_PACKAGE_PREFIXES));
        for (final String pkg : ignorePackages) {
            if (null == pkg) {
                continue;
            }
            packages.add(pkg.endsWith(".") ? pkg : (pkg + "."));
        }
        packages.add(getClass().getPackage().getName() + ".");
        this.mIgnorePackages = Collections.unmodifiableSet(packages);
        this.mHandler = getHandler(getActivityThread());
        this.mDelegate = ReflectionUtils.getFieldValue(this.mHandler, "mCallback");
    }

    @Override
    public final boolean handleMessage(final Message msg) {
        try {
            if (null != mDelegate) {
                return this.mDelegate.handleMessage(msg);
            }

            if (null != this.mHandler) {
                this.mHandler.handleMessage(msg);
            }
        } catch (final NullPointerException e) {
            if (hasStackTraceElement(e, ASSET_MANAGER_GET_RESOURCE_VALUE, LOADED_APK_GET_ASSETS)) {
                // usually occurred after app upgrade installation, it seems like a system bug
                return abort(e);
            }
            rethrowIfCausedByUser(e);
        } catch (final SecurityException
                | IllegalArgumentException
                | AndroidRuntimeException
                | Resources.NotFoundException
                | WindowManager.BadTokenException e) {
            rethrowIfCausedByUser(e);
        } catch (final RuntimeException e) {
            final Throwable cause = e.getCause();
            if (((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) && isCausedBy(cause, DeadSystemException.class))
                    // usually occurred after app upgrade installation, it seems like a system bug
                    || (isCausedBy(cause, NullPointerException.class) && hasStackTraceElement(e, LOADED_APK_GET_ASSETS))) {
                return abort(e);
            }
            rethrowIfCausedByUser(e);
        } catch (final Error e) {
            rethrowIfCausedByUser(e);
            return abort(e);
        }

        return true;
    }

    private void rethrowIfCausedByUser(final RuntimeException e) {
        if (isCausedByUser(e)) {
            for (Throwable cause = e; null != cause; cause = cause.getCause()) {
                Intrinsics.sanitizeStackTrace(cause, getClass());
            }
            throw e;
        }
    }

    private void rethrowIfCausedByUser(final Error e) {
        if (isCausedByUser(e)) {
            for (Throwable cause = e; null != cause; cause = cause.getCause()) {
                Intrinsics.sanitizeStackTrace(cause, getClass());
            }
            throw e;
        }
    }

    private boolean isCausedByUser(final Throwable t) {
        if (null == t) {
            return false;
        }

        for (Throwable cause = t; null != cause; cause = cause.getCause()) {
            for (final StackTraceElement element : cause.getStackTrace()) {
                if (isUserStackTrace(element)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isUserStackTrace(final StackTraceElement element) {
        final String name = element.getClassName();
        for (final String prefix : this.mIgnorePackages) {
            if (name.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasStackTraceElement(final Throwable t, final String... traces) {
        return hasStackTraceElement(t, new HashSet<>(Arrays.asList(traces)));
    }

    private static boolean hasStackTraceElement(final Throwable t, final Set<String> traces) {
        if (null == t || null == traces || traces.isEmpty()) {
            return false;
        }

        for (final StackTraceElement element : t.getStackTrace()) {
            if (traces.contains(element.getClassName() + "." + element.getMethodName())) {
                return true;
            }
        }

        return hasStackTraceElement(t.getCause(), traces);
    }

    @SafeVarargs
    private static boolean isCausedBy(final Throwable t, final Class<? extends Throwable>... causes) {
        return isCausedBy(t, new HashSet<>(Arrays.asList(causes)));
    }

    private static boolean isCausedBy(final Throwable t, final Set<Class<? extends Throwable>> causes) {
        if (null == t) {
            return false;
        }

        if (causes.contains(t.getClass())) {
            return true;
        }

        return isCausedBy(t.getCause(), causes);
    }

    private static boolean abort(final Throwable t) {
        final int pid = Process.myPid();
        final String msg = "Process " + pid + " is going to be killed";

        if (null != t) {
            Looger.w(msg + t.toString());
        } else {
            Looger.w(msg);
        }

        Process.killProcess(pid);
        System.exit(10);
        return true;
    }

    private static Handler getHandler(final Object thread) {
        Handler handler;

        if (null == thread) {
            return null;
        }

        if (null != (handler = ReflectionUtils.getFieldValue(thread, "mH"))) {
            return handler;
        }

        if (null != (handler = ReflectionUtils.invokeMethod(thread, "getHandler"))) {
            return handler;
        }

        try {
            if (null != (handler = ReflectionUtils.getFieldValue(thread, Class.forName("android.app.ActivityThread$H")))) {
                return handler;
            }
        } catch (final ClassNotFoundException e) {
            Looger.w("Main thread handler is inaccessible" + e.toString());
        }

        return null;
    }

    private static Object getActivityThread() {
        Object thread = null;

        try {
            thread = ReflectionUtils.getStaticFieldValue(Class.forName("android.app.ActivityThread"), "sCurrentActivityThread");
        } catch (final Throwable t2) {
            Looger.w("ActivityThread.sCurrentActivityThread is inaccessible" + t2.toString());
        }

        if (null != thread) {
            return thread;
        }

        Looger.w("ActivityThread instance is inaccessible");
        return null;
    }

    boolean hook() {
        if (null != this.mDelegate) {
            Looger.w("ActivityThread.mH.mCallback has already been hooked by " + this.mDelegate);
        }
        return ReflectionUtils.setFieldValue(this.mHandler, "mCallback", this);
    }
}
