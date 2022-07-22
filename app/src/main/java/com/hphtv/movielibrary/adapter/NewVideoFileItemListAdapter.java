package com.hphtv.movielibrary.adapter;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.PosterItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class NewVideoFileItemListAdapter extends BaseScaleAdapter<PosterItemBinding, BaseScaleAdapter.ViewHolder, UnrecognizedFileDataView> {

    public NewVideoFileItemListAdapter(Context context, List<UnrecognizedFileDataView> movieDataViewList) {
        super(context, movieDataViewList);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        UnrecognizedFileDataView dataView = mList.get(position);
        PosterItemBinding binding = (PosterItemBinding) holder.mBinding;
        Glide.with(mContext).load(R.drawable.default_poster)
                .into(binding.rvPoster);
        binding.setTitle(dataView.keyword);
    }
}


