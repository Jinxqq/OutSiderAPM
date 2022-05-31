package me.wsj.mem;

import android.os.Debug;

public class MemoryInfo {
    public String procName;
    public AppMemory appMemory;
    public SystemMemory systemMemoryInfo;
    public String display;
    public int activityCount;

    public static class AppMemory {
        public long dalvikPss;//java占用内存大小
        public long nativePss;//前进程总私有已用内存大小
        public long totalPss;//当前进程总内存大小
        public Debug.MemoryInfo mMemoryInfo;
    }

    public static class SystemMemory {
        public long availMem;
        public boolean lowMemory;
        public long threshold;
        public long totalMem;
    }
}
