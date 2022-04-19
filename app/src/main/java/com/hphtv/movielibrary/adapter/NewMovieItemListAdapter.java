package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.databinding.PosterItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.util.GlideTools;

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

    @NonNull
    @Override
    public BaseScaleApater.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        BaseScaleApater.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        PosterItemBinding itemBinding= (PosterItemBinding) viewHolder.mBinding;
        itemBinding.setShowConrerMark(Config.getShowCornerMark());
        itemBinding.setShowLike(Config.getShowLike());
        itemBinding.setShowRating(Config.getShowRating());
        itemBinding.setShowTitle(Config.getShowTitle());
        itemBinding.getRoot().setId(View.generateViewId());

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleApater.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        MovieDataView movieDataView = mList.get(position);
        PosterItemBinding binding = (PosterItemBinding) holder.mBinding;
        if (Config.getShowPoster().get())
            GlideTools.GlideWrapper(mContext, movieDataView.poster)
                    .into(binding.rvPoster);
        else
            Glide.with(mContext).load(R.mipmap.default_poster).into(binding.rvPoster);
        binding.setTitle(movieDataView.title);
        binding.setRating(movieDataView.ratings);
        binding.setLike(movieDataView.is_favorite);
    }

    public void remove(String movie_id,int pos){
        if(mList.get(pos).movie_id.equals(movie_id)){
            super.remove(mList.get(pos),pos);
        }
    }


}


