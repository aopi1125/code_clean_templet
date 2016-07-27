package com.kodelabs.boilerplate.presentation.ui.view;

import android.content.Context;
import android.graphics.Point;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.List;

import lombok.Setter;
import lombok.experimental.Accessors;


public class PopupWindowViewPagerAdapter extends PagerAdapter {

    private SparseArray<List<ShareOtherDialog.ShareItem>> items = new SparseArray<>();
    private Context mContext;
    private Point mPoint = new Point();
    @Setter
    @Accessors(prefix = {"m", "b"})
    private int mColumnCount;

    public PopupWindowViewPagerAdapter(Context c, Point p) {
        this.mContext = c;
        this.mPoint = p;
    }

    public void setData(SparseArray<List<ShareOtherDialog.ShareItem>> datas) {
        this.items = datas;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view = createGridView(items, position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == (arg1);
    }

    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    private GridView createGridView(final SparseArray<List<ShareOtherDialog.ShareItem>> popupList, int position) {
        final List<ShareOtherDialog.ShareItem> pops = popupList.get(position);
        if(pops == null){
            return null;
        }
        final GridView view = new GridView(mContext);
        view.setVerticalSpacing(PopupWindowGrid.ITEM_DIVIDETH);
        PopupWindowGridItemAdapter adapter = new PopupWindowGridItemAdapter(mContext, pops);
        view.setAdapter(adapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long arg3) {
                ShareOtherDialog.ShareItem si = pops.get(position);
                if (si != null && si.clickListener != null) {
                    si.clickListener.onClick(view);
                }
            }
        });
        view.setLayoutParams(new GridView.LayoutParams(mPoint.x, mPoint.y));
        view.setNumColumns(mColumnCount);
        return view;
    }

}
