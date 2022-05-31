
package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;


import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.RoundRectItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/11/9
 */
public class GenreTagAdapter extends RoundRectItemAdapter {
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_ADD = 1;
    public static final int TYPE_HOME = -1;

    public GenreTagAdapter(Context context, List<String> list) {
        super(context, list);
        setZoomRatio(1.15f);
    }


    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        RoundRectItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.round_rect_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(binding);
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_HOME;
        if (position == getItemCount() - 1)
            return TYPE_ADD;
        return TYPE_ITEM;
    }


    @Override
    public int getItemCount() {
        return super.getItemCount() + 2;
    }

    @Override
    public void onClick(View v) {
        int postion = (int) v.getTag();
        if (getItemViewType(postion) == TYPE_ITEM) {
            int realPos = postion - 1;
            if (mOnItemClickListener != null)
                mOnItemClickListener.onItemClick(v, realPos, mList.get(realPos));
        } else if (getItemViewType(postion) == TYPE_ADD) {
            if (mAddGenreListener != null) {
                mAddGenreListener.addGenre();
            }
        } else if (getItemViewType(postion) == TYPE_HOME) {
            if (mAddGenreListener != null) {
                mAddGenreListener.browseAll();
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            ViewCompat.animate((View) v).scaleX(mZoomRatio).scaleY(mZoomRatio).translationZ(1).setDuration(Constants.ANIMATION_DURATION).start();
        } else {
            ViewCompat.animate((View) v).scaleX(1f).scaleY(1f).translationZ(0).setDuration(Constants.ANIMATION_DURATION).start();
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
            ViewCompat.animate((View) v).scaleX(mZoomRatio).scaleY(mZoomRatio).translationZ(1).setDuration(Constants.ANIMATION_DURATION).start();
        } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
            ViewCompat.animate((View) v).scaleX(1f).scaleY(1f).translationZ(0).setDuration(Constants.ANIMATION_DURATION).start();
        }
        return false;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BaseScaleAdapter.ViewHolder holder, int position) {
        holder.mBinding.getRoot().setTag(position);
        RoundRectItemBinding binding = (RoundRectItemBinding) holder.mBinding;
        if (getItemViewType(position) == TYPE_HOME) {
            binding.setTitle(mContext.getString(R.string.genre_all));
        } else if (getItemViewType(position) == TYPE_ADD) {
            binding.setTitle(mContext.getString(R.string.genre_add));
        } else {
            binding.setTitle(mList.get(position - 1));
        }
    }

    private GenreListener mAddGenreListener;

    public interface GenreListener {
        void addGenre();
        void browseAll();
    }

    public void setOnGenreListener(GenreListener addGenreListener) {
        mAddGenreListener = addGenreListener;
    }

}
