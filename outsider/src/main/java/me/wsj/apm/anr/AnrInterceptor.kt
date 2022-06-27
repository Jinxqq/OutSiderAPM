package me.wsj.apm.anr

interface AnrInterceptor {
    fun intercept(duration: Long): Long
}