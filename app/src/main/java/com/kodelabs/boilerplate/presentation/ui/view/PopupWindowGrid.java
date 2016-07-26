package com.kodelabs.boilerplate.presentation.ui.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

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

    private static final int ITEMVIEW_WIDTH = AndroidUtils.Screen.dp2pix(59);//118
    private static final int ITEMVIEW_HEIGHT = AndroidUtils.Screen.dp2pix(62);//116
    private static final int ITEM_DIVIDETH = AndroidUtils.Screen.dp2pix(3);
    private static final int ARROW_WIDTH = AndroidUtils.Screen.dp2pix(20);//20dp
    private static final int WINDOW_CORNERS_RADIUS = AndroidUtils.Screen.dp2pix(10);//10dp

    private int mScreenWidth = AndroidUtils.Screen.getDisplay()[0];
    private int mScreenHeight = AndroidUtils.Screen.getDisplay()[1];
    private int mColumnCount = 5;

    private boolean bUnRegistered = true;

    private Context mContext;
    private PopupWindow mPopupListWindow;
    private ViewPager mPager;
    private View mLayoutView;
    private FrameLayout mFlayoutUp;
    private FrameLayout mFlayoutDown;

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

        int[] layoutPrms = initLayout(popupList);

        //计算出弹出窗口的宽高
        int popupWindowWidth = layoutPrms[0] + mLayoutView.getPaddingLeft() + mLayoutView.getPaddingRight();//左右留空隙不顶住edge
        int popupWindowHeight = layoutPrms[1] + ARROW_WIDTH;
        boolean isUp = initArrowView(popupWindowWidth, popupWindowHeight);//箭头在上为true在下为false
        int disY = isUp ? (int) sTouchRawY : (int) sTouchRawY - popupWindowHeight;

        mPopupListWindow = new PopupWindow(mLayoutView, popupWindowWidth, popupWindowHeight, true);
        mPopupListWindow.setAnimationStyle(R.style.adminlocation_popupwindow_anim);
        mPopupListWindow.setTouchable(true);
        //设置背景以便在外面包裹一层可监听触屏等事件的容器
        mPopupListWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupListWindow.showAtLocation(parent, Gravity.NO_GRAVITY, (int) sTouchRawX - popupWindowWidth / 2, disY);
    }

    private int[] initLayout(List<ShareOtherDialog.ShareItem> popupList) {
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
        List<View> listViews = new ArrayList<View>();
        

        int size = popupList.size();
        int[] layoutPrms = initViewPager(mPager, size);
        for (int i = 0; i < size; i = i + mColumnCount * 2) {
            List<ShareOtherDialog.ShareItem> pops = new ArrayList<>();
            for (int j = 0; j < mColumnCount * 2; j++) {
                if (i + j < size) {
                    pops.add(popupList.get(i + j));
                }
            }
            //every page for GridView
            GridView view = createGridView(pops, layoutPrms);
            view.setNumColumns(mColumnCount);
            listViews.add(view);
        }
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter((Activity) mContext, listViews);
        mPager.setAdapter(myPagerAdapter);
       
        mPager.setCurrentItem(0);
        return layoutPrms;
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
    private int[] initViewPager(ViewPager vp, int size) {
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
        return new int[]{width, height};
    }

    private GridView createGridView(final List<ShareOtherDialog.ShareItem> popupList, int[] layoutPrms) {
        final GridView view = new GridView(mContext);
        view.setVerticalSpacing(ITEM_DIVIDETH);
        ShareOtherAdapter adapter = new ShareOtherAdapter(popupList);
        view.setAdapter(adapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long arg3) {
                ShareOtherDialog.ShareItem si = popupList.get(position);
                if (si != null && si.clickListener != null) {
                    si.clickListener.onClick(view);
                }
            }
        });
        view.setLayoutParams(new GridView.LayoutParams(layoutPrms[0], layoutPrms[1]));
        return view;
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

    protected class ShareOtherAdapter extends BaseAdapter {

        private List<ShareOtherDialog.ShareItem> items = null;

        public ShareOtherAdapter(List<ShareOtherDialog.ShareItem> list) {
            this.items = list;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_menu_long, null);
                holder = new ViewHolder(convertView);
                GridView.LayoutParams lp = new GridView.LayoutParams(ITEMVIEW_WIDTH, ITEMVIEW_HEIGHT);
                LogUtil.i("PopupWindowGrid", "getView width_height =" + ITEMVIEW_WIDTH + ";" + ITEMVIEW_HEIGHT);
                convertView.setLayoutParams(lp);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.icon.setImageResource(items.get(position).iconRid);
            holder.text.setText(items.get(position).textRid);

            return convertView;
        }
    }

    private class ViewHolder {
        private ImageView icon;
        private TextView text;

        public ViewHolder(View convertView) {
            icon = (ImageView) convertView.findViewById(R.id.iv_img);
            text = (TextView) convertView.findViewById(R.id.tv_name);
        }
    }

}
