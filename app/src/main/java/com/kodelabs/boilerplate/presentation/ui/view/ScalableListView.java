package com.kodelabs.boilerplate.presentation.ui.view;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.xunlei.log.LogUtil;
import com.kodelabs.boilerplate.R;

/**
 * @author Chenwf
 * @date 2016/10/24
 * <p/>
 * 功能描述：主页面自定义的Listview，支持滑动时item的缩放
 */

public class ScalableListView extends ListView {
    private final static String TAG = ScalableListView.class.getSimpleName();
    private static final int THRESHOLD_DISTANCE = 10;
    private static final int THRESHOLD_VELOCITY = 7500;
    private static final int DURATION_LONG = 250;
    private static final int DURATION_SHORT = 150;

    private Point mStartPoint = new Point(0, 0);
    private View mViewHeader;
    private View mViewFooter;
    private View mScalableView;
    private View mBtnView;
    private Point mHeadLayoutParams;//记录初始头部宽高信息
    private GestureDetector mGestureDetector;
    private ArgbEvaluator mArgbEvaluator;//颜色计算

    private int mY120, mX62;
    private int mX13, mY80;
    private int mTextColorMax;
    private int mTextColorMin;
    private int mTitleColorMax;
    private int mMaxHeight;
    private int mMinxHeight;
    private int mBallMaxX, mBallMinX;

    private boolean mHasResetted = false;
    private boolean mScrollUp;//是否向上滑动
    private boolean mIsFling = false;//是否飞速滑动
    private boolean mEnableTry;

    private Context mContext;

    public ScalableListView(Context context) {
        super(context);
        init(context);
    }

    public ScalableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScalableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setScalableView(int viewId, int btnId) {
        if (mViewHeader != null) {
            mScalableView = mViewHeader.findViewById(viewId);
            mBtnView = mViewHeader.findViewById(btnId);
        }
    }

    public void setEnableTry(boolean mEnableTry) {
        this.mEnableTry = mEnableTry;
    }

