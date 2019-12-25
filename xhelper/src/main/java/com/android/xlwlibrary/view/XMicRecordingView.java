package com.android.xlwlibrary.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.android.xlwlibrary.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/5/16.
 * 一个类似于酷狗听歌识曲背景动画的控件
 */

public class XMicRecordingView extends View {
    private Paint mpaint;
    private List<Float> alphaList; //所有圆圈透明度
    private List<Float> rList; //所有圆圈半径大小
    /**是否运行动画*/
    private boolean isStarting = false;
    //设置圆圈的最大个数
    private int count=5;
    //圆圈的最大的半径，用来控制最外层一个圆圈
    private  float maxWidth=350;
    //设置我们圆圈的坐标点，
    private float circleX, circleY;
    //每个圆圈的半径
    private float gapLength=40;
    /**圆环的颜色*/
    private int circleColor = Color.RED;

    public XMicRecordingView(Context context) {
        super(context);
    }

    public XMicRecordingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        alphaList = new ArrayList<>();
        rList=new ArrayList<>();
        mpaint = new Paint();
        mpaint.setColor(ContextCompat.getColor(context, R.color.white));
        alphaList.add(0, 150f);//设置圆圈一开始的颜色；
        rList.add(0,0.f);//设置圆圈一开始的半径，这里意味着圆圈一开始就是一个点。
    }

    public XMicRecordingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        maxWidth = w > h ? h / 2 : w / 2;
        circleX=w/2; //圆心X
        circleY=h/2;//圆心Y
        gapLength=maxWidth/count;
        //count = maxWidth / gapLength;//最大的范围除以每个圆圈的半径确定可以绘制的圆圈
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i=0;i<rList.size();i++){
            float alpha=alphaList.get(i);//获取当前圆圈的透明度
            float circleWidth=rList.get(i);//获取当前圆圈的半径
            mpaint.setAlpha((int) alpha);
            float width=maxWidth/alpha;

            canvas.drawCircle(circleX,circleY,circleWidth,mpaint);//绘制圆
            if (isStarting&&alpha>0&&circleWidth<maxWidth){//如果当前圆他的透明度大于0，并且这个圆的半径没有超出我们的设定，就慢慢的扩大，相当于放大效果
                alphaList.set(i,alpha-1); //透明度不断的减少
                rList.set(i,circleWidth+width);
            }

            /**当一个圆形的半径扩大到了gapLength的长度，就创建下一个圆*/
            if(isStarting && rList.get(rList.size() - 1) >gapLength){
                alphaList.add(150f);
                rList.add(0.f);
            }

            if(isStarting && rList.size() == count){//如果现在的圆圈已经到顶了，就删除最开始的那个
                alphaList.remove(0);
                rList.remove(0);
            }
            invalidate();
        }
    }
    /**
     *执行动画
     */
    public void start() {
        isStarting = true;
        alphaList.add(0, 150f);//设置圆圈一开始的颜色；
        rList.add(0,0.f);//设置圆圈一开始的半径，这里意味着圆圈一开始就是一个点。
    }

    public void view_invalidate() {
        isStarting = false;
        alphaList.clear();
        rList.clear();
        this.invalidate();
    }
    /**
     * 判断是都在不在执行
     */
    public boolean isStarting() {
        return isStarting;
    }
}
