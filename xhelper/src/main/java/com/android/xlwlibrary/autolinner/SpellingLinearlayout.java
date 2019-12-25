package com.android.xlwlibrary.autolinner;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.android.xlwlibrary.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2018/7/18.
 */

public class SpellingLinearlayout /*extends LinearLayout implements AutoNewLineLayout.HeiWidListener*/{
/*
    private AutoNewLineLayout spe_auto_choice,spe_auto_choice_hui;
    private AutoAddLinelayout spe_auto_add_hui,spe_auto_add;
    private List<String> shuffleDataString=new ArrayList<>();//混乱
    private List<String> DataString=new ArrayList<>();//有序
    private Context mcontext;
    private Boolean toClickOn=true;
    private float rate =0f;

    public SpellingLinearlayout(Context context) {
        super(context);
        mcontext=context;
        init();
    }

    public void setRate(Integer rate){
        switch (rate){
            case 1:
                this.rate=0f;
                break;
            case 2:
                this.rate=0.5f;
                break;
            case 3:
                this.rate=0.7f;
                break;
        }

    }

    public SpellingLinearlayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mcontext=context;
        init();
    }

    public SpellingLinearlayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mcontext=context;
        init();
    }

    public void init(){
        LayoutInflater inflater=(LayoutInflater) mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
*//*        inflater.inflate(R.layout.spe_linearlayout, this);
        spe_auto_choice=findViewById(R.id.spe_auto_choice);
        spe_auto_choice_hui=findViewById(R.id.spe_auto_choice_hui);
        spe_auto_add_hui=findViewById(R.id.spe_auto_add_hui);
        spe_auto_add=findViewById(R.id.spe_auto_add);*//*
        setViewEvents();
        addTags();
    }

    private void setViewEvents(){
        spe_auto_choice.setHeiWidLinstener(this);
    }

    public void setData(List<String> args){
        DataString=args;
        List<String> shuffle=new ArrayList<>();
        shuffle.addAll(args);
        Collections.shuffle(shuffle);
        shuffleDataString=shuffle;
        spe_auto_choice.removeAllViews();
        spe_auto_choice_hui.removeAllViews();
        spe_auto_add.removeAllViews();
        spe_auto_add_hui.removeAllViews();
        addTags();
    }

    public void setAnimation(float set){
        this.rate=set;
    }

    private void addTags() {
        if (shuffleDataString.size()>0) {
            for (int i = 0; i < shuffleDataString.size(); i++) {
                //生成背景
                TextView tv1 = new TextView(mcontext);
                tv1.setBackgroundResource(R.drawable.spe_choice_addtexthui);
                tv1.setText(shuffleDataString.get(i));
                tv1.setTextColor(ContextCompat.getColor(mcontext, R.color.xian));
                spe_auto_choice_hui.addView(tv1);

                //生成表面单词
                final MyTextView tv = new MyTextView(mcontext);
                tv.setBackgroundResource(R.drawable.spe_choice_addtext);
                tv.setText(shuffleDataString.get(i));
                tv.setTag(true);
                spe_auto_choice.addView(tv);
                tv.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        if ((Boolean) tv.getTag()) {
                            clickAdd(tv);
                        }
                    }
                });
            }

            if(rate != 0f) {
                for (int i = 0; i < DataString.size(); i++) {
                    for (int j = 0; j < DataString.size(); j++) {
                        if (((MyTextView) spe_auto_choice.getChildAt(j)).getText().toString().equals(DataString.get(i))) {
                            initFixedNewAdd(spe_auto_choice.getChildAt(j));
                        }
                    }
                }
                moveAllNewAddToChoiceForRate(rate);
            }
        }
    }

    Set<Integer> grayId = new HashSet<>();
    private void clickAdd(final View view){
        int existGrayViewIndex = -1;
        //循环查找第一个上方灰色单词，之所以这样写，是因为我们在把单词点上去之后，还有可能会点下来，这个时候我们就判断一下，之前是否有灰色单词，
        // 有的话，说明我们已经点上去，那么我们就是用之前点上去的那个textview ，
        for (int i = 0; i < spe_auto_add.getChildCount(); i++) {
            MyTextView addtexthui = (MyTextView) spe_auto_add.getChildAt(i);
            if(grayId.contains(addtexthui.hashCode())){
                //如果找到了，就设置第一个灰色单词为当前的这个下表
                existGrayViewIndex = i;
                break;
            }
        }
        MyTextView _addtext;
        MyTextView _addtexthui;
        //判断是否找到，-1为未找到
        if(existGrayViewIndex != -1){
            //找到，使用已经存在的单词
            _addtext = (MyTextView) spe_auto_add.getChildAt(existGrayViewIndex);
            _addtexthui = (MyTextView) spe_auto_add_hui.getChildAt(existGrayViewIndex);
        }else {
            NewAddViews newAddViews = new NewAddViews().invoke();
            _addtext = newAddViews.getAddtext();
            _addtexthui = newAddViews.getAddtexthui();
        }
        //设置为不可点击，主要就是防止用户多次点击
        view.setTag(false);

        final MyTextView addtext = _addtext;
        final MyTextView addtexthui = _addtexthui;
        setNewAddProps(view, addtext, addtexthui);


        final int finalExistGrayViewIndex = existGrayViewIndex;

        //开始移动动画,下排的单词移动到上面
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (int i=0;i<spe_auto_add.getChildCount();i++){
                    MyTextView textView= (MyTextView) spe_auto_add.getChildAt(i);
                    //如果新建的，则判断单词是否一致，不是新建的，就判断下标是否一致。
                    //finalExistGrayViewIndex==-1说明是点上去的
                    if ((finalExistGrayViewIndex == -1 && addtext.getText().toString().equals(textView.getText().toString())) || finalExistGrayViewIndex == i){
                        //目标
                        int[] locationWindow = new int[2] ;
                        textView.getLocationOnScreen(locationWindow); //获取在当前窗口内的绝对坐标
                        //当前
                        int[] startViewPos = new int[2];
                        view.getLocationOnScreen(startViewPos);
                            //动画,坐标移动一般是目标-当前
                            TranslateAnimation translateAnimation = new TranslateAnimation(0, locationWindow[0] - startViewPos[0], 0, locationWindow[1] - startViewPos[1]);
                            translateAnimation.setDuration(500);
                            view.startAnimation(translateAnimation);
                            translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    view.setVisibility(INVISIBLE);
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    addtext.setVisibility(VISIBLE);
                                    addtexthui.setVisibility(VISIBLE);
                                    view.setTag(true);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }
                            });
                    }
                }
            }
        });

        //上排的单词点击返回下面
        addtext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                moveNewAddToChoice(addtext, view);
            }
        });
    }

    private void setNewAddProps(View view, MyTextView addtext, MyTextView addtexthui) {
        //初始化textview 的属性
        addtext.setText(((MyTextView) view).getText().toString());
        addtext.setBackgroundResource(R.drawable.spe_choice_addtext);
        addtext.setVisibility(INVISIBLE);
        addtext.setTag(view);

        addtexthui.setBackgroundResource(R.drawable.spe_choice_addtexthui);
        addtexthui.setText(((MyTextView) view).getText().toString());
        addtexthui.setTextColor(ContextCompat.getColor(mcontext, R.color.xian));


        //删除保存的灰色单词
        grayId.remove(addtext.hashCode());

    }

    private void initFixedNewAdd(final View view){
        NewAddViews newAddViews = new NewAddViews().invoke();
        final MyTextView _addtext = newAddViews.getAddtext();
        MyTextView _addtexthui = newAddViews.getAddtexthui();

        setNewAddProps(view, _addtext, _addtexthui);
        //开始移动动画,下排的单词移动到上面

        view.setVisibility(INVISIBLE);
        _addtext.setVisibility(VISIBLE);
        _addtexthui.setVisibility(VISIBLE);
        toClickOn=true;

        //上排的单词点击返回下面
        *//*_addtext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                moveNewAddToChoice(_addtext, view);
            }
        });*//*
    }


    public void moveAllNewAddToChoiceForRate(float rate){
        float count = (int)shuffleDataString.size() * (1f-rate);
        for (int i = 0; i < shuffleDataString.size(); i++) {
            String word = shuffleDataString.get(i);
            View view = this.spe_auto_choice.getChildAt(i);
            for (int j = 0; j < this.spe_auto_add.getChildCount(); j++) {
                if(((MyTextView)this.spe_auto_add.getChildAt(j)).getText().equals(word)){
                    if(i<=count) {
                        MyTextView addtext = ((MyTextView) this.spe_auto_add.getChildAt(j));
                        grayId.add(addtext.hashCode());
                        addtext.setVisibility(INVISIBLE);
                        //底部单词显示
                        view.setVisibility(VISIBLE);
                    }else{
                        View hui = spe_auto_choice_hui.getChildAt(i);
                        view.setVisibility(INVISIBLE);
                        hui.setVisibility(INVISIBLE);
                    }
                }
            }
        }
    }

    private void moveNewAddToChoice(final MyTextView addtext, final View view) {
            if(grayId.contains(addtext.hashCode())){
            }else {
                final View xiaview = (View) addtext.getTag();
                int[] end = new int[2];///目标
                xiaview.getLocationOnScreen(end);

                int[] start = new int[2];//当前
                addtext.getLocationOnScreen(start);
                //动画,坐标移动一般是目标-当前
                TranslateAnimation translateAnimation = new TranslateAnimation(0, end[0] - start[0], 0, end[1] - start[1]);
                translateAnimation.setDuration(500);
                addtext.startAnimation(translateAnimation);

                grayId.add(addtext.hashCode());
                translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        addtext.setVisibility(INVISIBLE);
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //底部单词显示
                        view.setVisibility(VISIBLE);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            }
    }

    //判断是否有点下去的单词
    public Boolean getGrayId(){
        Boolean b=false;
        if (grayId.size()>0){
            b=false;
        }else {
            b=true;
        }
        return b;
    }

    public List<String> getAddLinearData(){
        List<String> stringList=new ArrayList<>();
        for (int i=0;i<spe_auto_add.getChildCount();i++){
            stringList.add(((TextView)spe_auto_add.getChildAt(i)).getText().toString());
        }
        return stringList;
    }

    @Override
    public void setheiwidMap(Map map) {
        spe_auto_add.setMap(map);
    }

    private class NewAddViews {
        private MyTextView addtext;
        private MyTextView addtexthui;

        public MyTextView getAddtext() {
            return addtext;
        }

        public MyTextView getAddtexthui() {
            return addtexthui;
        }

        public NewAddViews invoke() {
            //没找到，则创建新单词控件
            final MyTextView addtext = new MyTextView(mcontext);
            spe_auto_add.addView(addtext);

            //生成背景
            final MyTextView addtexthui = new MyTextView(mcontext);
            spe_auto_add_hui.addView(addtexthui);
            this.addtext = addtext;
            this.addtexthui = addtexthui;
            addtexthui.setVisibility(INVISIBLE);
            return this;
        }
    }*/
}
