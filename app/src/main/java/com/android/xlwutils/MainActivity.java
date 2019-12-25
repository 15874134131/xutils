package com.android.xlwutils;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.xlwlibrary.callback.CustomTimeCountCallback;
import com.android.xlwlibrary.helper.XFileHelper;
import com.android.xlwlibrary.helper.XPreferencesHelper;
import com.android.xlwlibrary.helper.XlwForeignInterface;
import com.android.xlwlibrary.view.XCustomTimeCount;

public class MainActivity extends AppCompatActivity {
    XlwForeignInterface xlwForeignInterface;
    XFileHelper XFileHelper;
    XPreferencesHelper XPreferencesHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
