package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.PosterItemBinding;
import com.hphtv.movielibrary.databinding.PosterItemLargeBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.station.kit.util.LogUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/02/10
 */
public class NewMovieLargeItemListAdapter extends BaseScaleApater<PosterItemLargeBinding, BaseScaleApater.ViewHolder, MovieDataView> implements Filterable {
    private List<MovieDataView> mFilterMovieDataViewList;

    public NewMovieLargeItemListAdapter(Context context, List<MovieDataView> movieDataViewList) {
        super(context, movieDataViewList);
        mFilterMovieDataViewList = movieDataViewList;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleApater.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        MovieDataView movieDataView = mFilterMovieDataViewList.get(position);
        PosterItemLargeBinding binding = (PosterItemLargeBinding) holder.mBinding;
        Glide.with(mContext).load(movieDataView.poster)
                .placeholder(R.mipmap.default_poster)
                .error(R.mipmap.default_poster)
                .into(binding.rvPoster);
        binding.setTitle(movieDataView.title);
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
                LogUtil.v("filter "+filterDatas.size());
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