    private void init(final Context context) {
        mContext = context;
        mArgbEvaluator = new ArgbEvaluator();
        mTextColorMax = context.getResources().getColor(R.color.upgrade_item_max_color);
        mTextColorMin = context.getResources().getColor(R.color.upgrade_item_minx_color);
        mTitleColorMax = context.getResources().getColor(R.color.upgrade_item_title_color);

        mBallMaxX = context.getResources().getDimensionPixelSize(R.dimen.upgrade_head_ball_left_max);
        mBallMinX = context.getResources().getDimensionPixelSize(R.dimen.upgrade_head_ball_left);

        mY120 = context.getResources().getDimensionPixelSize(R.dimen.upgrade_list_item_max);
        mX62 = context.getResources().getDimensionPixelSize(R.dimen.upgrade_head_ball_left_max);
        mX13 = context.getResources().getDimensionPixelSize(R.dimen.upgrade_head_ball_left);
        mY80 = context.getResources().getDimensionPixelSize(R.dimen.upgrade_list_item_min);
        mMaxHeight = mY80;
        mMinxHeight = mY80;

        GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                int firstPosition = getFirstVisiblePosition();
                mIsFling = true;
//                LogUtil.d(TAG, "gestureListener onFling " + velocityX + ";" + velocityY + ";firstPosition=" + firstPosition);
                if (velocityY < 0) {
                    if (velocityY < -THRESHOLD_VELOCITY) {
                        smoothScrollToPositionFromTop(firstPosition + 2, 0, DURATION_LONG);
                    } else {
                        if (firstPosition <= 0 && getChildAt(firstPosition).getBottom() > mY80) {
                            smoothScrollToPositionFromTop(0, mY80 - mHeadLayoutParams.y, DURATION_LONG);
                        } else {
                            smoothScrollToPositionFromTop(firstPosition + 1, 0, DURATION_LONG);
                        }
                    }
                } else {
                    if (velocityY > THRESHOLD_VELOCITY) {
                        smoothScrollToPositionFromTop(0, 0, DURATION_LONG);
                    } else {
                        if (firstPosition <= 0) {
                            if (getChildAt(firstPosition).getBottom() > mY80) {
                                smoothScrollToPositionFromTop(0, 0, DURATION_LONG);
                            } else {
                                smoothScrollToPositionFromTop(0, mY80 - mHeadLayoutParams.y, DURATION_LONG);
                            }
                        } else {
                            if (firstPosition - 1 == 0) {
                                smoothScrollToPositionFromTop(firstPosition, 0, DURATION_LONG);
                            } else {
                                smoothScrollToPositionFromTop(firstPosition, 0, DURATION_LONG);
                            }
                        }
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//                LogUtil.d(TAG, "gestureListener onScroll " + distanceX + ";" + distanceY);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        };
        mGestureDetector = new GestureDetector(context, gestureListener);

        initListener(context);
    }

    private void initListener(final Context context) {
        setOnScrollListener(new OnScrollListener() {

            private int bottom;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int first = getFirstVisiblePosition();
                LogUtil.d(TAG, "onScrollStateChanged " + scrollState + " first =" + first);
                if (scrollState == SCROLL_STATE_IDLE) {
                    mScrollUp = false;
                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                LogUtil.d(TAG, "onScroll firstVisibleItem=" + firstVisibleItem + ";visibleItemCount=" + visibleItemCount + ";totalItemCount=" + totalItemCount);
                View child1 = view.getChildAt(0);
                if (child1 == null) {
                    return;
                }
                bottom = child1.getBottom();
                //头部可见
                if (firstVisibleItem <= 0) {
                    //头部
                    int top = view.getChildAt(firstVisibleItem).getTop();
                    int distance = Math.abs(top);
                    if (distance == 0) {
                        if (mScalableView != null) {
                            mScalableView.setScaleX(1.0f);
                            mScalableView.setScaleY(1.0f);
                            mScalableView.setTranslationX(0);
                            mScalableView.setTranslationY(0);
                        }
                        if (mBtnView != null) {
                            mBtnView.setAlpha(1.0f);
                        }
                    } else {
                        if (mScalableView != null && mHeadLayoutParams.y > 0 && distance != 0) {
//                            LogUtil.d(TAG, "onScroll setScaleX" + (mHeadLayoutParams.y - distance) * 1.0f / mHeadLayoutParams.y);
                            float scale = distance * 0.85f / (mHeadLayoutParams.y - mY120);
                            if (scale > 1.0f) {
                                scale = 1.0f;
                            }
                            mScalableView.setPivotX(mX62);
                            mScalableView.setPivotY(mScalableView.getMeasuredHeight());
                            mScalableView.setScaleY(1.0f - scale);
                            mScalableView.setScaleX(1.0f - scale);
                            //0.15≈40 * 1.0f / 285
                            if (1.0f - scale <= 0.15) {
                                mScalableView.setVisibility(View.INVISIBLE);
                                View ballView = findViewById(R.id.ll_ball);
                                if(ballView != null){
                                    ballView.setVisibility(View.VISIBLE);
                                }
                            } else {
                                mScalableView.setVisibility(View.VISIBLE);
                                View ballView = findViewById(R.id.ll_ball);
                                if(ballView != null){
                                    ballView.setVisibility(View.INVISIBLE);
                                }
                            }

                            if (mBtnView != null) {
                                if (distance <= mBtnView.getMeasuredHeight()) {
//                                    LogUtil.d(TAG, "onScroll mBtnView=" + (mBtnView.getMeasuredHeight() - distance) * 1.0f / mBtnView.getMeasuredHeight());
                                    mBtnView.setAlpha((mBtnView.getMeasuredHeight() - distance) * 1.0f / mBtnView.getMeasuredHeight());
                                } else {
                                    mBtnView.setAlpha(0.0f);
                                }
                            }
                        }
                    }
                }

//                LogUtil.d(TAG, "onScroll bottom=" + bottom);
                if (mY80<=bottom && bottom <= mY120) {
                    mHasResetted = false;
                    float fraction = (mY120 - bottom) * 1.0f / (mY120 - mY80);//缩放因子
                    int colorSubTitle = (int) mArgbEvaluator.evaluate(fraction, mTextColorMin, mTextColorMax);
                    int color1Title = (int) mArgbEvaluator.evaluate(fraction, mTextColorMax, mTitleColorMax);
//                    LogUtil.d(TAG, "onScroll  fraction=" + fraction);

                    if (firstVisibleItem <= 0) {
                        View ballView = findViewById(R.id.ll_ball);
                        if(ballView != null){
                            ballView.setTranslationX((mBallMinX - mBallMaxX) * fraction);
                            ballView.setTranslationY((mY120 - bottom) / 2);
                        }
                        View ballTview = findViewById(R.id.tv_ball_tips);
                        if(ballTview != null){
                            ballTview.setTranslationX((mBallMinX - mBallMaxX) * fraction);
                            ballTview.setTranslationY((mY120 - bottom) / 2);
                            ballTview.setVisibility(View.INVISIBLE);
                        }
                    }

                    View child2 = view.getChildAt(0 + 1);
                    if (child2 != null && child2 != mViewFooter) {
                        TextView tv1 = ((TextView) child2.findViewById(R.id.tv_subtitle));
                        tv1.setTextColor(colorSubTitle);
                        TextView tv = ((TextView) child2.findViewById(R.id.tv_title));
                        mMaxHeight = mY80 + Math.round((mY120 - mY80) * fraction);
                        tv1.setPivotX(0);
                        tv1.setPivotY(0);
                        float scale = (27 - 12) / 12 * 1.0f * fraction + 1;
                        if (scale < 1) {
                            scale = 1;
                        }
                        tv1.setScaleX(scale);
                        tv1.setScaleY(scale);
//                        tv1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12 + (27 - 12) * fraction);
                        if (tv != null) {
                            tv.setTextColor(color1Title);
                        }
                    }
                    requestLayout();
                }
                if (bottom <= mY80) {
                    mHasResetted = false;
                    float fraction = (mY80 - bottom) * 1.0f / (mY80);//缩放因子

                    if (firstVisibleItem <= 0 && mEnableTry) {
                        findViewById(R.id.tv_ball_tips).setVisibility(View.VISIBLE);
                    }

                    //second
                    int colorSub2 = (int) mArgbEvaluator.evaluate(fraction, mTextColorMax, mTextColorMin);
                    int colorTitle2 = (int) mArgbEvaluator.evaluate(fraction, mTitleColorMax, mTextColorMax);
                    View child2 = view.getChildAt(0 + 1);
                    if (child2 != null && child2 != mViewFooter) {
                        TextView tv1 = ((TextView) child2.findViewById(R.id.tv_subtitle));
                        tv1.setTextColor(colorSub2);
                        TextView tv = ((TextView) child2.findViewById(R.id.tv_title));
                        mMaxHeight = mY120 - Math.round((mY120 - mY80) * fraction);
                        tv1.setPivotX(0);
                        tv1.setPivotY(0);
                        float scale = 1 + (27 - 12) / 12 * 1.0f * (1 - fraction);
                        if (scale < 1) {
                            scale = 1;
                        }
                        tv1.setScaleX(scale);
                        tv1.setScaleY(scale);
                        if (tv != null) {
                            tv.setTextColor(colorTitle2);
                        }
                    }

                    //third
                    int colorSub3 = (int) mArgbEvaluator.evaluate(fraction, mTextColorMin, mTextColorMax);
                    int colorTitle3 = (int) mArgbEvaluator.evaluate(fraction, mTextColorMax, mTitleColorMax);
                    View child3 = view.getChildAt(0 + 2);
                    if (child3 != null && child3 != mViewFooter) {
                        TextView tv1 = ((TextView) child3.findViewById(R.id.tv_subtitle));
                        tv1.setTextColor(colorSub3);
                        TextView tv = ((TextView) child3.findViewById(R.id.tv_title));
                        mMinxHeight = mY80 + Math.round((mY120 - mY80) * fraction);
                        tv1.setPivotX(0);
                        tv1.setPivotY(0);
                        float scale = (27 - 12) / 12 * 1.0f * fraction + 1;
                        if (scale < 1) {
                            scale = 1;
                        }
                        tv1.setScaleX(scale);
                        tv1.setScaleY(scale);
//                        tv1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12 + (27 - 12) * fraction);
                        if (tv != null) {
                            tv.setTextColor(colorTitle3);
                        }
                    }
                    requestLayout();
                }
                if(mY120 <= bottom) {
                    //还原所有UI
                    resetNormal();
                    View tipView = findViewById(R.id.tv_ball_tips);
                    if (tipView != null && tipView.getVisibility() != View.INVISIBLE) {
                        tipView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
    }

    private void resetNormal() {
        LogUtil.d(TAG, "resetNormal mHasResetted=" + mHasResetted);
        if (mHasResetted) {
            return;
        }
        for (int i = 0; i <= getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != null && child != mViewHeader && child != mViewFooter) {
                AbsListView.LayoutParams layoutParams = new LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mY80);
                child.setLayoutParams(layoutParams);
                TextView tv1 = ((TextView) child.findViewById(R.id.tv_subtitle));
                tv1.setTextColor(mTextColorMin);
                TextView tv = ((TextView) child.findViewById(R.id.tv_title));
                tv.setTextColor(mTextColorMax);
                tv1.setScaleX(1.0f);
                tv1.setScaleY(1.0f);
            }
        }
        mHasResetted = true;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mViewHeader != null) {
            mHeadLayoutParams = new Point(mViewHeader.getMeasuredWidth(), mViewHeader.getMeasuredHeight());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        LogUtil.d(TAG, "onLayout " + changed + " mMaxHeight=" + mMaxHeight + " mMinxHeight=" + mMinxHeight);
        View layout2 = getChildAt(1);
        if (layout2 != null && layout2 != mViewFooter) {
            AbsListView.LayoutParams layoutParams = new LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mMaxHeight);
            layout2.setLayoutParams(layoutParams);
        }
        View layout3 = getChildAt(2);
        if (layout3 != null && layout3 != mViewFooter) {
            AbsListView.LayoutParams layoutParams = new LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mMinxHeight);
            layout3.setLayoutParams(layoutParams);
        }
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public void addHeaderView(View v) {
        super.addHeaderView(v);
        mViewHeader = v;
    }

    @Override
    public void addHeaderView(View v, Object data, boolean isSelectable) {
        super.addHeaderView(v, data, isSelectable);
        mViewHeader = v;
    }

    @Override
    public void addFooterView(View v) {
        super.addFooterView(v);
        mViewFooter = v;
    }

    @Override
    public void addFooterView(View v, Object data, boolean isSelectable) {
        super.addFooterView(v, data, isSelectable);
        mViewFooter = v;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        mGestureDetector.onTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                LogUtil.d(TAG, "onTouchEvent down");
                mStartPoint.x = x;
                mStartPoint.y = y;
                break;
            case MotionEvent.ACTION_MOVE:
//                LogUtil.d(TAG, "onTouchEvent move");
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
//                LogUtil.d(TAG, "onTouchEvent up");
                if (y - mStartPoint.y > 0) {
                    mScrollUp = false;
                } else {
                    mScrollUp = true;
                }
                LogUtil.d(TAG, "onTouchEvent  mScrollUp =" + mScrollUp);
                onTouchFinish(y - mStartPoint.y );
                mStartPoint.x = 0;
                mStartPoint.y = 0;
                mIsFling = false;
                mScrollUp = false;
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void smoothScrollToPositionFromTop(int position, int offset, int duration) {
        super.smoothScrollToPositionFromTop(position, offset, duration);
    }

    private void onTouchFinish(int distance) {
        if(mIsFling){
            return;
        }
		boolean forceRefresh = false;
        int firstPosition = getFirstVisiblePosition();
        forceRefresh = getChildAt(firstPosition).getBottom() != mY80;
        if(firstPosition <= 0){
            forceRefresh &= getChildAt(firstPosition).getBottom() != mHeadLayoutParams.y;
        }

        if(Math.abs(distance) <=  THRESHOLD_DISTANCE){
            if(!forceRefresh){
                return;
            }
        }
		
        LogUtil.d(TAG, "onTouchFinish firstPosition=" + firstPosition);
        if (mScrollUp) {
            if (firstPosition <= 0) {
                if (getChildAt(firstPosition).getBottom() > mY80) {
                    smoothScrollToPositionFromTop(0, mY80 - mHeadLayoutParams.y, DURATION_SHORT);
                    return;
                }
            }
            smoothScrollToPositionFromTop(firstPosition + 1, 0, DURATION_SHORT);
        }else {
            if (firstPosition <= 0) {
                if (getChildAt(firstPosition).getBottom() < mY80) {
                    smoothScrollToPositionFromTop(0, mY80 - mHeadLayoutParams.y, DURATION_SHORT);
                    return;
                }
            }
            smoothScrollToPositionFromTop(firstPosition, 0, DURATION_SHORT);
        }
    }
}
