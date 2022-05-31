package me.wsj.anrcheck

import android.os.Looper

class AnrError(
    private val stackTraceThrowable: StackTraceCollector.StackTraceThrowable,
    private val duration: Long
) : Error("Application Not Responding for at least $duration ms.", stackTraceThrowable) {

    override fun fillInStackTrace(): Throwable {
        stackTrace = emptyArray()
        return this
    }

    companion object {
        private fun threadTitle(thread: Thread): String {
            return "${thread.name} (state = ${thread.state})"
        }

        fun newMainInstance(duration: Long): AnrError {
            val mainThread = Looper.getMainLooper().thread
            val stackTraces = mainThread.stackTrace
            return AnrError(
                StackTraceCollector(
                    threadTitle(mainThread),
                    stackTraces
                ).StackTraceThrowable(null), duration
            )
        }

        fun newInstance(
            duration: Long,
            prefix: String,
            logThreadsWithoutStackTrace: Boolean
        ): AnrError {
            val mainThread = Looper.getMainLooper().thread
            val threadStackTraces =
                sortedMapOf<Thread, Array<StackTraceElement>>(ThreadComparator())
            for (entry in Thread.getAllStackTraces().entries) {
                if (entry.key == mainThread || entry.key.name.startsWith(prefix) && (logThreadsWithoutStackTrace || !entry.value.isNullOrEmpty())) {
                    threadStackTraces[entry.key] = entry.value
                }
            }
            /**
             * Sometimes main thread is not returned by {@link Thread#getAllStackTraces()} -- ensure that we have it.
             */
            if (!threadStackTraces.containsKey(mainThread)) {
                threadStackTraces[mainThread] = mainThread.stackTrace
            }
            var throwable: StackTraceCollector.StackTraceThrowable? = null
            for (entry in threadStackTraces.entries) {
                throwable = StackTraceCollector(threadTitle(entry.key), entry.value).StackTraceThrowable(throwable)
            }
            return AnrError(throwable!!, duration)
        }
    }
}