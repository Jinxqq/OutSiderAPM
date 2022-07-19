package me.wsj.apm

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.startup.Initializer
import me.wsj.core.utils.Looger

class OutSiderInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = Application.getProcessName()
            val packageName = context.packageName
            Looger.e("processName: $processName")
            Looger.e("packageName: $packageName")
        }
        // todo 多进程情况
        OutSider.init(context.applicationContext as Application)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}