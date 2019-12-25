package com.android.xlwlibrary.mvpbase;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public class XBasePresenter<V> implements XIbasePresenter {
    // 防止 Activity 不走 onDestory() 方法，所以采用弱引用来防止内存泄漏
    private WeakReference<V> mViewRef;
    public XBasePresenter(@NonNull V view) {
        attachView(view);
    }

    private void attachView(V view) {
        mViewRef = new WeakReference<V>(view);
    }

    public V getView() {
        return mViewRef.get();
    }

    @Override
    public boolean isViewAttach() {
        return mViewRef != null && mViewRef.get() != null;
    }

    @Override
    public void detachView() {
        if (mViewRef != null) {
            mViewRef.clear();
            mViewRef = null;
        }
    }

    @Deprecated
    @Override
    public void cancel(Object tag) {
    }

    @Override
    public void cancelAll() {
    }
}
