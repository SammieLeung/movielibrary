package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.ConstData;

import java.util.HashMap;
import java.util.List;

/**
 * Created by tchip on 18-3-1.
 */

public class LeftMenuListAdapter extends BaseAdapter {

    private List<HashMap<String, Object>> rawDataList;
    private Context context;

    public LeftMenuListAdapter(Context context, List<HashMap<String, Object>> rawDataList) {
        this.rawDataList = rawDataList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return rawDataList.size();
    }


    @Override
    public HashMap<String, Object> getItem(int position) {
        return rawDataList.get(position);
    }

    public int getIconResourceId(int position) {
        return (int) rawDataList.get(position).get(ConstData.ICON);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.left_menu_item, parent, false);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.lmi_icon);
            viewHolder.title = (TextView) convertView.findViewById(R.id.lmi_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.icon.setImageResource(getIconResourceId(position));
        viewHolder.title.setText((String) getItem(position).get(ConstData.TEXT));

        return convertView;
    }

    public class ViewHolder {
        public TextView title;
        public ImageView icon;
    }
}
