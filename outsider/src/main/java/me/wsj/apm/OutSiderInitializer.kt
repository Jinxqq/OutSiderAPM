package me.wsj.apm

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

class OutSiderInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        OutSider.init(context.applicationContext as Application)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}