package me.wsj.performance.ui;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import me.wsj.performance.R;

public
class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        WebView webView = findViewById(R.id.webView);
//        webView.loadUrl("https://gitstar.com.cn/")
        String url = "https://www.baidu.com/";
        webView.loadUrl(url);
        webView.getSettings().setJavaScriptEnabled(true);

        // 在这里织入addJavascriptInterface
        webView.setWebViewClient(new MyWebViewClient());
    }
}
