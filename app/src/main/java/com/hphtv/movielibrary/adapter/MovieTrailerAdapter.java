package com.hphtv.movielibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.MovieTrailerItemBinding;
import com.hphtv.movielibrary.roomdb.entity.Trailer;

import java.util.List;

public class MovieTrailerAdapter extends
        RecyclerView.Adapter<MovieTrailerAdapter.ViewHolder> implements OnClickListener {
    private static final String TAG = "MovieTrailerAdapter";
    private List<Trailer> list = null;

    private Context context;
    Boolean mIsNotLoop = false;

    public MovieTrailerAdapter(Context context, List<Trailer> trailerList) {
        this.list = trailerList;
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
        Trailer trailer = list.get(position);
        viewHolder.mTrailerItemBinding.tvTitle.setText(trailer.title);
            Glide.with(context).load(trailer.img)
                    .apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default)).into(viewHolder.mTrailerItemBinding.ivPoster);

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
        MovieTrailerItemBinding mTrailerItemBinding;

        public ViewHolder(View view) {
            super(view);//绑定RecyclerView.ViewHolder的itemView
            mTrailerItemBinding= DataBindingUtil.bind(view);
        }
    }


    public void addItems(List<Trailer> list) {
        if(list!=null) {
            this.list.addAll(list);
            notifyDataSetChanged();
        }
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
        void onItemClick(View view, Trailer data);
    }

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, (Trailer) v.getTag());
        }

    }

}