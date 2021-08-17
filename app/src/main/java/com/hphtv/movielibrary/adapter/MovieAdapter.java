package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.signature.ObjectKey;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.databinding.MovieLibraryItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.util.GlideTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MovieAdapter extends BaseAdapter<MovieLibraryItemBinding, BaseAdapter.ViewHolder, MovieDataView> {


    public MovieAdapter(Context context, List<MovieDataView> list) {
        super(context, list);
    }

    @Override
    protected int getBaseItemLayoutId() {
        return R.layout.movie_library_item;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseAdapter.ViewHolder holder, int position) {
        MovieDataView dataView = mList.get(position);
        String photo;
        if (dataView != null) {
            //将数据保存在itemView的Tag中，以便点击时进行获取
            holder.itemView.setTag(dataView);
            String title = dataView.title;
            photo = dataView.poster;
            MovieLibraryItemBinding binding = (MovieLibraryItemBinding) holder.mBinding;

            GlideTools.GlideWrapper(mContext, photo)
                    .into(binding.ivImg);
            binding.tvRating.setText(dataView.ratings);
            binding.rvRating.setVisibility(View.VISIBLE);
            binding.tvTitle.setText(title);
        }
    }
}