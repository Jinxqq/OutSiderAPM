package me.wsj.performance.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import me.wsj.performance.R
import okhttp3.*
import java.io.IOException

class JankActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        sleepFor6s()
    }

    private fun sleepFor6s() {
        try {
            Thread.sleep(6L * 1000L)
            Log.d("MainActivity", "sleep finish")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}
