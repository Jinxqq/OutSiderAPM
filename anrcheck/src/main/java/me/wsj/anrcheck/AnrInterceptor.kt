package me.wsj.anrcheck

interface AnrInterceptor {
    fun intercept(duration: Long): Long
}