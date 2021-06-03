package com.hphtv.movielibrary.adapter;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.MovieTrailer;

public class MovieTrailerAdapter extends
        RecyclerView.Adapter<MovieTrailerAdapter.ViewHolder> implements OnClickListener {
    private static final String TAG = "MovieTrailerAdapter";
    private List<MovieTrailer> list = null;

    private Context context;
    Handler handler = new Handler();
    Boolean mIsNotLoop = false;

    public MovieTrailerAdapter(Context context, List<MovieTrailer> datas) {
        this.list = datas;
        this.context = context;
    }

    // 创建新View，被LayoutManager所调用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.movie_trailer_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        //将创建的View注册点击事件
        view.setOnClickListener(this);
        return vh;
    }

    // 将数据与界面进行绑定的操作
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int rawposition) {
        int position = 0;

        if (list.size() != 0) {
            position = rawposition % list.size();
        } else {
            return;
        }
        MovieTrailer movieTrailer = list.get(position);
        viewHolder.mTitle.setText(movieTrailer.getTitle());
        viewHolder.mDuration.setText(movieTrailer.getDuration());
        ImageView imageView = viewHolder.mPoster;
//        if (movieTrailer.getBitmap() != null) {
//            imageView.setImageBitmap(movieTrailer.getBitmap());
//        } else {
            Glide.with(context).load(movieTrailer.getPhoto())
                    .apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default)).into(imageView);
//        }

        //将数据保存在itemView的Tag中，以便点击时进行获取
        viewHolder.itemView.setTag(list.get(position));

    }

    // 获取数据的数量
    @Override
    public int getItemCount() {
        return mIsNotLoop ? list.size() : Integer.MAX_VALUE;
    }

    // 自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTitle;
        TextView mDuration;
        ImageView mPoster;

        public ViewHolder(View view) {
            super(view);//绑定RecyclerView.ViewHolder的itemView
            mTitle = (TextView) view.findViewById(R.id.tv_title);
            mDuration = (TextView) view.findViewById(R.id.tv_duration);
            mPoster = (ImageView) view.findViewById(R.id.iv_poster);
        }
    }

    /**
     * 添加数据
     *
     * @param content
     * @param position
     */
    public void addItem(MovieTrailer content, int position) {
        list.add(position, content);
        notifyItemInserted(position); // Attention!
    }

    public void addItems(List<MovieTrailer> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void modifyItems(int started, int end) {
        notifyItemRangeChanged(started, end);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public void removeAll() {
        int size = list.size();
        list.clear();
        notifyItemRangeRemoved(0, size);

    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, MovieTrailer data);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        Log.v(TAG, "onClick() mOnItemClickListener= " + mOnItemClickListener);
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, (MovieTrailer) v.getTag());
        }

    }

}