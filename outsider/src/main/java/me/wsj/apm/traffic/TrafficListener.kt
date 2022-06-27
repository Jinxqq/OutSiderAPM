package me.wsj.apm.traffic

import android.app.Activity

interface TrafficListener {
    //获取流量
    fun getTrafficStats(activity: Activity?, value: Long)
}