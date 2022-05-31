package me.wsj.anrcheck

interface OnInterruptedListener {
    /**
     * Called when Anr-monitor Thread is interrupted.
     */
    fun onInterrupted(e: InterruptedException)
}