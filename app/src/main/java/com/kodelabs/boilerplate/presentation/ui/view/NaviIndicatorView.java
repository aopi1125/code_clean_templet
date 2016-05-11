package com.kodelabs.boilerplate.presentation.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;

/**
 * 滑动导航页的位置圆点指示器
 * 
 */
public class NaviIndicatorView extends View {

	private Context context;

	private int width, height;
	private Paint mPaint;

	private int position;
	private float positionOffset;

	/* 此值为启动导航页的 */
	private static final int DEFAULT_TAB_NORMAL = AndroidUtils.appCtx().getResources().getColor(R.color.dividing_line);// 默认色
	private static final int DEFAULT_TAB_SELECTED = AndroidUtils.appCtx().getResources().getColor(R.color.accent_fc5);// 选中色
	private static final int DEFAULT_TAB_STOKEN = 10;// 圆之间间距
	private static final int DEFAULT_TAB_RADIUS = 10;// 圆半径

	private int tabNormalColor;
	private int tabSelectedColor;
	private int tabRadius;
	private int tabStoken;
	private int mSize;
	private int mBottomMargin;

	private ViewPager mContentView;

	public NaviIndicatorView(Context context, int resourceId) {
		super(context);
		this.context = context;
		initIndicator();
		initPaint();
	}

	public NaviIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initIndicator();
		initPaint();
	}

	private void initPaint() {
		if (mPaint == null) {
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setColor(tabNormalColor);
			mPaint.setStyle(Paint.Style.STROKE);
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mSize <= 0)
			return;
		if (mBottomMargin <= 0) {
			mBottomMargin = 2 * tabStoken;
		}
		int left = 0;
		int circleCenterX = 0;
		mPaint.setColor(tabNormalColor);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setStrokeWidth(2);

		if (mSize % 2 == 0) {
			// 偶数个，第一个圆点位置
			left = width / 2 - mSize / 2 * (tabStoken + 2 * tabRadius)
					+ tabStoken / 2 + tabRadius;
			circleCenterX = left;
			for (int i = 1; i <= mSize; i++) {
				canvas.drawCircle(circleCenterX, height - mBottomMargin
						- tabRadius, tabRadius, mPaint);
				circleCenterX += tabStoken + 2 * tabRadius;
			}
		} else {
			// 奇数个，第一个圆点位置
			left = width / 2 - (mSize - 1) / 2 * (tabStoken + 2 * tabRadius);
			circleCenterX = left;
			for (int i = 1; i <= mSize; i++) {
				canvas.drawCircle(circleCenterX, height - mBottomMargin
						- tabRadius, tabRadius, mPaint);
				circleCenterX += tabStoken + 2 * tabRadius;
			}
		}
		// 当前位置圆点
		mPaint.setColor(tabSelectedColor);
		mPaint.setStyle(Paint.Style.FILL);
		canvas.drawCircle(left + position * (tabStoken + 2 * tabRadius)
				+ positionOffset * (tabStoken + 2 * tabRadius), height
				- mBottomMargin - tabRadius, tabRadius, mPaint);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (width <= 0) {
			width = this.getWidth();
		}
		if (height <= 0) {
			height = this.getHeight();
		}
	}

	/**
	 * 供pageScroll时调用
	 * 
	 * @param position
	 * @param positionOffset
	 */
	public void onScroll(int position, float positionOffset) {
		this.position = position;
		this.positionOffset = positionOffset;
		postInvalidate();
	}

	/**
	 * 将下方指示器回复到系统默认的状态
	 */
	private void initIndicator() {
		tabNormalColor = DEFAULT_TAB_NORMAL;
		tabSelectedColor = DEFAULT_TAB_SELECTED;
		tabRadius = DEFAULT_TAB_RADIUS;
		tabStoken = DEFAULT_TAB_STOKEN;
	}

	/**
	 * 指示器平常的圆点颜色
	 * 
	 * @param tabNormalColor
	 */
	public void setCircleNormalColor(int tabNormalColor) {
		this.tabNormalColor = tabNormalColor;
	}

	/**
	 * 展示页的显示圆点颜色
	 * 
	 * @param tabSelectedColor
	 */
	public void setCircleSelectedColor(int tabSelectedColor) {
		this.tabSelectedColor = tabSelectedColor;
	}

	/**
	 * 指示器圆点的半径
	 * 
	 * @param tabRadius
	 */
	public void setCircleRadius(int tabRadius) {
		this.tabRadius = tabRadius;
	}

	/**
	 * 指示器远点之间的距离
	 * 
	 * @param tabStoken
	 */
	public void setCircleStoken(int tabStoken) {
		this.tabStoken = tabStoken;
	}

	/**
	 * 总页数
	 * 
	 * @param mSize
	 */
	public void setCirclesCounts(int mSize) {
		this.mSize = mSize;
	}

	/**
	 * 距离底部距离
	 * 
	 * @param mBottomMargin
	 */
	public void setBottomMargin(int mBottomMargin) {
		this.mBottomMargin = mBottomMargin;
	}

	/**
	 * 若外部需要设置OnPageChangeListener，则在外部onPageScrolled调用onScroll(position, positionOffset)方法
	 * @param contentView
	 */
	public void setContentView(ViewPager contentView) {
		this.mContentView = contentView;
		if (mContentView != null) {
			mContentView.setOnPageChangeListener(new MyPageChangeListener());
		}
	}

	class MyPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			onScroll(position, positionOffset);
		}

		@Override
		public void onPageSelected(int arg0) {
		}
	}

}
