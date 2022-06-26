package me.wsj.apm.battery

interface IBatteryListener {
    fun onBatteryCost(batteryInfo: BatteryInfo?)
}