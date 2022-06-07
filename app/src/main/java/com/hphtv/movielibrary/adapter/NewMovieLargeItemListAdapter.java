package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.PosterItemLargeBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.util.GlideTools;
import com.station.kit.util.LogUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/02/10
 */
public class NewMovieLargeItemListAdapter extends BaseScaleAdapter<PosterItemLargeBinding, BaseScaleAdapter.ViewHolder, MovieDataView> implements Filterable {
    private List<MovieDataView> mFilterMovieDataViewList;

    public NewMovieLargeItemListAdapter(Context context, List<MovieDataView> movieDataViewList) {
        super(context, movieDataViewList);
        mFilterMovieDataViewList = movieDataViewList;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        MovieDataView movieDataView = mFilterMovieDataViewList.get(position);
        PosterItemLargeBinding binding = (PosterItemLargeBinding) holder.mBinding;
        if (movieDataView.type.equals(Constants.SearchType.tv) && !TextUtils.isEmpty(movieDataView.season_poster)) {
            GlideTools.GlideWrapper(mContext, movieDataView.season_poster)
                    .into(binding.rvPoster);
        } else {
            GlideTools.GlideWrapper(mContext, movieDataView.poster)
                    .into(binding.rvPoster);
        }
        String title = movieDataView.title;
        if (movieDataView.type.equals(Constants.SearchType.tv)) {
            if (!TextUtils.isEmpty(movieDataView.season_name))
                title += " " + movieDataView.season_name;
            else if (movieDataView.season != -1)
                title += " " + mContext.getResources().getString(R.string.season_name_for_unknow, movieDataView.season);
            if (movieDataView.episode_count != 0) {
                binding.setTag(mContext.getString(R.string.total_episodes, movieDataView.episode_count));
            }
        } else {
            if (!TextUtils.isEmpty(movieDataView.video_source)) {
                binding.setTag(movieDataView.video_source);
            } else if (!TextUtils.isEmpty(movieDataView.resolution)) {
                binding.setTag(movieDataView.resolution);
            }
        }
        binding.setType(movieDataView.type);
        binding.setTitle(title);
        binding.setRating(movieDataView.ratings);
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            int postion = (int) v.getTag();
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, postion, mFilterMovieDataViewList.get(postion));
        }
    }

    @Override
    public int getItemCount() {
        LogUtil.v("getItemCount ");
        return mFilterMovieDataViewList.size();
    }

    public int getRealCount() {
        return mList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<MovieDataView> filterDatas = new ArrayList<>();
                if (constraint == null) {
                    filterDatas = mList;
                } else {
                    Constants.SearchType searchType = Constants.SearchType.valueOf(constraint.toString());
                    for (int i = 0; i < mList.size(); i++) {
                        if (searchType == mList.get(i).type) {
                            filterDatas.add(mList.get(i));
                        }
                    }
                }
                LogUtil.v("filter " + filterDatas.size());
                FilterResults f = new FilterResults();
                f.values = filterDatas;
                return f;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mFilterMovieDataViewList = (List<MovieDataView>) results.values;
                LogUtil.v("publishResults ");

                notifyDataSetChanged();
            }
        };
    }
}


