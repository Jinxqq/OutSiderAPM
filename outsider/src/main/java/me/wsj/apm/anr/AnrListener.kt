package me.wsj.apm.anr

interface AnrListener {
    fun onAppNotResponding(error: AnrError)
}