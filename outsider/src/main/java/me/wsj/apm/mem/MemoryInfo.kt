package me.wsj.apm.mem

import android.os.Debug

data class MemoryInfo(
    var procName: String? = null,
    var appMemory: AppMemory? = null,
    var systemMemoryInfo: SystemMemory? = null,
    var display: String? = null,
    var activityCount: Int = 0
)

data class AppMemory(
    //java占用内存大小
    var dalvikPss: Long = 0,
    //前进程总私有已用内存大小
    var nativePss: Long = 0,
    //当前进程总内存大小
    var totalPss: Long = 0,
    var mMemoryInfo: Debug.MemoryInfo? = null
)

data class SystemMemory(
    var availMem: Long = 0,
    var lowMemory: Boolean = false,
    var threshold: Long = 0,
    var totalMem: Long = 0
)