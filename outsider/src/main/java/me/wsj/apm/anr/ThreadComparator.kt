package me.wsj.apm.anr

import android.os.Looper

/***
 * 线程池的判断 mainThread priority first other thread order by ThreadName
 */
class ThreadComparator:Comparator<Thread> {
    override fun compare(p0: Thread?, p1: Thread?): Int {
        if (p0 == p1) return 0
        if (p0 == Looper.getMainLooper().thread) return 1
        if (p1 == Looper.getMainLooper().thread) return -1
        return p1!!.name.compareTo(p0!!.name)
    }
}