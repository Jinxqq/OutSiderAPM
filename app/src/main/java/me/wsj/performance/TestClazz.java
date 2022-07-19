package me.wsj.performance;

import me.wsj.apm.thread.ShadowThread;

public class TestClazz {
    private void test1() {
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    private void test2() {
        new ShadowThread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }
}
