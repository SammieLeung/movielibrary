package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableInt;

import com.hphtv.movielibrary.databinding.RvItemEpisodeLayoutBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/5/27
 */
public class EpisodeItemListAdapter extends BaseScaleApater<RvItemEpisodeLayoutBinding, BaseScaleApater.ViewHolder, String> {

    public EpisodeItemListAdapter(Context context, List<String> list) {
        super(context, list);
    }


    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleApater.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        String title = mList.get(position);
        RvItemEpisodeLayoutBinding binding = ((RvItemEpisodeLayoutBinding) holder.mBinding);
        binding.setText(title);
        binding.setItemPos(position);
        binding.setSelectPos(new ObservableInt(0));
    }
}
