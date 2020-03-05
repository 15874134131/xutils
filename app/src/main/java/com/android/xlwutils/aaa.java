package com.android.xlwutils;

import android.app.Application;

import com.android.xlwlibrary.helper.XHelper;
import com.android.xlwlibrary.helper.disk.XDiskLruCacheHelper;

import java.io.IOException;

/**
 * Created by x on 2020/3/3.
 */
public class aaa extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        XHelper.install(this);
        XHelper.defaultXHelper().xCrashHandlerHelper.init();
        try {
            XHelper.defaultXHelper().xDiskLruCacheHelper.install(this,"Object",20 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
