package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.ActorPosterItemBinding;
import com.hphtv.movielibrary.roomdb.entity.Actor;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/1/19
 */
public class ActorPosterItemListApdater extends BaseScaleApater<ActorPosterItemBinding, BaseScaleApater.ViewHolder, Actor> {

    public ActorPosterItemListApdater(Context context, List<Actor> list) {
        super(context, list);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseScaleApater.@NotNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Actor actor = mList.get(position);
        ActorPosterItemBinding binding = (ActorPosterItemBinding) holder.mBinding;
        binding.setName(actor.name);
        Glide.with(mContext).load(actor.img).into(binding.ivActorPoster);
    }
}
