package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.UnionsearchMovieItemBinding;
import com.hphtv.movielibrary.roomdb.entity.Movie;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2021/7/7
 */
public class UnionSearchMovieAdapter extends PagingDataAdapter<Movie, UnionSearchMovieAdapter.MovieViewHolder> {

    private Context mContext;

    public UnionSearchMovieAdapter(Context context) {
        this(new DiffUtil.ItemCallback<Movie>() {
            @Override
            public boolean areItemsTheSame(@NonNull @NotNull Movie oldItem, @NonNull @NotNull Movie newItem) {
                if(oldItem==null||newItem==null)
                    return false;
                return oldItem.id==newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull @NotNull Movie oldItem, @NonNull @NotNull Movie newItem) {
                return oldItem.id==newItem.id;
            }
        });
        mContext = context;
    }

    public UnionSearchMovieAdapter(DiffUtil.@NotNull ItemCallback<Movie> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @NotNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        UnionsearchMovieItemBinding binding = UnionsearchMovieItemBinding.inflate(LayoutInflater.from(parent.getContext()));
        return new MovieViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull UnionSearchMovieAdapter.MovieViewHolder holder, int position) {
        Movie item = getItem(position);
        holder.bind(item);
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        public UnionsearchMovieItemBinding mBinding;

        public MovieViewHolder(UnionsearchMovieItemBinding binding) {

            super(binding.getRoot());
            mBinding = binding;
        }

        public void bind(Movie movie) {
            Glide.with(mContext).load(movie.poster).error(R.mipmap.ic_poster_default).into(mBinding.ivCover);
            mBinding.setMovie(movie);
        }
    }
}
