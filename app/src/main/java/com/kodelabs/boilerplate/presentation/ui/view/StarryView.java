package com.kodelabs.boilerplate.presentation.ui.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;


import static android.view.animation.Animation.INFINITE;

/**
 * Created by Harbor
 * 闪屏页星星闪烁与流量划过效果
 */

public class StarryView extends View{
    private Context mContext;
    private Bitmap mStarBitmap;
    private Bitmap mMeteorBitmap;
    private int mStarCount;
    private int mMeteorCount;
    private int mWidth, mHeight;
    private Point[] mStarPoints;
    private Point[] mMeteorPoints;
    private int[] mstarAlphas;
    private int mStartMargin;
    private ArrayMap<Integer, Point> mMeteorPointMap;

    private Paint mPaint;
    private Handler mHandler;
    public StarryView(Context context) {
        super(context);
        init(context);
    }

    public StarryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StarryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        this.mContext = context;
        mStarBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_flash_star);
        mMeteorBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_flash_meteor);
        mStarCount = 5;//randomCommon(5, 10);
        mMeteorCount = 3;//randomCommon(4, 6);
        mStartMargin = mStarBitmap.getHeight();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(255);
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mHandler = new Handler();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (mStarPoints == null && mWidth > 0 && mHeight > 0) {
            mStarPoints = new Point[mStarCount];
            Point point;//随机坐标点
            for (int i = 0; i < mStarCount; i++) {
                switch (i) {
                    case 0:
//                        point = new Point(randomCommon(20, mWidth/2), randomCommon(20, mHeight/2 - 20));//随机坐标点
                        point = new Point(mWidth / 6, mHeight / 20);
                        break;
                    case 1:
//                        point = new Point(randomCommon(mWidth/2, mWidth - mStartMargin), randomCommon(20, mHeight/2 - mStartMargin));//随机坐标点
                        point = new Point(mWidth * 4 / 5, mHeight / 10);
                        break;
                    case 2:
//                        point = new Point(randomCommon(20, mWidth/2), randomCommon(mHeight/2 , mHeight - 20));//随机坐标点
                        point = new Point(mWidth * 3 / 4, mHeight / 2 + 50);
                        break;
                    case 3:
//                        point = new Point(randomCommon(mWidth/2, mWidth - mStartMargin), randomCommon(mHeight/2 , mHeight - mStartMargin));//随机坐标点
                        point = new Point(mWidth / 8, mHeight * 3 / 4);
                        break;
                    default:
                        point = new Point(mWidth * 3 / 4, mHeight * 5 / 6);
                        break;
                }
                mStarPoints[i] = point;
            }
        }
        if (mMeteorPoints == null && mWidth > 0 && mHeight > 0) {
            mMeteorPoints = new Point[mMeteorCount];
            mMeteorPoints[0] = new Point(mWidth * 3 / 4, -mMeteorBitmap.getHeight());
            for (int i = 1; i < mMeteorCount; i++) {
                switch (i) {
                    case 1:
                        mMeteorPoints[i] = new Point(mWidth, mHeight / 4);
                        break;
                    default:
                        mMeteorPoints[i] = new Point(mWidth, mHeight / 2);
                        break;
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        startAnimation();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    AnimatorSet animatorSet;
    private void startAnimation() {
        if (mstarAlphas == null) {
            mstarAlphas = new int[mStarCount];
        }
        if (animatorSet == null) {
            animatorSet = new AnimatorSet();
            for(int i=0; i<mStarCount; i++){
                Animator animator = initStarAnimation(i);
                animatorSet.playTogether(animator);
            }
            for(int i=0; i<mMeteorCount; i++){
                Animator animator = initMeteorAnimation(i);
                animatorSet.playTogether(animator);
            }
            animatorSet.start();
        }
    }

    private Animator initStarAnimation(final int index){
        //星星闪烁动画
        ValueAnimator animator = ValueAnimator.ofInt(0, 0, 0, 64, 96, 128, 160, 192, 224, 255, 255, 255);
        if(index % 2 == 0){
            animator = ValueAnimator.ofInt(0, 0, 25, 50, 100, 150, 200, 200, 200);
        }
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int current = (int) animation.getAnimatedValue();
                mstarAlphas[index] = current;
                postInvalidate();
            }
        });
        animator.setDuration(1800);
        if(index % 2 == 0){
            animator.setDuration(1200);
        }
        animator.setStartDelay(index * 100);
        animator.setRepeatCount(INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        return animator;
    }

    //流星滑过的痕迹
    private ValueAnimator initMeteorAnimation(final int index) {
        Point startPoint = mMeteorPoints[index];
        Point endPoint;
        if (startPoint.y > 0) {
            endPoint = new Point(-mMeteorBitmap.getWidth(), startPoint.y + (mWidth + mMeteorBitmap.getWidth()) * mMeteorBitmap.getHeight() / mMeteorBitmap.getWidth());
        } else {
            endPoint = new Point(-mMeteorBitmap.getWidth(), startPoint.x * mMeteorBitmap.getHeight() / mMeteorBitmap.getWidth());
        }

        if (mMeteorPointMap == null) {
            mMeteorPointMap = new ArrayMap<>(mMeteorCount);
        }
        final ValueAnimator anim = ValueAnimator.ofObject(new PointEvaluator(), startPoint, endPoint);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Point currentPoint = (Point) animation.getAnimatedValue();
                mMeteorPointMap.put(index, currentPoint);
                postInvalidate();
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        anim.start();
                    }
                }, 1500);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        int duration = 800;
        if(index % 2 != 0){
            duration = 450;
        }
        anim.setDuration(duration);
        anim.setRepeatMode(ValueAnimator.RESTART);
        anim.setStartDelay(index * 50);
        anim.setInterpolator(new AccelerateInterpolator());
        return anim;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        for (int i = 0; i < mStarCount; i++) {
            mPaint.setAlpha(mstarAlphas[i]);
            Point point = mStarPoints[i];
            canvas.drawBitmap(mStarBitmap, null, new Rect(point.x, point.y,
                    (int) (point.x + (i + 1) * 0.2f * mStarBitmap.getWidth() / mStarCount + 0.2f * mStarBitmap.getWidth())
                    , (int) (point.y + (i + 1) * 0.2f * mStarBitmap.getHeight() / mStarCount + 0.2f * mStarBitmap.getHeight())), mPaint);
        }
        mPaint.setAlpha(255);
        for (int i = 0; i < mMeteorCount; i++) {
            Point point = mMeteorPointMap.get(i);
            if (point == null) {
                point = mMeteorPoints[i];
            }
            if(i == mMeteorCount -1){
                canvas.drawBitmap(mMeteorBitmap, null, new Rect(point.x, point.y,
                        Math.round(point.x + mMeteorBitmap.getWidth() * 0.7f), Math.round(point.y + mMeteorBitmap.getHeight() * 0.7f)), mPaint);
            }else{
                canvas.drawBitmap(mMeteorBitmap, point.x, point.y, mPaint);
            }
        }
        canvas.restore();
    }

    public void release(){
        if(animatorSet != null){
            animatorSet.cancel();
        }
        if(mStarBitmap != null && !mStarBitmap.isRecycled()){
            mStarBitmap.recycle();
        }
        if(mMeteorBitmap != null && !mMeteorBitmap.isRecycled()){
            mMeteorBitmap.recycle();
        }
    }


    /**
     * 随机指定范围内的数
     * 最简单最基本的方法
     * @param min 指定范围最小值
     * @param max 指定范围最大值
     */
    public static int randomCommon(int min, int max){
        if (max < min) {
            return min;
        }
            int num = (int) (Math.random() * (max - min)) + min;
        return num;
    }

    private static class PointEvaluator implements TypeEvaluator {
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            Point startPoint = (Point) startValue;
            Point endPoint = (Point) endValue;
            int x = Math.round(startPoint.x + fraction * (endPoint.x - startPoint.x));
            int y = Math.round(startPoint.y + fraction * (endPoint.y - startPoint.y));
            return new Point(x, y);
        }

    }

}
