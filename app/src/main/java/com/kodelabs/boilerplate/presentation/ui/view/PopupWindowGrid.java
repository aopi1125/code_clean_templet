package com.kodelabs.boilerplate.presentation.ui.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.android.event.PopUpWindowEvent;
import com.android.util.AndroidUtils;
import com.android.util.BusProvider;
import com.client.R;
import com.eas.eclite.ui.utils.LogUtil;
import com.squareup.otto.Subscribe;

import com.android.event.PopUpWindowEvent;
import com.android.ui.adapter.MyPagerAdapter;
import com.android.util.AndroidUtils;
import com.android.util.BusProvider;
import com.client.R;
import com.eas.eclite.ui.utils.LogUtil;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表项的长按弹出菜单
 */
public class PopupWindowGrid {

    public static final int ITEMVIEW_WIDTH = AndroidUtils.Screen.dp2pix(59);//118
    public static final int ITEMVIEW_HEIGHT = AndroidUtils.Screen.dp2pix(62);//116
    public static final int ITEM_DIVIDETH = AndroidUtils.Screen.dp2pix(3);
    private static final int ARROW_WIDTH = AndroidUtils.Screen.dp2pix(20);//20dp
    private static final int WINDOW_CORNERS_RADIUS = AndroidUtils.Screen.dp2pix(10);//10dp

    private Context mContext;
    private PopupWindow mPopupListWindow;
    private ViewPager mPager;
    private View mLayoutView;
    private FrameLayout mFlayoutUp;
    private FrameLayout mFlayoutDown;
    private PopupWindowViewPagerAdapter mMyPagerAdapter;

    private int mScreenWidth = AndroidUtils.Screen.getDisplay()[0];
    private int mScreenHeight = AndroidUtils.Screen.getDisplay()[1];
    private int mColumnCount = 5;
    private boolean bUnRegistered = true;
    private SparseArray<List<ShareOtherDialog.ShareItem>> mPagerItems;

    private static float sTouchRawX;
    private static float sTouchRawY;

    public PopupWindowGrid(Context context) {
        super();
        this.mContext = context;
    }

    /**
     * 在手指按下的位置显示弹出菜单
     *
     * @param parent    view，同时也是用来寻找窗口token的view，此处为ListView
     * @param popupList 要显示的列表
     */
    public void showPopupWindow(View parent, List<ShareOtherDialog.ShareItem> popupList) {

        if (bUnRegistered) {
            BusProvider.getInstance().register(this);
            bUnRegistered = false;
        }

        //预防性Bug修复，详见http://blog.csdn.net/shangmingchao/article/details/50947418
        if (mContext instanceof Activity && ((Activity) mContext).isFinishing()) {
            return;
        }
        if (popupList == null || popupList.isEmpty()) {
            return;
        }

        Point point = initLayout(popupList);

        //计算出弹出窗口的宽高
        int popupWindowWidth = point.x + mLayoutView.getPaddingLeft() + mLayoutView.getPaddingRight();//左右留空隙不顶住edge
        int popupWindowHeight = point.y + ARROW_WIDTH;
        boolean isUp = initArrowView(popupWindowWidth, popupWindowHeight);//箭头在上为true在下为false
        int disY = isUp ? (int) sTouchRawY : (int) sTouchRawY - popupWindowHeight;

        mPopupListWindow = new PopupWindow(mLayoutView, popupWindowWidth, popupWindowHeight, true);
        mPopupListWindow.setAnimationStyle(R.style.adminlocation_popupwindow_anim);
        mPopupListWindow.setTouchable(true);
        //设置背景以便在外面包裹一层可监听触屏等事件的容器
        mPopupListWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupListWindow.showAtLocation(parent, Gravity.NO_GRAVITY, (int) sTouchRawX - popupWindowWidth / 2, disY);
    }

    private Point initLayout(List<ShareOtherDialog.ShareItem> popupList) {
        if (mLayoutView == null) {
            mLayoutView = LayoutInflater.from(mContext).inflate(R.layout.popup_grid, null);
        }
        //ViewPager
        if (mPager == null) {
            mPager = (ViewPager) mLayoutView.findViewById(R.id.viewpager_popup);
        }
        if (mFlayoutUp == null) {
            mFlayoutUp = (FrameLayout) mLayoutView.findViewById(R.id.ll_arrow_up);
        }
        mFlayoutUp.removeAllViews();
        if (mFlayoutDown == null) {
            mFlayoutDown = (FrameLayout) mLayoutView.findViewById(R.id.ll_arrow_down);
        }
        mFlayoutDown.removeAllViews();

        int size = popupList.size();
        Point point = initViewPager(mPager, size);
        if(mPagerItems == null){
            mPagerItems = new SparseArray<>();
        }else{
            mPagerItems.clear();
        }
        int index = 0;
        for (int i = 0; i < size; i = i + mColumnCount * 2) {
            List<ShareOtherDialog.ShareItem> pops = new ArrayList<>();
            for (int j = 0; j < mColumnCount * 2; j++) {
                if (i + j < size) {
                    pops.add(popupList.get(i + j));
                }
            }
            mPagerItems.put(index++, pops);
        }
        if(mMyPagerAdapter == null){
            mMyPagerAdapter = new PopupWindowViewPagerAdapter(mContext, point);
            mMyPagerAdapter.setColumnCount(mColumnCount);
            mMyPagerAdapter.setData(mPagerItems);
            mPager.setAdapter(mMyPagerAdapter);
        }else{
            mMyPagerAdapter.setColumnCount(mColumnCount);
            mMyPagerAdapter.setData(mPagerItems);
            mMyPagerAdapter.notifyDataSetChanged();
        }
        mPager.setCurrentItem(0);
        return point;
    }

