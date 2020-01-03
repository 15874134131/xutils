package com.android.xlwlibrary.mvpbase;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.android.xlwlibrary.bean.Event;
import com.android.xlwlibrary.helper.XBaseActivityHelper;
import com.android.xlwlibrary.helper.XEventBusHelper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.ButterKnife;

public abstract class XBaseActivity<P extends XBasePresenter> extends FragmentActivity implements XIbaseView {
    private static final String TAG="XBaseActivity";
    private P mPresenter;
    // 加载进度框  ，如果需要，就可以初始化这个对象
    private ProgressDialog mProgressDialog;
    //判断显示在前端是否是当前activity,有个隐患如果新打开的activity 是个透明属性的，旧的activity 极有可能没有走onPause 方法。
    protected Boolean isFront = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果不是特别需要的话 ，可以设置为null ,减少一层绘制
        this.getWindow().setBackgroundDrawable(null);
        setContentView(initContentView());
        ButterKnife.bind(this);//初始化ButterKnife
        XBaseActivityHelper.getUtils().addActivity(this);
		if (isRegisterEventBus()) {
            XEventBusHelper.register(this);
        }
    }

    protected abstract int initContentView();

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
     * @param event 事件
     */
    protected void receiveEvent(Event event) {

    }
    /**
     * 接受到分发的粘性事件
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
    public void onResume() {
        super.onResume();
        isFront = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isFront = false;
    }

    @Override
    protected void onDestroy() {
        hideLoading();
        super.onDestroy();
        /**
         * 在生命周期结束时，将 presenter 与 view 之间的联系断开，防止出现内存泄露
         */
        if (mPresenter != null) {
            mPresenter.cancelAll();
            mPresenter.detachView();
        }
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
}
