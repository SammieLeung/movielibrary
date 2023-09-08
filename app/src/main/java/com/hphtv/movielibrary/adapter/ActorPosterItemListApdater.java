package com.hphtv.movielibrary.adapter;

import android.content.Context;

import androidx.annotation.NonNull;

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
public class ActorPosterItemListApdater extends BaseScaleAdapter<ActorPosterItemBinding, Actor> {

    public ActorPosterItemListApdater(Context context, List<Actor> list) {
        super(context, list);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseScaleAdapter.@NotNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Actor actor = mList.get(position);
        ActorPosterItemBinding binding = (ActorPosterItemBinding) holder.mBinding;
        binding.setName(actor.name);
        Glide.with(mContext).load(actor.img).placeholder(R.mipmap.actor_placeholder).error(R.mipmap.actor_placeholder).into(binding.ivActorPoster);
    }
}
