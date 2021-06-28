package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.FLayoutFavoriteBinding;
import com.hphtv.movielibrary.databinding.MovieLibraryItemBinding;
import com.hphtv.movielibrary.roomdb.entity.UnrecognizedFileDataView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/26
 */
public class UnrecognizedFileListAdapter extends BaseAdapter<MovieLibraryItemBinding, UnrecognizedFileListAdapter.ViewHolder>{

    public UnrecognizedFileListAdapter(Context context, List<UnrecognizedFileDataView> unrecognizedFileDataViewList) {
        super(context,ViewHolder.class,unrecognizedFileDataViewList);
    }

    @Override
    protected int getBaseItemLayoutId() {
        return R.layout.movie_library_item;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull UnrecognizedFileListAdapter.ViewHolder holder, int position) {
        UnrecognizedFileDataView unrecognizedFileDataView = (UnrecognizedFileDataView) mList.get(position);
        MovieLibraryItemBinding binding= (MovieLibraryItemBinding) holder.mBinding;
        Glide.with(mContext).load("").apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default)).into(binding.ivImg);
        binding.tvTitle.setText(unrecognizedFileDataView.keyword);
        binding.rvRating.setVisibility(View.GONE);
        binding.getRoot().setTag(unrecognizedFileDataView);
    }


    public class ViewHolder extends BaseAdapter.ViewHolder {

        public ViewHolder(MovieLibraryItemBinding binding) {
            super(binding);

        }
    }
}
