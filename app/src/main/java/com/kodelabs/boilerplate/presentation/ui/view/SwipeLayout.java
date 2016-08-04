package com.kodelabs.boilerplate.presentation.ui.view;

import android.content.Context;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.kdweibo.client.R;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Created on 16/08/04.
 */
public class SwipeLayout extends FrameLayout {

    private static final int STATE_CLOSE = 0;
    private static final int STATE_OPEN = 1;


    private int mState = STATE_CLOSE;
    @Setter
    @Getter
    @Accessors(prefix = {"m"})
    private int mPosition;
    @Setter
    @Getter
    @Accessors(prefix = {"m"})
    private boolean mSwipEnable = true;
    private int mBaseX;

    private Context mContext;
    private View mContentView;
    private View mMenuView;
    private ScrollerCompat mScroller;

    public SwipeLayout(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStryle) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        if (mScroller == null) {
            mScroller = ScrollerCompat.create(mContext);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = findViewById(R.id.view_content);
        mMenuView = findViewById(R.id.view_menu);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMenuView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mContentView.layout(0, 0, getMeasuredWidth(), mContentView.getMeasuredHeight());
        mMenuView.layout(getMeasuredWidth(), 0, getMeasuredWidth() + mMenuView.getMeasuredWidth(), mContentView.getMeasuredHeight());
    }

    @Override
    public void computeScroll() {
        if (mState == STATE_OPEN) {
            if (mScroller.computeScrollOffset()) {
                swipe(mScroller.getCurrX());
                postInvalidate();
            }
        } else {
            if (mScroller.computeScrollOffset()) {
                swipe(mBaseX - mScroller.getCurrX());
                postInvalidate();
            }
        }
    }

    private void swipe(int dis) {
        if (!mSwipEnable) {
            return;
        }
        if (Math.abs(dis) > mMenuView.getWidth()) {
            dis = mMenuView.getWidth();
        }
        mContentView.layout(-dis, mContentView.getTop(), mContentView.getWidth() - dis, getMeasuredHeight());

        mMenuView.layout(mContentView.getWidth() - dis, mMenuView.getTop(), mContentView.getWidth() + mMenuView.getWidth() - dis,
                mMenuView.getBottom());
    }

    private void smoothCloseMenu() {
        mState = STATE_CLOSE;
        mBaseX = -mContentView.getLeft();
        mScroller.startScroll(0, 0, mMenuView.getWidth(), 0, 300);//由左往右
        postInvalidate();
    }

    private void smoothOpenMenu() {
        if (!mSwipEnable) {
            return;
        }
        mState = STATE_OPEN;
        mScroller.startScroll(0, 0, mMenuView.getWidth(), 0, 300);//从有往左
        postInvalidate();
    }

    public void smoothOpenOrCloseMenu(){
        if(!mSwipEnable){
            return;
        }
        if(mState == STATE_OPEN){
            smoothCloseMenu();
        }else{
            smoothOpenMenu();
        }
    }

    private void closeMenu() {
        if (mScroller.computeScrollOffset()) {
            mScroller.abortAnimation();
        }
        if (mState == STATE_OPEN) {
            mState = STATE_CLOSE;
            swipe(0);
        }
    }

    private void openMenu() {
        if (!mSwipEnable) {
            return;
        }
        if (mState == STATE_CLOSE) {
            mState = STATE_OPEN;
            swipe(mMenuView.getWidth());
        }
    }
}