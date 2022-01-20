package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.HistoryItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.util.ThumbnailUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class HistoryListAdapter extends BaseAdapter2<HistoryItemBinding, BaseAdapter2.ViewHolder,HistoryMovieDataView> {

    public HistoryListAdapter(Context context, List<HistoryMovieDataView> historyList) {
        super(context, historyList);
    }

    @Override
    protected int getBaseItemLayoutId() {
        return R.layout.history_item;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseAdapter2.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        HistoryMovieDataView dataView = mList.get(position);
        HistoryItemBinding binding = (HistoryItemBinding) holder.mBinding;
        Glide.with(mContext).load(!TextUtils.isEmpty(dataView.stage_photo)?dataView.stage_photo:dataView.poster).centerCrop()
                .into(binding.ivImg);
        binding.setTitle(!TextUtils.isEmpty(dataView.source)?dataView.title:!TextUtils.isEmpty(dataView.keyword)?dataView.keyword:dataView.filename);
    }
}


