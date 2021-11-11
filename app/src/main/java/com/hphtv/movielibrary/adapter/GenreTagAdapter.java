
package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.RoundRectItem2Binding;
import com.hphtv.movielibrary.databinding.RoundRectItemBinding;
import com.station.kit.util.LogUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/11/9
 */
public class GenreTagAdapter extends RoundRectItemAdapter {
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_ADD = 1;

    public GenreTagAdapter(Context context, List<String> list) {
        super(context, list);
    }


    @Override
    protected int getBaseItemLayoutId() {
        return super.getBaseItemLayoutId();
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD) {
            RoundRectItem2Binding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.round_rect_item_2, parent, false);
            ViewHolder viewHolder = new ViewHolder(binding);
            return viewHolder;
        } else {
            RoundRectItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.round_rect_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(binding);
            return viewHolder;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount()-1)
            return TYPE_ADD;
        return TYPE_ITEM;
    }


    @Override
    public int getItemCount() {
        return super.getItemCount()+1;
    }

    @Override
    public void onClick(View v) {
        int postion = (int) v.getTag();
        if(getItemViewType(postion)==TYPE_ITEM)
            super.onClick(v);
        else if(getItemViewType(postion)==TYPE_ADD){
            if(mAddGenreListener!=null){
                mAddGenreListener.addGenre();
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseAdapter2.ViewHolder holder, int position) {
        holder.mBinding.getRoot().setTag(position);
        if (getItemViewType(position) == TYPE_ADD) {
            RoundRectItem2Binding binding = (RoundRectItem2Binding) holder.mBinding;
        } else {
            RoundRectItemBinding binding = (RoundRectItemBinding) holder.mBinding;
            binding.setTitle(mList.get(position));
        }
    }

    private AddGenreListener mAddGenreListener;

    public interface AddGenreListener{
        void addGenre();
    }

    public void setAddGenreListener(AddGenreListener addGenreListener) {
        mAddGenreListener = addGenreListener;
    }
}
