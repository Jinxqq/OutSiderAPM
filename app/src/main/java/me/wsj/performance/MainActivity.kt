package me.wsj.performance

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("MainActivity", "aaaaa")

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            startActivity(Intent(this, MainActivity2::class.java))
        }

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