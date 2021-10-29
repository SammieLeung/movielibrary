package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.HistoryItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.util.ThumbnailUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class HistoryListAdapter extends BaseAdapter<HistoryItemBinding, BaseAdapter.ViewHolder,UnrecognizedFileDataView> {

    public HistoryListAdapter(Context context, List<UnrecognizedFileDataView> unrecognizedFileDataViewList) {
        super(context, unrecognizedFileDataViewList);
    }

    @Override
    protected int getBaseItemLayoutId() {
        return R.layout.history_item;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseAdapter.ViewHolder holder, int position) {
        UnrecognizedFileDataView unrecognizedFileDataView = mList.get(position);
        HistoryItemBinding binding = (HistoryItemBinding) holder.mBinding;
        Bitmap bitmap= ThumbnailUtils.getVideoThumb(unrecognizedFileDataView.path,15);
        Glide.with(mContext).load(bitmap)
                .into(binding.ivImg);
        binding.tvTitle.setText(unrecognizedFileDataView.filename);
        binding.getRoot().setTag(unrecognizedFileDataView);
    }
}


