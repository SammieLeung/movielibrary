package com.hphtv.movielibrary.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hphtv.movielibrary.R;

import java.util.List;

/**
 * Created by tchip on 17-12-19.
 */

public class LocalSearchAdapter extends RecyclerView.Adapter<LocalSearchAdapter.ViewHolder> {

    private static final String TAG = "LocalSearchAdapter";
    private Context context;


    private List<Object[]> list;

    public LocalSearchAdapter(Context context, List<Object[]> datas) {
        this.list = datas;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.keyboard_btn_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int left = v.getLeft();
                int top = v.getTop();
                int w = v.getMeasuredWidth();
                int h=v.getMeasuredHeight();
                Log.v(TAG, "click left=" + left + " top=" + top + " width=" + w+" height="+h);
                if (mOnKeyBoardClickListener != null) {
                    mOnKeyBoardClickListener.OnKeyBoardClick(v, (Integer) v.getTag(), list.get((Integer) v.getTag()));
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StringBuffer buttonText = new StringBuffer();
        Object[] dataArray = list.get(position);
        if (dataArray instanceof String[]) {
            if (dataArray.length == 1) {
                holder.single_num.setText((String) dataArray[0]);
            } else {
                for (int i = 0; i < dataArray.length; i++) {
                    String item = (String) dataArray[i];
                    String s = "";
                    if (i == 0) {
                        holder.num.setText(item);
                    } else {
                        s = item;
                    }
                    buttonText.append(s);

                }
                holder.text.setText(Html.fromHtml(buttonText.toString()));
            }
        } else if (dataArray instanceof Integer[]) {
            if (dataArray.length > 0) {
                int resId = (int) dataArray[0];
                holder.img.setImageResource(resId);
            }
        }
        holder.itemView.setTag(position);


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView num;
        TextView text;
        TextView single_num;
        ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            num = (TextView) itemView.findViewById(R.id.kbi_num);
            text = (TextView) itemView.findViewById(R.id.kbi_text);
            img = (ImageView) itemView.findViewById(R.id.kbi_img);
            single_num = (TextView) itemView.findViewById(R.id.kbi_single_num);
        }
    }

    public interface OnKeyBoardClickListener {
        public void OnKeyBoardClick(View view, int pos, Object[] keyValues);
    }

    OnKeyBoardClickListener mOnKeyBoardClickListener;

    public void setOnKeyBoardClickListener(OnKeyBoardClickListener listener) {
        mOnKeyBoardClickListener = listener;
    }
}
