package com.android.xlwlibrary.view;

import android.os.CountDownTimer;

import com.android.xlwlibrary.listener.CustomTimeCountListener;

/**
 * Created by xu on 2019/12/11.
 * 传入总毫秒数和 每次减少多少毫秒
 */
public class XCustomTimeCount extends CountDownTimer {
    private CustomTimeCountListener callback;

    public void setCallback(CustomTimeCountListener countCallback){
        this.callback=countCallback;
    }
    public XCustomTimeCount(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    @Override
    public void onTick(long l) {
        callback.onCountDownTimerTick(l);
    }

    @Override
    public void onFinish() {
        callback.onCountDownTimerFinish();
    }
}
