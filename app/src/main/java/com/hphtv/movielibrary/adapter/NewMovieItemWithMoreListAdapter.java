package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

import com.hphtv.movielibrary.databinding.PosterMoreItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class NewMovieItemWithMoreListAdapter extends NewMovieItemListAdapter {
    public static final int MAX_LEN = 5;
    public static final int TYPE_POSTER = 1;
    public static final int TYPE_MORE = 2;
    private int mType=-1;

    public NewMovieItemWithMoreListAdapter(Context context, List<MovieDataView> movieDataViewList,int type) {
        super(context, movieDataViewList);
        mType=type;
    }

    @NonNull
    @Override
    public BaseScaleAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_MORE) {
            PosterMoreItemBinding itemBinding = PosterMoreItemBinding.inflate(LayoutInflater.from(mContext), parent, false);
            ViewHolder holder = new ViewHolder(itemBinding);
            return holder;
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleAdapter.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_POSTER)
            super.onBindViewHolder(holder, position);
        else
            holder.mBinding.getRoot().setTag(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < MAX_LEN)
            return TYPE_POSTER;
        return TYPE_MORE;
    }

    @Override
    public int getItemCount() {
        if (mList.size() > MAX_LEN)
            return MAX_LEN + 1;
        return super.getItemCount();
    }

    public void onMoreItemClick(View v) {
        if (mOnMoreItemClickListener != null)
            mOnMoreItemClickListener.onClick(mType);
    }

    public class ViewHolder extends BaseScaleAdapter.ViewHolder{
        public ViewHolder(ViewDataBinding binding) {
            super(binding);
            binding.getRoot().setOnClickListener(NewMovieItemWithMoreListAdapter.this::onMoreItemClick);
        }
    }

    public interface OnMoreItemClickListener{
        void onClick(int type);
    }

    private OnMoreItemClickListener mOnMoreItemClickListener;

    public void setOnMoreItemClickListener(OnMoreItemClickListener onMoreItemClickListener) {
        mOnMoreItemClickListener = onMoreItemClickListener;
    }
}


