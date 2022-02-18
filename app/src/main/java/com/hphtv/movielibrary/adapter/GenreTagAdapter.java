
package com.hphtv.movielibrary.adapter;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;


import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.RoundRectItemBinding;
import com.hphtv.movielibrary.ui.homepage.NewHomePageActivity;
import com.hphtv.movielibrary.ui.homepage.NewPageFragment;
import com.station.kit.util.LogUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Handler;

/**
 * author: Sam Leung
 * date:  2021/11/9
 */
public class GenreTagAdapter extends RoundRectItemAdapter {
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_ADD = 1;
    public GenreTagAdapter(Context context, List<String> list) {
        super(context, list);
        setZoomRatio(1.15f);
    }


    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            RoundRectItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.round_rect_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(binding);
            return viewHolder;
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
    public void onFocusChange(View v, boolean hasFocus) {
        super.onFocusChange(v, hasFocus);
        if(hasFocus){
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleApater.ViewHolder holder, int position) {
        holder.mBinding.getRoot().setTag(position);
        RoundRectItemBinding binding = (RoundRectItemBinding) holder.mBinding;
        if (getItemViewType(position) == TYPE_ADD) {
            binding.setTitle(mContext.getString(R.string.genre_add));
        } else {
            binding.setTitle(mList.get(position));
        }
    }

    private GenreListener mAddGenreListener;

    public interface GenreListener {
        void addGenre();
    }

    public void setOnGenreListener(GenreListener addGenreListener) {
        mAddGenreListener = addGenreListener;
    }

}
