package me.wsj.performance.ui;

import android.app.ActivityManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import me.wsj.apm.thread.CibThreadPool;
import me.wsj.apm.thread.ShadowThread;
import me.wsj.apm.thread.ThreadTracker;
import me.wsj.performance.R;
import me.wsj.performance.TestClazz;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public
class ThreadActivity extends AppCompatActivity {

    me.wsj.performance.MyTest test = new me.wsj.performance.MyTest();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setTaskDescription(new ActivityManager.TaskDescription("aaaa", R.mipmap.fy));
        }

        findViewById(R.id.btnNewThread).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*CibThreadPool.getInstance().getIoTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        int i = 1 + 2;
                        i = i++;
                    }
                });
                test2();*/
            }
        });

        findViewById(R.id.btnUIThread).setOnClickListener(v ->
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        String a = "abc";
                    }
                }));

    }

    private void test() {
        new Thread(() -> {
            int i = 1 + 2;
            String a = "myTest: " + test;
        }).start();
    }

    private void test2() {
        new Thread(() -> {
            int i = 1 + 2;
            int j = 0;
            int k = i / j;
        }).start();
    }
}
