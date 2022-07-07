package me.wsj.core.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.FileInputStream;

import me.wsj.apm.OutSiderKt;

/**
 * @author OutSiderAPM
 */
public class ProcessUtils {

    private static String sProcessName = null;

    /**
     * 返回当前的进程名
     *
     * @return
     */
    public static String getCurrentProcessName() {
        if (TextUtils.isEmpty(sProcessName)) {
            sProcessName = getCurrentProcessNameInternal();
        }
        return sProcessName;
    }

    private static String getCurrentProcessNameInternal() {
        FileInputStream in = null;
        try {
            String fn = "/proc/self/cmdline";
            in = new FileInputStream(fn);
            byte[] buffer = new byte[256];
            int len = 0;
            int b;
            while ((b = in.read()) > 0 && len < buffer.length) {
                buffer[len++] = (byte) b;
            }
            if (len > 0) {
                String s = new String(buffer, 0, len, "UTF-8");
                return s;
            }
        } catch (Throwable e) {
            Looger.d(OutSiderKt.TAG, "getCurrentProcessName: got exception: " + Log.getStackTraceString(e));
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable e) {
                    Looger.d(OutSiderKt.TAG, "getCurrentProcessName: got exception: " + Log.getStackTraceString(e));
                }
            }
        }
        return null;
    }
}
