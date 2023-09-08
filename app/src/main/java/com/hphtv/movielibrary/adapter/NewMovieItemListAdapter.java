package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.PosterItemBinding;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.util.GlideTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class NewMovieItemListAdapter extends BaseScaleAdapter<PosterItemBinding, MovieDataView> {
    public static final String TAG = NewMovieItemListAdapter.class.getSimpleName();

    public NewMovieItemListAdapter(Context context, List<MovieDataView> movieDataViewList) {
        super(context, movieDataViewList);
    }

    @NonNull
    @Override
    public BaseScaleAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        BaseScaleAdapter.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        PosterItemBinding itemBinding = (PosterItemBinding) viewHolder.mBinding;
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
            if (movieDataView.type.equals(Constants.VideoType.tv) && !TextUtils.isEmpty(movieDataView.season_poster)) {
                GlideTools.GlideWrapper(mContext, movieDataView.season_poster)
                        .into(binding.rvPoster);
            } else {
                GlideTools.GlideWrapper(mContext, movieDataView.poster)
                        .into(binding.rvPoster);
            }
        else
            Glide.with(mContext).load(R.drawable.default_poster).into(binding.rvPoster);
        String title = movieDataView.title;
        if (movieDataView.type.equals(Constants.VideoType.tv)) {
            if (!TextUtils.isEmpty(movieDataView.season_name))
                title += " " + movieDataView.season_name;
            else if (movieDataView.season != -1)
                title += " " + mContext.getResources().getString(R.string.season_name_for_unknow, movieDataView.season);
            if (movieDataView.episode_count != 0) {
                binding.setTag(mContext.getString(R.string.total_episodes, movieDataView.episode_count));
            } else {
                binding.setTag(null);
            }
        } else {
            if (!TextUtils.isEmpty(movieDataView.video_source)) {
                binding.setTag(movieDataView.video_source);
            } else if (!TextUtils.isEmpty(movieDataView.resolution)) {
                binding.setTag(movieDataView.resolution);
            } else {
                binding.setTag(null);
            }
        }

        binding.setType(movieDataView.type);
        binding.setTitle(title);
        binding.setRating(movieDataView.ratings);
        binding.setLike(movieDataView.is_favorite);
        binding.setCloudUserFav(movieDataView.is_user_fav);
    }


    public void remove(String movie_id, String type, int pos) {
        if (mList.get(pos).movie_id.equals(movie_id)
                && type.equals(mList.get(pos).type.name())) {
            mList.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, mList.size());

        }
    }


    public void remove(String movie_id, String type) {
        for (int i = 0; i < mList.size(); i++) {
            MovieDataView dataView = mList.get(i);
            if (dataView.movie_id.equals(movie_id)
                    && dataView.type.name().equals(type)) {
                mList.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, mList.size());
                break;
            }
        }
    }

    public void remove(MovieDataView movieDataView) {
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).equals(movieDataView)) {
                mList.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, mList.size());

                break;
            }
        }
    }

    /**
     * 适用于更改影片的收藏状态，可视状态之类
     *
     * @param movieDataView
     */
    public void updateStatus(MovieDataView movieDataView) {
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).equals(movieDataView)) {
                mList.set(i, movieDataView);
                notifyItemChanged(i);
                break;
            }
        }
    }


}


