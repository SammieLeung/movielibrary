package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.os.Build;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.roomdb.entity.MovieWrapper;

import java.util.List;

public class MovieLibraryAdapter extends
        RecyclerView.Adapter<MovieLibraryAdapter.ViewHolder> implements OnClickListener {
    private static final String TAG = "MovieLibraryAdapter";
    private List<MovieWrapper> list = null;
    private Context context;

    public MovieLibraryAdapter(Context context, List<MovieWrapper> wrappers) {
        this.list = wrappers;
        this.context = context;
    }

    // 创建新View，被LayoutManager所调用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view;
        view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.movie_library_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        //将创建的View注册点击事件
        view.setOnClickListener(this);
        vh.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //获取焦点时变化
                if (hasFocus) {
                    if (Build.VERSION.SDK_INT >= 21) {
                        ViewCompat.animate((View) v).scaleX(1.08f).scaleY(1.08f).translationZ(1).start();
                    }
                } else {
                    ViewCompat.animate((View) v).scaleX(1f).scaleY(1f).translationZ(0).start();
                }
            }
        });
        return vh;
    }

    // 将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        MovieWrapper wrapper = list.get(position);
        String photo;
        if (wrapper != null) {
            //将数据保存在itemView的Tag中，以便点击时进行获取
            viewHolder.itemView.setTag(wrapper);
            String title = wrapper.movie.title;
            photo = wrapper.movie.poster;

            Glide.with(context).load(photo).apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default)).into(viewHolder.mImage);
            float average=0;
            try {
                average = Float.parseFloat(wrapper.movie.ratings);
            } catch (NumberFormatException e) {
                average=0;
            }
            if (average <= 0) {
                viewHolder.mRatingLayout.setVisibility(View.GONE);
            } else {
                viewHolder.mRating.setText(String.valueOf(average));
                viewHolder.mRatingLayout.setVisibility(View.VISIBLE);
            }
            viewHolder.mTitle.setText(title);
        }

    }

    // 获取数据的数量
    @Override
    public int getItemCount() {
        return list.size();
    }

    // 自定义的ViewHolder，持有每个Item的的所有界面元素
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTitle;
        ImageView mImage;
        RelativeLayout mRatingLayout;
        TextView mRating;
        //        private CreatePosterTask task;
//        private Runnable runnable;

        public ViewHolder(View view) {
            super(view);//绑定RecyclerView.ViewHolder的itemView
            mTitle = (TextView) view.findViewById(R.id.tv_title);
            mImage = (ImageView) view.findViewById(R.id.iv_img);
            mRatingLayout = (RelativeLayout) view.findViewById(R.id.rv_rating);
            mRating = (TextView) view.findViewById(R.id.tv_rating);

        }

    }

    /**
     * 添加数据
     *
     * @param wrapper
     * @param position
     */
    public void addItem(MovieWrapper wrapper, int position) {
        list.add(position, wrapper);
        notifyItemInserted(position); // Attention!
    }


    public void removeAll() {
        list.clear();
        notifyDataSetChanged();
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, MovieWrapper wrapper);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setData(List<MovieWrapper> movieWrappers) {
        list = movieWrappers;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, (MovieWrapper) v.getTag());
        }

    }

}