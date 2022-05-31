package me.wsj.performance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.luge.performancedemo.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("MainActivity", "aaaaa")
        sleepFor8s()
    }


    private fun sleepFor8s() {
        try {
            Thread.sleep(8L * 1000L)
            Log.d("MainActivity", "sleep finish")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}