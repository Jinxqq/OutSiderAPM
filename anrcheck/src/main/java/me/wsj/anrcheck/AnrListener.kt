package me.wsj.anrcheck

interface AnrListener {
    fun onAppNotResponding(error: AnrError)
}