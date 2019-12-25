package com.android.xlwlibrary.autolinner;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.xlwlibrary.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenliu on 2016/8/17.<br/>
 * 描述：自动换行Layout
 * </br>
 */
public class AutoNewLineLayout extends ViewGroup {

    /**
     * 两个子控件之间的横向间隙
     */
    protected float horizontalSpace = 0;

    /**
     * 两个子控件之间的垂直间隙
     */
    protected float vertivalSpace = 0;
    private int oneShareHeight=0;//第一条横线的位置
    private int twoshareHeight=0;//第二条横线的位置
    private int threeshareHeight=0;//第二条横线的位置
    private int myrawHeight=0;
    private HeiWidListener callback;

    public interface HeiWidListener{
        void setheiwidMap(Map map);
    }
    public void setHeiWidLinstener(HeiWidListener widLinstener){
        this.callback=widLinstener;
    }

    public AutoNewLineLayout(Context context) {
        this(context, null, 0);
    }

    public AutoNewLineLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoNewLineLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
         //获取自定义属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AutoNewLineLayout);
        horizontalSpace = ta.getDimension(R.styleable.AutoNewLineLayout_horizontalSpace, 0);
        vertivalSpace = ta.getDimension(R.styleable.AutoNewLineLayout_vertivalSpace, 0);
        ta.recycle();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        //AT_MOST
        int width = 0;
        int height = 0;
        int rawWidth = 0;//当前行总宽度
        int rawHeight = 0;// 当前行高

        int rowIndex = 0;//当前行位置
        int count = getChildCount();//获取子view
        //循环获取子view
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if(child.getVisibility() == GONE){
                if(i == count - 1){
                    //最后一个child，设置高度为累加的高度
                    height += rawHeight;
                    width = Math.max(width, rawWidth);
                }
                continue;
            }

            //这里调用measureChildWithMargins 而不是measureChild,使用这个方法的原因是吧子view 的margin 和padiing 也考虑进去,gai
            //这个方法就是测量子view
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            //获取子view 的布局模式
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            //getMeasuredWidth原始的宽度 +各种数值组合为最终的宽度
            //getMeasuredHeight原始的高度+各种数值
            int childWidth = child.getMeasuredWidth()  + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            //最后得出子view 的宽高
            //这里的意思大概是把所有的child 宽度相加，如果超过了就换行
            if(rawWidth + childWidth  + (rowIndex > 0 ? horizontalSpace : 0)> widthSpecSize - getPaddingLeft() - getPaddingRight()){
                //换行
                //判断宽度，是总宽度大还是设置的宽度大，把当前的宽度设置为最大的那个值
                width = Math.max(width, rawWidth);
                rawWidth = childWidth; //总宽度累加
                height += rawHeight + vertivalSpace;//这里的意思应该是高度累加，，
                rawHeight = childHeight;
                rowIndex = 0;
            } else {
                //如果没有超出就不换行
                rawWidth += childWidth;//宽度累加
                if(rowIndex > 0){//位置》0 就将横向的间距加上
                    rawWidth += horizontalSpace;
                }
                //设置当前行的高度
                rawHeight = Math.max(rawHeight, childHeight);
                if (oneShareHeight==0){
                    oneShareHeight=rawHeight;
                }
            }
            //判断一下，如果是最后一个
            if(i == count - 1){
                width = Math.max(rawWidth, width);
                height += rawHeight;
            }
            rowIndex++;
        }
        //如果是精准模式，将宽度设置为设置的值，如果不是，就将行的宽度设置为前面计算好的宽度，高度也是。
        setMeasuredDimension(widthSpecMode == MeasureSpec.EXACTLY ? widthSpecSize : width + getPaddingLeft() + getPaddingRight(),
                heightSpecMode == MeasureSpec.EXACTLY ? heightSpecSize : height + getPaddingTop() + getPaddingBottom()
        );
        myrawHeight=height + getPaddingTop() + getPaddingBottom();

        Map<String, Integer> xian=new HashMap<>();
        xian.put("one",oneShareHeight);
        xian.put("vertivalSpace", (int) vertivalSpace);
        if (callback!=null) {
            if (oneShareHeight>0){
                callback.setheiwidMap(xian);
            }
        }
    }

    //boolean changed, int left, int top, int right, int bottom
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int viewWidth = r - l;//获取了view width
        int leftOffset = getPaddingLeft();//left padding 值
        int topOffset = getPaddingTop(); //top padding
        int rowMaxHeight = 0;
        int rowIndex = 0;//当前行位置
        View childView;
        for( int w = 0, count = getChildCount(); w < count; w++ ){
            //如果是隐藏的就下一个
            childView = getChildAt(w);
            if(childView.getVisibility() == GONE) continue;
            //获取ziview 的宽度
            MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
            int occupyWidth = lp.leftMargin + childView.getMeasuredWidth() + lp.rightMargin;
            //如果这个ziview 的宽度大于我们的父控件的宽度
            if(leftOffset + occupyWidth + getPaddingRight() > viewWidth){
                leftOffset = getPaddingLeft();  // 回到最左边
                topOffset += rowMaxHeight + vertivalSpace;  // 换行
                //初始化下标
                rowMaxHeight = 0;
                rowIndex = 0;
            }

            int left = leftOffset + lp.leftMargin;
            int top = topOffset + lp.topMargin;
            int right = leftOffset+ lp.leftMargin + childView.getMeasuredWidth();
            int bottom =  topOffset + lp.topMargin + childView.getMeasuredHeight();

            childView.layout(left, top, right, bottom);
            // 横向偏移
            leftOffset += occupyWidth;
            // 试图更新本行最高View的高度
            int occupyHeight = lp.topMargin + childView.getMeasuredHeight() + lp.bottomMargin;
            if(rowIndex != count - 1){
                leftOffset += horizontalSpace;
            }
            rowMaxHeight = Math.max(rowMaxHeight, occupyHeight);
            rowIndex++;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }


}

