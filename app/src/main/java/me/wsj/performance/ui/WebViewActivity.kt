package me.wsj.performance.ui

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import me.wsj.performance.R


/*
class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val webView = findViewById<WebView>(R.id.webView)
//        webView.loadUrl("https://gitstar.com.cn/")
        val url = "https://www.baidu.com/"
        webView.loadUrl(url)

        Log.e("WebViewActivity", "start: " + Thread.currentThread())

        webView.settings.apply {
            javaScriptEnabled = true
        }

        // todo 在这里织入addJavascriptInterface
        webView.webViewClient = MyWebViewClient()
//        webView.addJavascriptInterface(JSBridge(), "android_apm");
    }
}*/
