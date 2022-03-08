package com.hphtv.movielibrary.ui.homepage.genretag;


import android.content.Context;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.adapter.BaseApater2;
import com.hphtv.movielibrary.databinding.RvItemGenreTagCheckableBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/11/13
 */
public class GenreListApter extends BaseApater2<RvItemGenreTagCheckableBinding, GenreListApter.ViewHolder,GenreTagItem> {


    public GenreListApter(Context context, List<GenreTagItem> list) {
        super(context, list);
    }

    public class ViewHolder extends BaseApater2.ViewHolder{
        RvItemGenreTagCheckableBinding mBinding;
        public ViewHolder(RvItemGenreTagCheckableBinding binding) {
            super(binding);
            mBinding=binding;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        RvItemGenreTagCheckableBinding binding=holder.mBinding;
        binding.setTag(mList.get(position));
    }
}
