package com.kodelabs.boilerplate.presentation.ui.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;


/**
 * Created by Harbor
 * 闪屏首页，地点晃动的效果
 */
public class WaggleImageView extends FrameLayout{
    private Context mContext;

    private View mIvTokyo, mIvHk, mIvNewyork, mIvChina;
    private View mTvTokyo, mTvHk, mTvNewyork, mTvChina;

    private int mWidth, mHeight;

    public WaggleImageView(Context context) {
        super(context);
        init(context);
    }

    public WaggleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WaggleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        this.mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.layout_splash_first_view, this);
        mIvTokyo = view.findViewById(R.id.iv_tokyo);
        mIvHk= view.findViewById(R.id.iv_hongkong);
        mIvNewyork= view.findViewById(R.id.iv_newyork);
        mIvChina= view.findViewById(R.id.iv_china);
        mTvTokyo = view.findViewById(R.id.tv_tokyo);
        mTvHk= view.findViewById(R.id.tv_hongkong);
        mTvNewyork= view.findViewById(R.id.tv_newyork);
        mTvChina= view.findViewById(R.id.tv_china);
    }

    private AnimatorSet initAnimation(View locationView, View textView, long delay){
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animator = ObjectAnimator.ofFloat(locationView, "translationY", 0, textView.getHeight() * 0.3f);
        animator.setDuration(800);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(textView, "translationY", 0, textView.getHeight() * 0.45f);
        animator1.setDuration(800);
        animator1.setRepeatMode(ValueAnimator.REVERSE);
        animator1.setRepeatCount(ValueAnimator.INFINITE);
        animator1.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.playTogether(animator, animator1);
        animatorSet.setStartDelay(delay);
        return animatorSet;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    AnimatorSet animatorSet = null;
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(animatorSet == null) {
            animatorSet = new AnimatorSet();
            animatorSet.play(initAnimation(mIvTokyo, mTvTokyo, 0)).with(
                    initAnimation(mIvHk, mTvHk, 150)).with(
                    initAnimation(mIvNewyork, mTvNewyork, 200)).with(
                    initAnimation(mIvChina, mTvChina, 350));
            animatorSet.start();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }

    public void release(){
        if(animatorSet != null){
            animatorSet.cancel();
        }
    }

}
