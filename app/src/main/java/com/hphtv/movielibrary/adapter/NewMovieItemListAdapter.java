package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.PosterItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class NewMovieItemListAdapter extends BaseScaleApater<PosterItemBinding, BaseScaleApater.ViewHolder, MovieDataView> {

    public NewMovieItemListAdapter(Context context, List<MovieDataView> movieDataViewList) {
        super(context, movieDataViewList);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleApater.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        MovieDataView movieDataView = mList.get(position);
        PosterItemBinding binding = (PosterItemBinding) holder.mBinding;
        Glide.with(mContext).load(movieDataView.poster)
                .placeholder(R.mipmap.default_poster)
                .into(binding.rvPoster);
        binding.setTitle(movieDataView.title);
        binding.setRating(movieDataView.ratings);
    }

}


