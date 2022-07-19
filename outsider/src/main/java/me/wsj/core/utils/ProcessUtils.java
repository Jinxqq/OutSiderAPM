package me.wsj.core.utils;

import android.app.Application;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            sProcessName = Application.getProcessName();
            return sProcessName;
        } else {
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

    public static String shellExec(String cmd) {
        Runtime mRuntime = Runtime.getRuntime();
        try {
            //Process中封装了返回的结果和执行错误的结果
            Process mProcess = mRuntime.exec(cmd);
            BufferedReader mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            StringBuffer mRespBuff = new StringBuffer();
            char[] buff = new char[1024];
            int ch = 0;
            while ((ch = mReader.read(buff)) != -1) {
                mRespBuff.append(buff, 0, ch);
            }
            mReader.close();
            Looger.i("nioTag2", "执行shell2脚本成功 " + mRespBuff.toString());//结果
            return mRespBuff.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Looger.i("nioTag2", "执行shell2脚本失败");
        }
        return null;
    }
}
