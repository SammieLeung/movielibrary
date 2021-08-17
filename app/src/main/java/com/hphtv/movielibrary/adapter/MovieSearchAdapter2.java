package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.databinding.UnionsearchMovieItemBinding;
import com.hphtv.movielibrary.roomdb.entity.Movie;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/8/12
 */
public class MovieSearchAdapter2 extends RecyclerView.Adapter<MovieSearchAdapter2.ViewHolder> {


    private Context mContext;
    private UnionsearchMovieItemBinding mBinding;
    private List<Movie> mMovieList;

    public MovieSearchAdapter2(Context context) {
        mContext = context;
        mMovieList=new ArrayList<>();
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        mBinding = UnionsearchMovieItemBinding.inflate(LayoutInflater.from(mContext), parent, false);
        ViewHolder vh=new ViewHolder(mBinding);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MovieSearchAdapter2.ViewHolder holder, int position) {
            Movie movie=mMovieList.get(position);
            holder.mDataBinding.setMovie(movie);
    }


    @Override
    public int getItemCount() {
        return mMovieList.size();
    }


    public void setMovies(List<Movie> movies){
        this.mMovieList.clear();
        mMovieList.addAll(movies);
        notifyDataSetChanged();
    }

    public class ViewHolder extends CommonViewHolder<UnionsearchMovieItemBinding> {

        public ViewHolder(UnionsearchMovieItemBinding binding) {
            super(binding);
        }
    }
}
