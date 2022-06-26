package me.wsj.apm.mem

interface ITrackMemoryListener {
    fun onLeakActivity(activity: String, count: Int)

    fun onCurrentMemoryCost(trackMemoryInfo: MemoryInfo?)
}