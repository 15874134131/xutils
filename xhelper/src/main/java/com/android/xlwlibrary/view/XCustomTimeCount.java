package com.android.xlwlibrary.view;

import android.os.CountDownTimer;

import com.android.xlwlibrary.callback.CustomTimeCountCallback;

/**
 * Created by xu on 2019/12/11.
 * 传入总毫秒数和 每次减少多少毫秒
 */
public class XCustomTimeCount extends CountDownTimer {
    private CustomTimeCountCallback callback;

    public void setCallback(CustomTimeCountCallback countCallback){
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
