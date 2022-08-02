package me.wsj.performance.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import me.wsj.performance.MyTest;
import me.wsj.performance.R;
import me.wsj.performance.test.CibThreadPool;

public class FuncActivity extends AppCompatActivity {

    MyTest myTest = new MyTest();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

    }

    private void test1() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e("FuncActivity", "run finish " + myTest);*/
                Log.e("FuncActivity", "thread - new Runnable");
            }
        }).start();
    }

    private void test2() {
        new Thread(() -> {
            Log.e("FuncActivity", "thread - lambda");
        }).start();
    }

    private void test3() {
        new Thread(CibThreadPool::getInstance).start();
    }


}
