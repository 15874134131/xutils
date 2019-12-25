package com.android.xlwlibrary.mvpbase;

import android.util.Log;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * author: xu 2019/3/21
 * desc:
 */
 public abstract class XBaseObserver<T> implements Observer<T>{
    private String TAG = getClass().getSimpleName();
    private Disposable mDisposable;
   @Override
   public void onSubscribe(Disposable d) {
       subscribe(d);
       Log.i(TAG,"onSubscribe");
   }

    @Override
    public void onNext(T value) {
        onSuccess(value);
        Log.i(TAG,"onNext");
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof XApiException) {
            XApiException exception = (XApiException) e;
            error(exception.getCode(), exception.getMsg());
        } else {
            error(XExceptionEngine.UN_KNOWN_ERROR, "未知错误");
        }
        Log.i(TAG,"onError");
    }

    @Override
    public void onComplete() {
        complete();
        Log.i(TAG,"onComplete");
    }
    public abstract void onSuccess(T data);
    public abstract void error(int error_code,String error_msg);
    public abstract void complete();
    public abstract void subscribe(Disposable d);
}
