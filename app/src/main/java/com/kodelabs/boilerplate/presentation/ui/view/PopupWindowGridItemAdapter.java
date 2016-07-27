package com.kodelabs.boilerplate.presentation.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.client.R;
import com.eas.eclite.ui.utils.LogUtil;

import java.util.List;

/**
 * Created on 16/7/27.
 */
public class PopupWindowGridItemAdapter extends BaseAdapter {

    private List<ShareOtherDialog.ShareItem> items = null;
    private Context mContext;
    private  GridView.LayoutParams mLp = new GridView.LayoutParams(PopupWindowGrid.ITEMVIEW_WIDTH, PopupWindowGrid.ITEMVIEW_HEIGHT);

    public PopupWindowGridItemAdapter(Context c, List<ShareOtherDialog.ShareItem> list) {
        this.items = list;
        this.mContext = c;
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
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LogUtil.i("PopupWindowGrid", "getView width_height =" + PopupWindowGrid.ITEMVIEW_WIDTH + ";" + PopupWindowGrid.ITEMVIEW_HEIGHT);
        convertView.setLayoutParams(mLp);
        holder.icon.setImageResource(items.get(position).iconRid);
        holder.text.setText(items.get(position).textRid);

        return convertView;
    }

    class ViewHolder {
        private ImageView icon;
        private TextView text;

        public ViewHolder(View convertView) {
            icon = (ImageView) convertView.findViewById(R.id.iv_img);
            text = (TextView) convertView.findViewById(R.id.tv_name);
        }
    }
}
