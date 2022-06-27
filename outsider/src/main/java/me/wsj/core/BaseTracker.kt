package me.wsj.core

import java.util.*

open class BaseTracker<T> {

    protected val TAG = javaClass.toString()

    protected val listeners: MutableList<T> = LinkedList()

    fun addTrackerListener(leakListener: T) {
        listeners.add(leakListener)
    }

    fun removeTrackerListener(leakListener: T) {
        listeners.remove(leakListener)
    }
}