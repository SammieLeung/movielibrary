package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.HistoryItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class HistoryListAdapter extends BaseScaleApater<HistoryItemBinding, BaseScaleApater.ViewHolder,HistoryMovieDataView> {
    public HistoryListAdapter(Context context, List<HistoryMovieDataView> historyList) {
        super(context, historyList);
        setZoomRatio(1.1f);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleApater.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        HistoryMovieDataView dataView = mList.get(position);
        HistoryItemBinding binding = (HistoryItemBinding) holder.mBinding;
        Glide.with(mContext).load(!TextUtils.isEmpty(dataView.stage_photo)?dataView.stage_photo:dataView.poster).placeholder(R.mipmap.default_poster)
                .into(binding.ivImg);
        binding.setTitle(!TextUtils.isEmpty(dataView.source)?dataView.title:!TextUtils.isEmpty(dataView.keyword)?dataView.keyword:dataView.filename);
    }
}


