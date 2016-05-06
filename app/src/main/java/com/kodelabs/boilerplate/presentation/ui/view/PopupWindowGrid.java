package com.kdweibo.android.dailog;

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

import com.kdweibo.android.config.KdweiboApplication;
import com.kdweibo.android.ui.adapter.MyPagerAdapter;
import com.kdweibo.android.util.AndroidUtils;
import com.kdweibo.client.R;
import com.kingdee.eas.eclite.ui.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表项的长按弹出菜单
 */
public class PopupWindowGrid {

    private static final int itemViewWidth = AndroidUtils.Screen.dp2pix(59);//118
    private static final int itemViewHeight = AndroidUtils.Screen.dp2pix(62);//116
    private static final int itemDivideth = AndroidUtils.Screen.dp2pix(3);
    private static final int arrowWidth = AndroidUtils.Screen.dp2pix(20);//20dp
    private static final int windowCornersRadius = AndroidUtils.Screen.dp2pix(10);//10dp
    private static final int space_gap_down_length = KdweiboApplication.getContext().getResources().getDimensionPixelSize(R.dimen.popup_grid_gap);
    private static final int space_gap_up_length = KdweiboApplication.getContext().getResources().getDimensionPixelSize(R.dimen.popup_grid_gap_up);

    private int screenWidth = AndroidUtils.Screen.getDisplay()[0];
    private int screenHeight = AndroidUtils.Screen.getDisplay()[1];
    private int columnCount = 5;

    private Context mContext;
    private PopupWindow popupListWindow;

    private static float rawX;
    private static float rawY;

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
        //预防性Bug修复，详见http://blog.csdn.net/shangmingchao/article/details/50947418
        if (mContext instanceof Activity && ((Activity) mContext).isFinishing()) {
            return;
        }
        if (popupList == null || popupList.isEmpty()) {
            return;
        }

        ViewGroup layoutView = (ViewGroup) LayoutInflater.from(mContext).inflate(
                    R.layout.popup_grid, null);

        //ViewPager
        ViewPager mPager = (ViewPager) layoutView.findViewById(R.id.viewpager_popup);

        int size = popupList.size();
        int[] layoutPrms = initViewPager(mPager, size);

        ArrayList<View> listViews = new ArrayList<View>();
        for (int i = 0; i < size; i = i + columnCount * 2) {
            List<ShareOtherDialog.ShareItem> pops = new ArrayList<>();
            for (int j = 0; j < columnCount * 2; j++) {
                if (i + j < size) {
                    pops.add(popupList.get(i + j));
                }
            }
            //every page for GridView
            GridView view = createGridView(pops, layoutPrms);
            view.setNumColumns(columnCount);
            listViews.add(view);
        }
        mPager.setAdapter(new MyPagerAdapter((Activity) mContext, listViews));
        mPager.setCurrentItem(0);

        //计算出弹出窗口的宽高
        int PopupWindowWidth = layoutPrms[0]+ layoutView.getPaddingLeft() + layoutView.getPaddingRight();//左右留空隙不顶住edge
        int PopupWindowHeight = layoutPrms[1] + arrowWidth ;

        boolean isUp = initArrowView(layoutView, PopupWindowWidth, PopupWindowHeight);//箭头在上为true在下为false

