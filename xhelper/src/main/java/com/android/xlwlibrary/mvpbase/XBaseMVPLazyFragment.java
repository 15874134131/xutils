package com.android.xlwlibrary.mvpbase;

import android.os.Bundle;

import com.android.xlwlibrary.bean.Event;
import com.android.xlwlibrary.helper.XEventBusHelper;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by xu on 2019/9/9.
 * 先显示基础布局，再显示网络等数据
 */
public abstract class XBaseMVPLazyFragment<P extends XBasePresenter> extends XBaseFragment<P> implements XIbaseView {
    private static final String TAG="XBaseFragment";
    private P mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isRegisterEventBus()) {
            XEventBusHelper.register(this);
        }
    }

    /**
     * 第一步,改变isViewInitiated标记
     * 当onViewCreated()方法执行时,表明View已经加载完毕,此时改变isViewInitiated标记为true,并调用 懒加载 方法
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isViewInitiated = true;
        //调用懒加载
        prepareFetchData();
    }

    /**
     * 第二步
     * 此方法会在onCreateView(）之前执行
     * 当viewPager中fragment改变可见状态时也会调用
     * 当fragment 从可见到不见，或者从不可见切换到可见，都会调用此方法
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisibleToUser = isVisibleToUser;
        prepareFetchData();
    }
    /**
     * Fragment的View加载完毕的标记
     */
    private boolean isViewInitiated;
    /**
     * Fragment对用户可见的标记
     */
    private boolean isVisibleToUser;
    /**
     * 是否懒加载
     */
    private boolean isDataInitiated;
    /**
     * 调用懒加载
     */
    private void prepareFetchData() {
        prepareFetchData(false);
    }

    /**
     * 第三步:进行双重标记判断,通过后即可进行数据加载
     */
    private void prepareFetchData(boolean forceUpdate) {
        if (isVisibleToUser && isViewInitiated && (!isDataInitiated || forceUpdate)) {
            fetchData();
            isDataInitiated = true;
        }
    }

    /**
     * 第四步:定义抽象方法fetchData(),具体加载数据的工作,交给子类去完成
     */
    public abstract void fetchData();

    /**
     * 获取 Presenter 对象，在需要获取时才创建`Presenter`，起到懒加载作用
     */
    public P getPresenter() {
        if (mPresenter == null) {
            mPresenter = onBindPresenter();
        }
        return mPresenter;
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

    @Override
    public void onDestroy() {
        hideLoading();
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.cancelAll();
            mPresenter.detachView();
        }
        if (isRegisterEventBus()) {
            XEventBusHelper.unregister(this);
        }
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
