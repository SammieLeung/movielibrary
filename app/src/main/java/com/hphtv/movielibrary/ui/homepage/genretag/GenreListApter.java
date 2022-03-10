package com.hphtv.movielibrary.ui.homepage.genretag;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableInt;

import com.hphtv.movielibrary.adapter.BaseApater2;
import com.hphtv.movielibrary.databinding.RvItemGenreTagCheckableBinding;
import com.hphtv.movielibrary.databinding.RvItemGenreTagSortBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/11/13
 */
public class GenreListApter extends BaseApater2<RvItemGenreTagCheckableBinding, BaseApater2.ViewHolder, GenreTagItem>  {
    public static final int TYPE_EDIT = 0;
    public static final int TYPE_SORT = 1;
    private int mType;
    private ObservableInt mSortPos;

    public GenreListApter(Context context, List<GenreTagItem> list, int type) {
        super(context, list);
        mType = type;
    }

    public void setSortPos(ObservableInt checkPos) {
        mSortPos = checkPos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (mType == TYPE_EDIT) {
            return super.onCreateViewHolder(parent, viewType);
        } else {
            RvItemGenreTagSortBinding binding = RvItemGenreTagSortBinding.inflate(LayoutInflater.from(mContext), parent, false);
            ViewHolder viewHolder = new ViewHolder(binding);
            if (mSortPos != null) {
                binding.setSortPos(mSortPos);
            }
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseApater2.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (mType == TYPE_EDIT) {
            RvItemGenreTagCheckableBinding binding = (RvItemGenreTagCheckableBinding) holder.mBinding;
            binding.setGenreItemTag(mList.get(position));
        } else {
            RvItemGenreTagSortBinding binding = (RvItemGenreTagSortBinding) holder.mBinding;
            binding.setPos(position);
            binding.setName((position + 1) + "." + mList.get(position).getName());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mType;
    }


}
