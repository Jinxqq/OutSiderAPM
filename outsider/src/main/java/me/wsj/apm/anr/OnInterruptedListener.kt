package me.wsj.apm.anr

interface OnInterruptedListener {
    /**
     * Called when Anr-monitor Thread is interrupted.
     */
    fun onInterrupted(e: InterruptedException)
}