package me.wsj.batterycheck

data class BatteryInfo(
    var charging: Boolean = false,
    var activityName: String? = null,
    var cost: Float = 0f,
    var duration: Long = 0,
    var display: String? = null,
    var total: Int = 0,
    var voltage: Int = 0,
    var screenBrightness: Float = 0f
)