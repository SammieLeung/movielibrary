package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.HistoryItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.util.GlideTools;
import com.station.kit.util.TimeFormatter;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class HistoryListAdapter extends BaseScaleAdapter<HistoryItemBinding, HistoryMovieDataView> {
    public HistoryListAdapter(Context context, List<HistoryMovieDataView> historyList) {
        super(context, historyList);
        setZoomRatio(1.1f);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        BaseScaleAdapter.ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        HistoryItemBinding binding = (HistoryItemBinding) viewHolder.mBinding;
        binding.setShowConrerMark(Config.getShowCornerMark());
        binding.setShowRating(Config.getShowRating());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        HistoryMovieDataView dataView = mList.get(position);
        HistoryItemBinding binding = (HistoryItemBinding) holder.mBinding;
        if (dataView.episode != -1) {
            binding.setTag(mContext.getString(R.string.btn_play_episode, dataView.episode));
        } else if (!TextUtils.isEmpty(dataView.aired) && !"-1".equals(dataView.aired)) {
            binding.setTag(mContext.getString(R.string.btn_play_episode_2, dataView.aired));
        } else {
            binding.setTag(null);
        }

        String title = !TextUtils.isEmpty(dataView.source) ? dataView.title : !TextUtils.isEmpty(dataView.keyword) ? dataView.keyword : dataView.filename;

        if (!TextUtils.isEmpty(dataView.season_name))
            title += " " + dataView.season_name;
        else if (dataView.season != -1)
            title += " " + mContext.getResources().getString(R.string.season_name_for_unknow, dataView.season);
        GlideTools.GlideWrapper(mContext, !TextUtils.isEmpty(dataView.stage_photo) ? dataView.stage_photo : dataView.poster, R.drawable.default_poster_land)
                .into(binding.ivImg);
        binding.setTitle(title);

        if (dataView.duration != 0) {
            binding.setMax(100);
            long duration = dataView.duration;
            long lastPosition = dataView.last_position;
            int progress = (int) Math.floorDiv(lastPosition * 100, duration);
            binding.setProgress(progress);
            String durationHRS = TimeFormatter.formatMillisecondToHumanReadableString(duration);
            String lastPositionHRS = TimeFormatter.formatMillisecondToHumanReadableString(lastPosition);
            binding.setProgressFormatText(lastPositionHRS + "/" + durationHRS);
        } else {
            binding.setMax(0);
        }
    }
}


