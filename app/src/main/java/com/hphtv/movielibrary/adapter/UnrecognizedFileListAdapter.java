package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.MovieLibraryItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class UnrecognizedFileListAdapter extends BaseAdapter<MovieLibraryItemBinding, BaseAdapter.ViewHolder,UnrecognizedFileDataView> {

    public UnrecognizedFileListAdapter(Context context, List<UnrecognizedFileDataView> unrecognizedFileDataViewList) {
        super(context, unrecognizedFileDataViewList);
    }

    @Override
    protected int getBaseItemLayoutId() {
        return R.layout.movie_library_item;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseAdapter.ViewHolder holder, int position) {
        UnrecognizedFileDataView unrecognizedFileDataView = mList.get(position);
        MovieLibraryItemBinding binding = (MovieLibraryItemBinding) holder.mBinding;
        Glide.with(mContext).load(R.mipmap.ic_poster_default)
                .into(binding.ivImg);
        binding.tvTitle.setText(unrecognizedFileDataView.keyword);
        binding.rvRating.setVisibility(View.GONE);
        binding.getRoot().setTag(unrecognizedFileDataView);
    }
}


