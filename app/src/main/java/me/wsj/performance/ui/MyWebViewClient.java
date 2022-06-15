package me.wsj.performance.ui;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import me.wsj.core.job.webview.JSBridge;

public class MyWebViewClient extends WebViewClient {

    @Override
    public void onPageFinished(WebView view, String url) {
        /*if (view.getProgress() == 100) {
            Log.e("MyWebViewClient", "onPageFinished..........");
            WebSettings var3 = view.getSettings();
            var3.setJavaScriptEnabled(true);
//            view.addJavascriptInterface(new JSBridge(), "android_apm");
            String var4 = String.format("javascript:%s.sendResource(\"%s\", JSON.stringify(window.performance.timing));", "android_apm", url);
            view.loadUrl(var4);
        }*/
        super.onPageFinished(view, url);
    }

}
