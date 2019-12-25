package com.android.xlwlibrary.mvpbase;

public interface XIbasePresenter {
    /**
     * 取消网络请求
     *
     * @param tag 网络请求标记
     */
    void cancel(Object tag);

    /**
     * 取消所有的网络请求
     */
    void cancelAll();

    /**
     * 判断 presenter 是否与 view 建立联系，防止出现内存泄露状况
     * @return {@code true}: 联系已建立<br>{@code false}: 联系已断开
     */
    boolean isViewAttach();

    /**
     * 断开 presenter 与 view 直接的联系
     */
    void detachView();
}
