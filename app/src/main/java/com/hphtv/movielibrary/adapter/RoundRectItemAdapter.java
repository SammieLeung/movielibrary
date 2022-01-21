package com.hphtv.movielibrary.adapter;

import android.content.Context;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.RoundRectItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/11/9
 */
public class RoundRectItemAdapter extends BaseScaleApater<RoundRectItemBinding, BaseScaleApater.ViewHolder, String> {


    public RoundRectItemAdapter(Context context, List<String> list) {
        super(context, list);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleApater.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        RoundRectItemBinding binding = (RoundRectItemBinding) holder.mBinding;
        binding.setTitle(mList.get(position));
    }


}