        //实例化弹出窗口并显示
        popupListWindow = new PopupWindow(layoutView, PopupWindowWidth,
                PopupWindowHeight, true);
        popupListWindow.setAnimationStyle(R.style.adminlocation_popupwindow_anim);
        popupListWindow.setTouchable(true);
        //设置背景以便在外面包裹一层可监听触屏等事件的容器
        popupListWindow.setBackgroundDrawable(new BitmapDrawable());
        LogUtil.i("PopupWindowGrid", "rawx_rawY =" + rawX + ";" + rawY + ";" + isUp + ";itemHeight=" + itemViewHeight);
        int disY = isUp ? (int) rawY - screenHeight / 2 + PopupWindowHeight / 2 + space_gap_up_length : (int) rawY - screenHeight / 2 - PopupWindowHeight / 2 - space_gap_down_length * 2;
        popupListWindow.showAtLocation(parent, Gravity.CENTER,
                (int) rawX - screenWidth / 2, disY);
        LogUtil.i("PopupWindowGrid", "showAtLocation " + (rawX - screenWidth / 2) + ";" + disY);
    }


    private boolean initArrowView(View layoutView, int PopupWindowWidth, int PopupWindowHeight) {
        FrameLayout ll_up = (FrameLayout) layoutView.findViewById(R.id.ll_arrow_up);
        FrameLayout ll_down = (FrameLayout) layoutView.findViewById(R.id.ll_arrow_down);
        //为水平列表添加指示箭头，默认在列表的左下角，根据手指按下位置绝对坐标进行位置调整
        ImageView iv = new ImageView(mContext);
        float leftEdgeOffset = rawX;
        float rightEdgeOffset = screenWidth - rawX;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (leftEdgeOffset < PopupWindowWidth / 2) {
                if (leftEdgeOffset < arrowWidth / 2.0) {
                    iv.setTranslationX(windowCornersRadius);
                } else {
                    iv.setTranslationX(leftEdgeOffset - arrowWidth / 2.0f);
                }
            } else if (rightEdgeOffset < PopupWindowWidth / 2) {
                if (rightEdgeOffset < arrowWidth / 2.0f) {
                    iv.setTranslationX(PopupWindowWidth - rightEdgeOffset - arrowWidth / 2.0f - windowCornersRadius);
                } else {
                    iv.setTranslationX(PopupWindowWidth - rightEdgeOffset - arrowWidth / 2.0f);
                }
            } else {
                iv.setTranslationX(PopupWindowWidth / 2 - arrowWidth / 2.0f);
            }
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(arrowWidth, arrowWidth);
        boolean isUp = false;
        if (rawY > PopupWindowHeight * 1.15f) {
            iv.setBackgroundResource(R.drawable.bg_popup_grid_arrowdown);
            ll_down.addView(iv, layoutParams);
            ll_down.setVisibility(View.VISIBLE);
            isUp = false;
        } else {
            iv.setBackgroundResource(R.drawable.bg_popup_grid_arrowup);
            ll_up.addView(iv, layoutParams);
            ll_up.setVisibility(View.VISIBLE);
            isUp = true;
        }
        return isUp;
    }


    //计算出ViewPager的宽高
    private int[] initViewPager(ViewPager vp, int size) {
        int width;
        int height;
        int padding = vp.getPaddingLeft();
        if (size <= columnCount) {
            columnCount = size;//显示多余空白
            width = padding * 2 + itemViewWidth * size;
            height = padding * 2 + itemViewHeight;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
            vp.setLayoutParams(lp);
        } else {
            width = padding * 2 + itemViewWidth * columnCount;
            height = padding * 2 + itemViewHeight * 2 + itemDivideth;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, height);
            vp.setLayoutParams(lp);
        }
        LogUtil.i("PopupWindowGrid", "initViewPager width_height =" + width + ";" + height);
        return new int[]{width, height};
    }

    private GridView createGridView(final List<ShareOtherDialog.ShareItem> popupList, int[] layoutPrms) {
        final GridView view = new GridView(mContext);
        view.setHorizontalSpacing(itemDivideth);
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
        if (popupListWindow != null && popupListWindow.isShowing()) {
            popupListWindow.dismiss();
        }
    }

    public void reset() {
        rawX = 0;
        rawY = 0;
        columnCount = 5;
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        if(popupListWindow == null){
            return;
        }
        popupListWindow.setOnDismissListener(listener);
    }

    public static boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            rawX = event.getRawX();
            rawY = event.getRawY();
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
                GridView.LayoutParams lp = new GridView.LayoutParams(itemViewWidth, itemViewHeight);
                LogUtil.i("PopupWindowGrid", "getView width_height =" + itemViewWidth + ";" + itemViewHeight);
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
