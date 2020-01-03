package com.android.xlwlibrary.listener;

/**
 * Created by xu on 2019/12/11.
 */
public interface IntervalEdittextListener {
    //ed 满足要求返回
    void onEdSatisfy(String number);
    //ed 文本不为null但是未满足要求
    void onUnsatisfied();
    //ed 为null
    void onEdEmpty();
}
