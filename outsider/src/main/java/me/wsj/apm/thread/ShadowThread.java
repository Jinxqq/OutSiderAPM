package me.wsj.apm.thread;

import android.util.Log;

import me.wsj.core.utils.Looger;

public class ShadowThread extends Thread {

    public ShadowThread(Runnable target) {
        super(target);
    }

    @Override
    public synchronized void start() {
        Looger.i("ShadowThread", "start,name=" + getName());
        CibThreadPool.getInstance().getDefaultTasks().execute(new MyRunnable(getName()));
    }

    class MyRunnable implements Runnable {

        String name;

        public MyRunnable(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            try {
                ShadowThread.this.run();
                Log.d("ShadowThread", "run name=" + name);
            } catch (Exception e) {
                Log.w("ShadowThread", "name=" + name + ",exception:" + e.getMessage());
                RuntimeException exception = new RuntimeException("threadName=" + name + ",exception:" + e.getMessage());
                exception.setStackTrace(e.getStackTrace());
                throw exception;
            }
        }
    }
}