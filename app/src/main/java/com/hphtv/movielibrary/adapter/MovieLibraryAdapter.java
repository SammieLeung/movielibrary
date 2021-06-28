package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.FLayoutFavoriteBinding;
import com.hphtv.movielibrary.databinding.MovieLibraryItemBinding;
import com.hphtv.movielibrary.roomdb.entity.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.MovieWrapper;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MovieLibraryAdapter extends BaseAdapter<MovieLibraryItemBinding, MovieLibraryAdapter.ViewHolder> {


    public MovieLibraryAdapter(Context context, List list) {
        super(context, ViewHolder.class, list);
    }

    @Override
    protected int getBaseItemLayoutId() {
        return R.layout.movie_library_item;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MovieLibraryAdapter.ViewHolder holder, int position) {
        MovieDataView dataView = (MovieDataView) mList.get(position);
        String photo;
        if (dataView != null) {
            //将数据保存在itemView的Tag中，以便点击时进行获取
            holder.itemView.setTag(dataView);
            String title = dataView.title;
            photo = dataView.poster;
            MovieLibraryItemBinding binding = (MovieLibraryItemBinding) holder.mBinding;

            Glide.with(mContext).load(photo).apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default)).into(binding.ivImg);
            binding.tvRating.setText(dataView.ratings);
            binding.rvRating.setVisibility(View.VISIBLE);
            binding.tvTitle.setText(title);
        }
    }


    public class ViewHolder extends BaseAdapter.ViewHolder {
        public ViewHolder(MovieLibraryItemBinding binding) {
            super(binding);
        }
    }

    /**
     * 添加数据
     *
     * @param dataView
     * @param position
     */
    public void addItem(MovieDataView dataView, int position) {
        mList.add(position, dataView);
        notifyItemInserted(position); // Attention!
    }


    public void removeAll() {
        mList.clear();
        notifyDataSetChanged();
    }


}