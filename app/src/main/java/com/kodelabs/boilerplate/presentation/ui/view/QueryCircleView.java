package com.kodelabs.boilerplate.presentation.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.client.R;

public class QueryCircleView extends View {

    private Paint mPaint;
    private Bitmap mBitmap4;
    private Bitmap mBitmap2;
    private Bitmap mBitmap3;
    private Bitmap mBitmap1;
    private ValueAnimator mAnimator1;
    private ValueAnimator mAnimator2;
    private ValueAnimator mAnimator3;
    private ValueAnimator mAnimator4;
    private int mDegree1;
    private int mDegree2;
    private int mDegree3;
    private int mDegree4;

    private int mWidth, mHeight;
    private Context mContext;

    public QueryCircleView(Context context) {
        super(context);
        initView(context);
    }

    public QueryCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public QueryCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mWidth <= 0) {
            mWidth = getMeasuredWidth();
        }
        if (mHeight <= 0) {
            mHeight = getMeasuredHeight();
        }
        initBitmap(mContext);
    }

    private void initView(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mContext = context;
    }

    public void startQueryAnimation(int count, boolean disappear) {
        if (mAnimator4 == null) {
            mAnimator4 = ValueAnimator.ofInt(0, -360);
            mAnimator4.setDuration(4000);
            mAnimator4.setRepeatCount(count);
            mAnimator4.setInterpolator(new LinearInterpolator());
            mAnimator4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    mDegree4 = value;
                    invalidate();
                }
            });
        }
        mAnimator4.start();
        if (mAnimator3 == null) {
            mAnimator3 = ValueAnimator.ofInt(0, 360);
            mAnimator3.setDuration(3000);
            mAnimator3.setRepeatCount(count);
            mAnimator3.setInterpolator(new LinearInterpolator());
            mAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    mDegree3 = value;
                    invalidate();
                }
            });
        }
        mAnimator3.start();
        if (mAnimator2 == null) {
            mAnimator2 = ValueAnimator.ofInt(0, -360);
            mAnimator2.setDuration(3500);
            mAnimator2.setRepeatCount(count);
            mAnimator2.setInterpolator(new LinearInterpolator());
            mAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    mDegree2 = value;
                    invalidate();
                }
            });
        }
        mAnimator2.start();
        if (mAnimator1 == null) {
            mAnimator1 = ValueAnimator.ofInt(0, 360);
            mAnimator1.setDuration(4000);
            mAnimator1.setInterpolator(new LinearInterpolator());
            mAnimator1.setRepeatCount(count);
            mAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    mDegree1 = value;
                    invalidate();
                }
            });
        }
        mAnimator1.start();
    }

    public void stopAnimation() {
        if (mAnimator4 != null) {
            mAnimator4.cancel();
        }
        if (mAnimator2 != null) {
            mAnimator2.cancel();
        }
        if (mAnimator3 != null) {
            mAnimator3.cancel();
        }
        if (mAnimator4 != null) {
            mAnimator4.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.rotate(mDegree1, Math.round(mWidth / 2), Math.round(mHeight / 2));
        canvas.drawBitmap(mBitmap1, mWidth / 2 - mBitmap1.getWidth() / 2, mHeight / 2 - mBitmap1.getHeight() / 2, mPaint);
        canvas.restore();
        canvas.save();
        canvas.rotate(mDegree2, Math.round(mWidth / 2), Math.round(mHeight / 2));
        canvas.drawBitmap(mBitmap2, mWidth / 2 - mBitmap2.getWidth() / 2, mHeight / 2 - mBitmap2.getHeight() / 2, mPaint);
        canvas.restore();
        canvas.save();
        canvas.rotate(mDegree3, Math.round(mWidth / 2), Math.round(mHeight / 2));
        canvas.drawBitmap(mBitmap3, mWidth / 2 - mBitmap3.getWidth() / 2, mHeight / 2 - mBitmap3.getHeight() / 2, mPaint);
        canvas.restore();
        canvas.save();
        canvas.rotate(mDegree4, Math.round(mWidth / 2), Math.round(mHeight / 2));
        canvas.drawBitmap(mBitmap4, mWidth / 2 - mBitmap4.getWidth() / 2, mHeight / 2 - mBitmap4.getHeight() / 2, mPaint);
        canvas.restore();
    }

    private void initBitmap(Context context) {
        if(mWidth <= 0 || mHeight <= 0){
            return;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.outWidth = mWidth;
        opts.outHeight = mHeight;
        if (mBitmap1 == null) {
            mBitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.upgrade_query_1, opts);
        }
        if (mBitmap2 == null) {
            mBitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.upgrade_query_2, opts);
        }
        if (mBitmap3 == null) {
            mBitmap3 = BitmapFactory.decodeResource(context.getResources(), R.drawable.upgrade_query_3, opts);
        }
        if (mBitmap4 == null) {
            mBitmap4 = BitmapFactory.decodeResource(context.getResources(), R.drawable.upgrade_query_4, opts);
        }
    }

    public void onDestroy(){
        stopAnimation();
        if(mBitmap1 != null && !mBitmap1.isRecycled()){
            mBitmap1.recycle();
        }
        if(mBitmap2 != null && !mBitmap2.isRecycled()){
            mBitmap2.recycle();
        }
        if(mBitmap3 != null && !mBitmap3.isRecycled()){
            mBitmap3.recycle();
        }
        if(mBitmap4 != null && !mBitmap4.isRecycled()){
            mBitmap4.recycle();
        }
    }
}
