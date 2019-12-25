package com.android.xlwlibrary.threadpool;

/**
 * Created by Administrator on 2018/10/15.
 */

public abstract class RunWithPriority implements Runnable {
    public int priority;

    public RunWithPriority(int priority){
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
