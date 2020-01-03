package com.android.xlwutils;

import android.os.Bundle;
import com.android.xlwlibrary.callback.CustomTimeCountCallback;
import com.android.xlwlibrary.helper.XFileHelper;
import com.android.xlwlibrary.helper.XPreferencesHelper;
import com.android.xlwlibrary.helper.XlwForeignInterface;
import com.android.xlwlibrary.mvpbase.XBaseActivity;
import com.android.xlwlibrary.mvpbase.XBasePresenter;
import com.android.xlwlibrary.view.XCustomTimeCount;

public class MainAActivity extends XBaseActivity {
    XlwForeignInterface xlwForeignInterface;
    XFileHelper XFileHelper;
    XPreferencesHelper XPreferencesHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XCustomTimeCount XCustomTimeCount =new XCustomTimeCount(5000,1000);
        XCustomTimeCount.setCallback(new CustomTimeCountCallback() {
            @Override
            public void onCountDownTimerTick(long l) {

            }
            @Override
            public void onCountDownTimerFinish() {

            }
        });
        XCustomTimeCount.start();

    }

    @Override
    protected int initContentView() {
        return R.layout.activity_main;
    }

    @Override
    public XBasePresenter onBindPresenter() {
        return null;
    }

    @Override
    public void mvpError(String action, int code, String msg) {

    }
}