    private boolean initArrowView(int PopupWindowWidth, int PopupWindowHeight) {
        //为水平列表添加指示箭头，默认在列表的左下角，根据手指按下位置绝对坐标进行位置调整
        ImageView iv = new ImageView(mContext);
        float leftEdgeOffset = sTouchRawX;
        float rightEdgeOffset = mScreenWidth - sTouchRawX;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (leftEdgeOffset < PopupWindowWidth / 2) {
                if (leftEdgeOffset < ARROW_WIDTH / 2.0) {
                    iv.setTranslationX(WINDOW_CORNERS_RADIUS);
                } else {
                    iv.setTranslationX(leftEdgeOffset - ARROW_WIDTH / 2.0f);
                }
            } else if (rightEdgeOffset < PopupWindowWidth / 2) {
                if (rightEdgeOffset < ARROW_WIDTH / 2.0f) {
                    iv.setTranslationX(PopupWindowWidth - rightEdgeOffset - ARROW_WIDTH / 2.0f - WINDOW_CORNERS_RADIUS);
                } else {
                    iv.setTranslationX(PopupWindowWidth - rightEdgeOffset - ARROW_WIDTH / 2.0f);
                }
            } else {
                iv.setTranslationX(PopupWindowWidth / 2 - ARROW_WIDTH / 2.0f);
            }
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ARROW_WIDTH, ARROW_WIDTH);
        boolean isUp;
        if (sTouchRawY > PopupWindowHeight * 1.15f) {
            iv.setBackgroundResource(R.drawable.bg_popup_grid_arrowdown);
            mFlayoutDown.addView(iv, layoutParams);
            mFlayoutDown.setVisibility(View.VISIBLE);
            isUp = false;
        } else {
            iv.setBackgroundResource(R.drawable.bg_popup_grid_arrowup);
            mFlayoutUp.addView(iv, layoutParams);
            mFlayoutUp.setVisibility(View.VISIBLE);
            isUp = true;
        }
        return isUp;
    }


    //计算出ViewPager的宽高
    private Point initViewPager(ViewPager vp, int size) {
        int width;
        int height;
        int padding = vp.getPaddingLeft();
        if (size <= mColumnCount) {
            mColumnCount = size;//显示多余空白
            width = padding * 2 + ITEMVIEW_WIDTH * size;
            height = padding * 2 + ITEMVIEW_HEIGHT;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
            vp.setLayoutParams(lp);
        } else {
            width = padding * 2 + ITEMVIEW_WIDTH * mColumnCount;
            height = padding * 2 + ITEMVIEW_HEIGHT * 2 + ITEM_DIVIDETH;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
            vp.setLayoutParams(lp);
        }
        LogUtil.i("PopupWindowGrid", "initViewPager width_height =" + width + ";" + height);
        Point p = new Point();
        p.x = width;
        p.y = height;
        return p;
    }

    /**
     * 隐藏气泡式弹出菜单PopupWindow
     */
    public void hiddenPopupWindow() {
        reset();

        if (mContext != null && mContext instanceof Activity && ((Activity) mContext).isFinishing()) {
            return;
        }
        if (mPopupListWindow != null && mPopupListWindow.isShowing()) {
            mPopupListWindow.dismiss();
        }
    }

    public void reset() {
        sTouchRawX = 0;
        sTouchRawY = 0;
        mColumnCount = 5;
        if (!bUnRegistered) {
            BusProvider.getInstance().unregister(this);
            bUnRegistered = true;
        }
    }

    @Subscribe
    public void onPopUpWindowStated(PopUpWindowEvent event) {
        hiddenPopupWindow();
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        if (mPopupListWindow == null) {
            return;
        }
        mPopupListWindow.setOnDismissListener(listener);
    }

    public static boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            sTouchRawX = event.getRawX();
            sTouchRawY = event.getRawY();
        }
        return false;
    }

}
