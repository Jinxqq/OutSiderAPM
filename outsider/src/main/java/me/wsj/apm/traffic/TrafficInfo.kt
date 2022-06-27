package me.wsj.apm.traffic

import android.app.Activity

data class TrafficInfo(
    var activity: Activity? = null,
    var trafficCost: Long = 0,
    var sequence: Int = 0,
    var activityName: String? = null
)
