package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.PosterItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.util.GlideTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class NewMovieItemListAdapter extends BaseScaleAdapter<PosterItemBinding, BaseScaleAdapter.ViewHolder, MovieDataView> {

    public NewMovieItemListAdapter(Context context, List<MovieDataView> movieDataViewList) {
        super(context, movieDataViewList);
    }

    @NonNull
    @Override
    public BaseScaleAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        BaseScaleAdapter.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        PosterItemBinding itemBinding= (PosterItemBinding) viewHolder.mBinding;
        itemBinding.setShowConrerMark(Config.getShowCornerMark());
        itemBinding.setShowLike(Config.getShowLike());
        itemBinding.setShowRating(Config.getShowRating());
        itemBinding.setShowTitle(Config.getShowTitle());
        itemBinding.getRoot().setId(View.generateViewId());

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        MovieDataView movieDataView = mList.get(position);
        PosterItemBinding binding = (PosterItemBinding) holder.mBinding;
        if (Config.getShowPoster().get())
            if(movieDataView.type.equals(Constants.SearchType.tv)&&!TextUtils.isEmpty(movieDataView.season_poster)){
                GlideTools.GlideWrapper(mContext, movieDataView.season_poster)
                        .into(binding.rvPoster);
            }else {
                GlideTools.GlideWrapper(mContext, movieDataView.poster)
                        .into(binding.rvPoster);
            }
        else
            Glide.with(mContext).load(R.mipmap.default_poster).into(binding.rvPoster);
        String title=movieDataView.title;
        if(movieDataView.type.equals(Constants.SearchType.tv)){
            if(!TextUtils.isEmpty(movieDataView.season_name))
                title+=" "+ movieDataView.season_name;
            else if(movieDataView.season!=-1)
                title+=" "+ mContext.getResources().getString(R.string.season_name_for_unknow,movieDataView.season);
            if(movieDataView.episode_count!=0){
                binding.setTag(mContext.getString(R.string.total_episodes,movieDataView.episode_count));
            }
        }else{
            if(!TextUtils.isEmpty(movieDataView.video_source)){
                binding.setTag(movieDataView.video_source);
            }else if(!TextUtils.isEmpty(movieDataView.resolution)){
                binding.setTag(movieDataView.resolution);
            }
        }
        binding.setType(movieDataView.type);
        binding.setTitle(title);
        binding.setRating(movieDataView.ratings);
        binding.setLike(movieDataView.is_favorite);
    }

    public void remove(String movie_id,int pos){
        if(mList.get(pos).movie_id.equals(movie_id)){
            super.remove(mList.get(pos),pos);
        }
    }


}


