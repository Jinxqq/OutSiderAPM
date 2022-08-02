package me.wsj.performance

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import me.wsj.performance.ui.FuncActivity
import me.wsj.performance.ui.NetworkActivity
import me.wsj.performance.ui.ThreadActivity
import me.wsj.performance.ui.WebViewActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            startActivity(Intent(this, FuncActivity::class.java))
        }

        findViewById<Button>(R.id.btnWebView).setOnClickListener {
            startActivity(Intent(this, WebViewActivity::class.java))
        }

        findViewById<Button>(R.id.btnNetwork).setOnClickListener {
            startActivity(Intent(this, NetworkActivity::class.java))
        }

        findViewById<Button>(R.id.btnNetwork).setOnClickListener {
            startActivity(Intent(this, NetworkActivity::class.java))
        }

        findViewById<Button>(R.id.tvThread).setOnClickListener {
            startActivity(Intent(this, ThreadActivity::class.java))
        }

//        Observable.create(ObservableOnSubscribe<String> {
//            it.onNext("123456")
//        }).subscribeOn(Schedulers.io())
//            .subscribe {
//                Log.e("Rxjava", "result: $it")
//            }
    }
}