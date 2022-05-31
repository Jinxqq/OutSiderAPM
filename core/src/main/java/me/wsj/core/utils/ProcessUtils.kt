package me.wsj.core.utils

import android.app.ActivityManager
import android.content.Context

class ProcessUtils {
    /**
     * 通过ActivityManager获取到进程名字
     */
    companion object{
        fun getCurrentProcessName(context: Context):String?{
            var myPid = android.os.Process.myPid()
            var activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            var runningAppProcesses = activityManager.runningAppProcesses
            if (!runningAppProcesses.isNullOrEmpty()){
                runningAppProcesses.forEach {
                    if (it.pid == myPid){
                        return it.processName
                    }
                }
            }
            return null
        }
    }

}