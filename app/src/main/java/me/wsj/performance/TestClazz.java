package me.wsj.performance;


import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.Executors;

import me.wsj.performance.anno.MyAnno;
import me.wsj.performance.test.CibThreadPool;

public class TestClazz {

    /*private void test3() {
        Executors.newFixedThreadPool(1);
        Executors.newSingleThreadExecutor();
        Executors.newCachedThreadPool();
//        Executors.newScheduledThreadPool();

        CibThreadPool.getInstance().getIoTasks().execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }*/

    @MyAnno
    private void test1() {
        CibThreadPool.getInstance().getIoTasks().execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    public static void test2() {
        CibThreadPool.getInstance().getIoTasks().execute(() -> {
            Log.e("TestClazz", "test() 拉姆达测试");
        });
    }
}
