package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.util.DeviceUtil;

import java.util.HashMap;
import java.util.List;


/**
 * Created by tchip on 17-10-17.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> implements View.OnClickListener {
    private static final String TAG = "MovieLibraryAdapter";
    private Context context;
    private List<HashMap<String, Object>> list;
    private ProgressBar pbSearch;
    public static final String CUSTOM_ADD="cuistom_add";

    public DeviceAdapter(Context context, List<HashMap<String, Object>> datas) {
        this.list = datas;
        this.context = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.device_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        view.setOnClickListener(this);
        vh.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ViewCompat.animate(v).scaleX(1.04f).scaleY(1.04f).translationZ(1.0f).start();
                } else {
                    ViewCompat.animate(v).scaleX(1f).scaleY(1f).translationZ(0).start();
                }
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        HashMap<String, Object> map = list.get(i);
        if(map.get(CUSTOM_ADD)!=null&&(boolean)map.get(CUSTOM_ADD)){
            viewHolder.mTitle.setText(context.getResources().getString(R.string.custom_add));
            viewHolder.mImage.setImageDrawable(context.getResources().getDrawable(R.mipmap.add_device));
            return;
        }
        viewHolder.mImage.setImageDrawable(context.getResources().getDrawable(R.mipmap.usb));
        viewHolder.mTitle.setText((String) map.get(DeviceUtil.USB_LABEL));
        //保存数据到view的Tag上
        viewHolder.itemView.setTag(map);
        if ((boolean) map.get(DeviceUtil.PROGRESS_BAR)) {
            pbSearch = viewHolder.mPb;
            pbSearch.setVisibility(View.VISIBLE);
        }else{
            pbSearch = viewHolder.mPb;
            pbSearch.setVisibility(View.GONE);
        }
    }

    public ProgressBar getSearchProgressBar() {
        if (list.size() == 1 && (boolean) list.get(0).get(DeviceUtil.PROGRESS_BAR)) {
            return pbSearch;
        }
        return null;
    }

    public void addCustomAddButton() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(CUSTOM_ADD,true);
        list.add(map);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onClick(View v) {
        if (this.mListener != null) {
            this.mListener.OnItemClick(v, (HashMap<String, Object>) v.getTag());
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTitle;
        ImageView mImage;
        ProgressBar mPb;

        public ViewHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.tv_title);
            mImage = (ImageView) itemView.findViewById(R.id.iv_img);
            mPb = (ProgressBar) itemView.findViewById(R.id.pb_search);
        }
    }

    OnItemClickListener mListener;

    public interface OnItemClickListener {
        public void OnItemClick(View view, HashMap<String, Object> map);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    /**
     * 添加数据
     *
     * @param map
     * @param position
     */
    public void addItem(HashMap<String, Object> map, int position) {
        list.add(position, map);
        notifyItemInserted(position); // Attention!
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }


}
