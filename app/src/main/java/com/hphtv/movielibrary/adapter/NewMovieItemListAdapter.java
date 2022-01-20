package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.HistoryItemBinding;
import com.hphtv.movielibrary.databinding.PosterItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.util.ThumbnailUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class NewMovieItemListAdapter extends BaseAdapter2<PosterItemBinding, BaseAdapter2.ViewHolder, MovieDataView> {

    public NewMovieItemListAdapter(Context context, List<MovieDataView> movieDataViewList) {
        super(context, movieDataViewList);
    }

    @Override
    protected int getBaseItemLayoutId() {
        return R.layout.poster_item;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseAdapter2.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        MovieDataView movieDataView = mList.get(position);
        PosterItemBinding binding = (PosterItemBinding) holder.mBinding;
        Glide.with(mContext).load(movieDataView.poster)
                .into(binding.rvPoster);
        binding.setTitle(movieDataView.title);
    }
}


