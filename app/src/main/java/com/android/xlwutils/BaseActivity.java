package com.android.xlwutils;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import org.greenrobot.eventbus.*;


public abstract class BaseActivity extends FragmentActivity {
    private static final String TAG="XBaseActivity";
    //判断显示在前端是否是当前activity,如果新打开的activity 是个透明属性的，旧的activity 极有可能没有走onPause 方法。
    protected Boolean isFront = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setBackgroundDrawable(null);//如果不是特别需要的话 ，可以设置为null ,减少一层绘制

        //这里注意下 因为在评论区发现有网友调用setRootViewFitsSystemWindows 里面 winContent.getChildCount()=0 导致代码无法继续
        //是因为你需要在setContentView之后才可以调用 setRootViewFitsSystemWindows

        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
/*        XScreenHelper.setRootViewFitsSystemWindows(this,true);
        //设置状态栏透明
        XScreenHelper.setTranslucentStatus(this);*/
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
/*        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this,0x55000000);
        }*/
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
        super.onDestroy();

    }


/*    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBusCome(Event event) {
        if (event != null) {
            receiveEvent(event);
        }
    }*/

/*    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onStickyEventBusCome(Event event) {
        if (event != null) {
            receiveStickyEvent(event);
        }
    }*/

    /**
     * 接收到分发到事件
     * @param event 事件
     */
/*    protected void receiveEvent(Event event) {

    }*/
    /**
     * 接受到分发的粘性事件
     * @param event 粘性事件
     */
/*    protected void receiveStickyEvent(Event event) {

    }*/
    /**
     * @return true绑定EventBus事件分发，默认不绑定，子类需要绑定的话复写此方法返回true.
     */
    protected boolean isRegisterEventBus() {
        return false;
    }

}
