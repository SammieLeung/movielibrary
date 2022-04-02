package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.databinding.PosterItemBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.util.GlideTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class UnknowFileItemListAdapter extends BaseScaleApater<PosterItemBinding, BaseScaleApater.ViewHolder, UnrecognizedFileDataView> {

    public UnknowFileItemListAdapter(Context context, List<UnrecognizedFileDataView> list) {
        super(context, list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        PosterItemBinding itemBinding= (PosterItemBinding) viewHolder.mBinding;
        itemBinding.setShowConrerMark(Config.getShowCornerMark());
        itemBinding.setShowLike(Config.getShowLike());
        itemBinding.setShowRating(Config.getShowRating());
        itemBinding.setShowTitle(Config.getShowTitle());
        itemBinding.getRoot().setId(View.generateViewId());

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleApater.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        UnrecognizedFileDataView dataView = mList.get(position);
        PosterItemBinding binding = (PosterItemBinding) holder.mBinding;
            Glide.with(mContext).load(R.mipmap.default_poster).into(binding.rvPoster);
        binding.setTitle(dataView.filename);
    }
//
//    public void remove(String path,int pos){
//        if(mList.get(pos).path.equals(path)){
//            super.remove(mList.get(pos),pos);
//        }
//    }


}


