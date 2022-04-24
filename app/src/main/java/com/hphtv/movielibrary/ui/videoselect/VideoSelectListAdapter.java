package com.hphtv.movielibrary.ui.videoselect;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

import com.hphtv.movielibrary.adapter.BaseApater2;
import com.hphtv.movielibrary.databinding.RvItemVideosourceBinding;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.util.StringTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/1/21
 */
public class VideoSelectListAdapter extends BaseApater2<RvItemVideosourceBinding, VideoSelectListAdapter.ViewHolder, VideoFile> implements View.OnFocusChangeListener, View.OnHoverListener {

    public VideoSelectListAdapter(Context context, List<VideoFile> list) {
        super(context, list);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        VideoFile videoFile = mList.get(position);
        RvItemVideosourceBinding binding = (RvItemVideosourceBinding) holder.mBinding;
        binding.setPath(StringTools.hideSmbAuthInfo(videoFile.path));
        binding.setTitle(videoFile.filename);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus)
            v.setSelected(true);
        else
            v.setSelected(false);
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
            v.setSelected(true);
        } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
            v.setSelected(false);
        }
        return false;
    }

    public class ViewHolder extends BaseApater2.ViewHolder {

        public ViewHolder(ViewDataBinding binding) {
            super(binding);
            binding.getRoot().setOnFocusChangeListener(VideoSelectListAdapter.this::onFocusChange);
            binding.getRoot().setOnHoverListener(VideoSelectListAdapter.this::onHover);

        }
    }
}
