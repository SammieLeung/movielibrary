package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.databinding.HistoryItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.util.GlideTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class HistoryListAdapter extends BaseScaleAdapter<HistoryItemBinding, BaseScaleAdapter.ViewHolder,HistoryMovieDataView> {
    public HistoryListAdapter(Context context, List<HistoryMovieDataView> historyList) {
        super(context, historyList);
        setZoomRatio(1.1f);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        HistoryMovieDataView dataView = mList.get(position);
        HistoryItemBinding binding = (HistoryItemBinding) holder.mBinding;
        binding.setRatings(dataView.ratings);
        GlideTools.GlideWrapper(mContext,!TextUtils.isEmpty(dataView.stage_photo)?dataView.stage_photo:dataView.poster)
                .into(binding.ivImg);
        binding.setTitle(!TextUtils.isEmpty(dataView.source)?dataView.title:!TextUtils.isEmpty(dataView.keyword)?dataView.keyword:dataView.filename);
    }
}


