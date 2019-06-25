package com.hphtv.movielibrary.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.view.RecyclerViewWithMouseScroll;

import java.util.List;

/**
 * Created by tchip on 18-1-5.
 */

public class FileChooserListAdapter extends RecyclerViewWithMouseScroll.Adapter<FileChooserListAdapter.ViewHolder> {
    private Context context;
    private List<String> child_folders;
    private String current_path;
    private int deep=0;

    public FileChooserListAdapter(Context context, List<String> child_folders) {
        this.context = context;
        this.child_folders = child_folders;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.file_chooser_listitem, null);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getTag() == null) {
                    if (backListener != null) {
                        backListener.OnItemClick(current_path);
                    }
                } else {
                    if (listener != null) {
                        listener.OnItemClick((String) view.getTag(), current_path);
                    }
                }

            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String filename = child_folders.get(position);
        if (deep==0&&position == 0) {
            holder.itemView.setTag(null);
            holder.textView.setText(filename);
        } else {
            holder.itemView.setTag(filename);
            holder.textView.setText(filename);
        }

    }

    @Override
    public int getItemCount() {
        return child_folders.size();
    }

    public class ViewHolder extends RecyclerViewWithMouseScroll.ViewHolder {
        TextView textView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tv_folder_name);
            imageView = (ImageView) itemView.findViewById(R.id.img_icon);
        }
    }

    public void setData(List<String> child_folders, String parent_path, int deep) {
        if (deep<0||child_folders == null) return;
        current_path = parent_path;
        this.child_folders = child_folders;
        this.deep=deep;
        if (deep != 0){
            this.child_folders.add(0, context.getResources().getString(R.string.filelist_back));
        }
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        public void OnItemClick(String filename, String path);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public interface OnItemBackClickListener {
        public void OnItemClick(String path);
    }

    private OnItemBackClickListener backListener;


    public void setOnItemBackClickListener(OnItemBackClickListener listener) {
        this.backListener = listener;
    }


    public String getCurrent_path() {
        return current_path;
    }

    public int getDeepth() {
        return deep;
    }
}
