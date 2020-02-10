package com.android.xlwutils;

import android.app.Activity;
import android.os.Bundle;

import com.android.xlwlibrary.helper.XStringHelper;
import com.android.xlwlibrary.helper.XToastHelper;
import com.android.xlwlibrary.helper.disk.XDiskLruCacheHelper;

import java.io.IOException;

public class MainAActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        XDiskLruCacheHelper diskLruCacheHelper = null;
        try {
            diskLruCacheHelper=new XDiskLruCacheHelper(this,"Object",20 * 1024 * 1024);
            diskLruCacheHelper.put(XStringHelper.md5Decode("test"),"asdasdasdasdasdasdasd");
        } catch (IOException e) {
            e.printStackTrace();
        }
        XToastHelper.showLong(this, diskLruCacheHelper != null ? diskLruCacheHelper.getAsString(XStringHelper.md5Decode("test")) : "getAsString is null");
    }

}
