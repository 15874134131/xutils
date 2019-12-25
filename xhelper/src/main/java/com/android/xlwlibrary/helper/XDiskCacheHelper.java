package com.android.xlwlibrary.helper;

import android.content.Context;
import android.os.Environment;

import com.jakewharton.disklrucache.DiskLruCache;
import java.io.File;
import java.io.IOException;

/**
 * Created by xu on 2019/9/20.
 */
public class XDiskCacheHelper {

    /**
     * 判断是否挂在了SDcard
     * @param context
     * @param uniqueName
     * @return
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 创建缓存对象
     * @param context 上下文
     * @param uniqueName 缓存目录名
     * @return
     */
    public static DiskLruCache createUniqueNameCache(Context context,String uniqueName){
        DiskLruCache diskLruCache=null;
        //缓存bitmap
        try {
            File cacheDir = XDiskCacheHelper.getDiskCacheDir(context,uniqueName);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            diskLruCache= DiskLruCache.open(cacheDir, XSystemHelper.getVersionCode(context),1,10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return diskLruCache;
    }
}
