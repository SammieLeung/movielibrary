package com.hphtv.movielibrary.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.CircleItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/11/9
 */
public class CircleItemAdapter extends BaseAdapter2<CircleItemBinding, BaseAdapter2.ViewHolder, String> {


    public CircleItemAdapter(Context context, List<String> list) {
        super(context, list);
    }

    @Override
    protected int getBaseItemLayoutId() {
        return R.layout.circle_item;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseAdapter2.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        CircleItemBinding binding = (CircleItemBinding) holder.mBinding;
        binding.setTitle(mList.get(position));
    }
}
