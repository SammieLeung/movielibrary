package com.hphtv.movielibrary.ui.common;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.databinding.ObservableInt;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.CommonViewHolder;
import com.hphtv.movielibrary.databinding.UnionsearchMovieFooterBinding;
import com.hphtv.movielibrary.databinding.UnionsearchMovieItemBinding;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.util.GlideTools;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/8/12
 */
public class MovieSearchAdapter extends RecyclerView.Adapter<CommonViewHolder> implements View.OnFocusChangeListener {
    private final static int TYPE_CONTENT = 0;//正常内容
    private final static int TYPE_FOOTER = 1;//下拉刷新


    private Context mContext;
    private List<Movie> mMovieList;
    private UnionsearchMovieFooterBinding mFooterBinding;
    private ObservableInt mSeletPos;

    public MovieSearchAdapter(Context context,ObservableInt seletPos) {
        mContext = context;
        mSeletPos=seletPos;
        mMovieList = new ArrayList<>();
    }

    @NonNull
    @NotNull
    @Override
    public CommonViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            UnionsearchMovieFooterBinding footerBinding = UnionsearchMovieFooterBinding.inflate(LayoutInflater.from(mContext), parent, false);
            FootViewHolder vh = new FootViewHolder(footerBinding);
            return vh;
        } else {
            UnionsearchMovieItemBinding binding = UnionsearchMovieItemBinding.inflate(LayoutInflater.from(mContext), parent, false);
            ViewHolder vh = new ViewHolder(binding);
            vh.mDataBinding.setSelectPos(mSeletPos);
            return vh;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CommonViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_FOOTER) {
            FootViewHolder vh = (FootViewHolder) holder;
        } else {
            ViewHolder vh = (ViewHolder) holder;
            Movie movie = mMovieList.get(position);
            vh.mDataBinding.setMovie(movie);
            vh.mDataBinding.setPos(position);
            GlideTools.GlideWrapper(mContext, movie.poster)
                    .into(vh.mDataBinding.ivCover);
        }
    }


    @Override
    public int getItemCount() {
        return mMovieList.size() + 1;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == mMovieList.size()) {
            return TYPE_FOOTER;//最后的项为footer
        }
        return TYPE_CONTENT;
    }


    //用于GridLayoutManager header footer只显示一行


    @Override
    public void onAttachedToRecyclerView(@NonNull @NotNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager layout = ((GridLayoutManager) manager);
            GridLayoutManager.SpanSizeLookup mGridSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (getItemViewType(position) == TYPE_FOOTER) {
                        //footer占用一整行
                        return layout.getSpanCount();
                    } else {
                        //The number of spans occupied by the item at the provided position，Default Each item occupies 1 span.
                        //在某个位置的item所占用的跨度的数量，默认情况下占用一个跨度。
                        return 1;
                    }
                }

            };
            layout.setSpanSizeLookup(mGridSpanSizeLookup);
        }
    }

    public void clearAll() {
        this.mMovieList.clear();
        notifyDataSetChanged();
    }

    public void setMovies(List<Movie> movies) {
        mMovieList.addAll(movies);
        notifyDataSetChanged();
    }

    public void addMovies(List<Movie> movies) {
        if (movies != null && movies.size() > 0) {
            int startIndex = mMovieList.size();
            mMovieList.addAll(movies);
            notifyItemRangeInserted(startIndex, movies.size());
        }
    }

    public void loading() {
        if (mFooterBinding != null) {
            mFooterBinding.viewLoading.setVisibility(View.VISIBLE);
            mFooterBinding.tvTips.setVisibility(View.GONE);
        }
    }

    public void cancelLoading() {
        if (mFooterBinding != null) {
            mFooterBinding.viewLoading.setVisibility(View.GONE);
            mFooterBinding.tvTips.setVisibility(View.GONE);
        }
    }

    public void cancelLoadingAndShowTips(String msg){
        if (mFooterBinding != null) {
            mFooterBinding.viewLoading.setVisibility(View.INVISIBLE);
            mFooterBinding.tvTips.setText(msg);
            mFooterBinding.tvTips.setVisibility(View.VISIBLE);
        }
    }


    private OnClickListener mOnClickListener;

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(v.getTag()!=null){
            if(v.getTag() instanceof Integer){
                if(hasFocus)
                    mSeletPos.set((Integer) v.getTag());
            }
        }
    }

    public interface OnClickListener {
        void onClick(Movie movie);
    }

    public void setOnItemClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public class ViewHolder extends CommonViewHolder<UnionsearchMovieItemBinding> {

        public ViewHolder(UnionsearchMovieItemBinding binding) {
            super(binding);
            mDataBinding.getRoot().setOnClickListener(v -> {
                if (mOnClickListener != null)
                    mOnClickListener.onClick(mDataBinding.getMovie());
            });
            mDataBinding.getRoot().setOnFocusChangeListener(MovieSearchAdapter.this);
        }
    }

    private class FootViewHolder extends CommonViewHolder<UnionsearchMovieFooterBinding> {
        public FootViewHolder(UnionsearchMovieFooterBinding binding) {
            super(binding);
            mFooterBinding = binding;
        }
    }

}
