package me.wsj.performance.utils;

import android.util.Log;

import java.util.Locale;

public class Looger {
    private static boolean DEBUG = true;
    public static final String TAG = "wsjLib";

    public static int v(String tag, String msg) {
        if (DEBUG) {
            Log.v(tag, buildMessage(msg));
        }
        return 0;
    }

    public static int i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, buildMessage(msg));
        }
        return 0;
    }

    public static int d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, buildMessage(msg));
        }
        return 0;
    }

    public static int e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, buildMessage(msg));
        }
        return 0;
    }

    public static int i(String msg) {
        if (DEBUG) {
            Log.i(TAG, buildMessage(msg));
        }
        return 0;
    }

    public static int d(String msg) {
        if (DEBUG) {
            Log.d(TAG, buildMessage(msg));
        }
        return 0;
    }

    public static int e(String msg) {
        if (DEBUG) {
            Log.e(TAG, buildMessage(msg));
        }
        return 0;
    }

    /**
     * 构建信息，用于创建自定义的Log信息结构
     *
     * @param msg Log信息
     * @return 整个创建好的信息内容
     */
    private static String buildMessage(String msg) {
        StackTraceElement targetStackTraceElement = getTargetStackTraceElement();

        return String.format(Locale.US, "%s -->%s",
                "(" + targetStackTraceElement.getFileName() + ":"
                        + targetStackTraceElement.getLineNumber() + ")", msg);
    }

    private static StackTraceElement getTargetStackTraceElement() {
        // find the target invoked method
        StackTraceElement targetStackTrace = null;
        boolean shouldTrace = false;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            boolean isLogMethod = stackTraceElement.getClassName().equals(Looger.class.getName());
            if (shouldTrace && !isLogMethod) {
                targetStackTrace = stackTraceElement;
                break;
            }
            shouldTrace = isLogMethod;
        }
        return targetStackTrace;
    }
}
