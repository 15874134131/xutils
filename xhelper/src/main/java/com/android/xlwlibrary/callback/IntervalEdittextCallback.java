package com.android.xlwlibrary.callback;

/**
 * Created by xu on 2019/12/11.
 */
public interface IntervalEdittextCallback {
    //ed 满足要求返回
    void onEdSatisfy(String number);
    //ed 文本不为null但是未满足要求
    void onUnsatisfied();
    //ed 为null
    void onEdEmpty();
}
