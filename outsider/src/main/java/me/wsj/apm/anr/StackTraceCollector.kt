package me.wsj.apm.anr

import java.io.Serializable

class StackTraceCollector(
    private val name: String,
    private val stackTraces: Array<StackTraceElement>
) : Serializable {
    inner class StackTraceThrowable(private val other: StackTraceThrowable?) :
        Throwable(name, other) {
        override fun fillInStackTrace(): Throwable {
            stackTrace = stackTraces
            return this
        }
    }
}