package me.wsj.performance

import me.wsj.core.utils.Looger

class MyTest {
    private fun test() {
//        Log.e("MainActivity", "aaaaa")
//        Looger.e("test", "123456")
        Thread {
            Looger.e("test", "123456")
        }.start()
    }
}