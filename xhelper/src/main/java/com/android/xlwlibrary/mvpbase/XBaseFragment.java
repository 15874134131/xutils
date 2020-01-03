package com.android.xlwlibrary.mvpbase;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.android.xlwlibrary.bean.Event;
import com.android.xlwlibrary.helper.XEventBusHelper;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by xuliangwang on 2019/9/4.
 */
public abstract class XBaseFragment<P extends XBasePresenter> extends Fragment implements XIbaseView {
    private static final String TAG="XBaseFragment";
    private P mPresenter;
    private Unbinder unbinder;

    //初始化xml文件
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getContentViewLayoutID() != 0) {
            return inflater.inflate(getContentViewLayoutID(), null);
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    protected abstract int getContentViewLayoutID();

    //注解绑定以及初始化组件
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder=ButterKnife.bind(this, view);
        if (isRegisterEventBus()) {
            XEventBusHelper.register(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBusCome(Event event) {
        if (event != null) {
            receiveEvent(event);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onStickyEventBusCome(Event event) {
        if (event != null) {
            receiveStickyEvent(event);
        }
    }

    /**
     * 接收到分发到事件
     *
     * @param event 事件
     */
    protected void receiveEvent(Event event) {

    }
    /**
     * 接受到分发的粘性事件
     *
     * @param event 粘性事件
     */
    protected void receiveStickyEvent(Event event) {

    }
    /**
     * @return true绑定EventBus事件分发，默认不绑定，子类需要绑定的话复写此方法返回true.
     */
    protected boolean isRegisterEventBus() {
        return false;
    }

    /**
     * 创建 Presenter
     *
     * @return
     */
    public abstract P onBindPresenter();


    /**
     * 获取 Presenter 对象，在需要获取时才创建`Presenter`，起到懒加载作用
     */
    public P getPresenter() {
        if (mPresenter == null) {
            mPresenter = onBindPresenter();
        }
        return mPresenter;
    }

    @Override
    public void onDestroy() {
        hideLoading();
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.cancelAll();
            mPresenter.detachView();
        }
        unbinder.unbind();
        if (isRegisterEventBus()) {
            XEventBusHelper.unregister(this);
        }
        Log.i(TAG,"Base onDestroy");
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void mvpError(String action, int code, String msg) {

    }
}
