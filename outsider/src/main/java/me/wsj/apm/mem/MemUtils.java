package me.wsj.apm.mem;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Debug;
import android.text.TextUtils;

import me.wsj.core.utils.ProcessUtils;

public class MemUtils {

    private static String display;

    public static MemoryInfo collectMemoryInfo(Application application) {

        if (TextUtils.isEmpty(display)) {
            display = "" + application.getResources().getDisplayMetrics().widthPixels + "*" + application.getResources().getDisplayMetrics().heightPixels;
        }
        ActivityManager activityManager = (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);
        // 系统内存
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        SystemMemory systemMemory = new SystemMemory();
        systemMemory.setAvailMem(memoryInfo.availMem >> 20);
        systemMemory.setTotalMem(memoryInfo.totalMem >> 20);
        systemMemory.setLowMemory(memoryInfo.lowMemory);
        systemMemory.setThreshold(memoryInfo.threshold >> 20);

        //java内存
        Runtime rt = Runtime.getRuntime();

        //进程Native内存

        AppMemory appMemory = new AppMemory();
        Debug.MemoryInfo debugMemoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(debugMemoryInfo);
        appMemory.setNativePss(debugMemoryInfo.nativePss >> 10);
        appMemory.setDalvikPss(debugMemoryInfo.dalvikPss >> 10);
        appMemory.setTotalPss(debugMemoryInfo.getTotalPss() >> 10);
        appMemory.setMMemoryInfo(debugMemoryInfo);

        MemoryInfo trackMemoryInfo = new MemoryInfo();
        trackMemoryInfo.setSystemMemoryInfo(systemMemory);
        trackMemoryInfo.setAppMemory(appMemory);


        trackMemoryInfo.setProcName(ProcessUtils.getCurrentProcessName());
        trackMemoryInfo.setDisplay(display);
        trackMemoryInfo.setActivityCount(ActivityStack.getInstance().getSize());
        return trackMemoryInfo;
    }

    private static void mallocBigMem() {
        byte[] leakHelpBytes = new byte[4 * 1024 * 1024];
        for (int i = 0; i < leakHelpBytes.length; i += 1024) {
            leakHelpBytes[i] = 1;
        }
    }

    public static void gcTragger(){
        mallocBigMem();
        Runtime.getRuntime().gc();
    }
}
