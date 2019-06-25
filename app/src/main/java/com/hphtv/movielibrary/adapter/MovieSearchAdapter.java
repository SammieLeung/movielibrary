package com.hphtv.movielibrary.adapter;

import java.util.List;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Rating;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.SimpleMovie;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class MovieSearchAdapter extends BaseAdapter {
    private List<SimpleMovie> datalist;
    private LayoutInflater inflater;
    private Context context;

    public MovieSearchAdapter(Context context, List<SimpleMovie> list) {
        this.context = context;
        this.datalist = list;
        this.inflater = LayoutInflater.from(this.context);
    }

    @Override
    public int getCount() {
        return datalist.size();
    }

    @Override
    public Object getItem(int arg0) {
        return datalist.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup arg2) {
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            //获得组件，实例化组件
            view = inflater.inflate(R.layout.search_list_item, null);
            viewHolder.setIv_sli_cover((ImageView) view.findViewById(R.id.iv_sli_cover));
            viewHolder.setRb_sli_star((RatingBar) view.findViewById(R.id.rb_sli_star));
            viewHolder.setTv_sli_title((TextView) view.findViewById(R.id.tv_sli_title));
            viewHolder.setTv_sli_rate((TextView) view.findViewById(R.id.tv_sli_rate));
            viewHolder.setTv_sli_cnum((TextView) view.findViewById(R.id.tv_sli_cnum));
            viewHolder.setTv_sli_abstract((TextView) view.findViewById(R.id.tv_sli_abstract));
            viewHolder.setTv_sli_abstract2((TextView) view.findViewById(R.id.tv_sli_abstract2));
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        SimpleMovie simpleMovie = (SimpleMovie) datalist.get(position);
        Glide.with(context).load(simpleMovie.getImages().getLarge()).apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default)).into(viewHolder.getIv_sli_cover());
        viewHolder.getTv_sli_title().setText(simpleMovie.getTitle());
        RatingBar ratingBar = viewHolder.getRb_sli_star();
        Rating rating = simpleMovie.getRating();
        ratingBar.setRating(rating.average * 5 / rating.max);
        viewHolder.getTv_sli_rate().setText(String.valueOf(rating.average));
        viewHolder.getTv_sli_cnum().setText(simpleMovie.getRatingsCounts());
        viewHolder.getTv_sli_abstract().setText(simpleMovie.getAbstracts()[0]);
        viewHolder.getTv_sli_abstract2().setText(simpleMovie.getAbstracts()[1]);
        return view;
    }

    public class ViewHolder {
        private ImageView iv_sli_cover;
        private TextView tv_sli_title;
        private RatingBar rb_sli_star;
        private TextView tv_sli_cnum;
        private TextView tv_sli_rate;
        private TextView tv_sli_abstract;
        private TextView tv_sli_abstract2;

        public ImageView getIv_sli_cover() {
            return iv_sli_cover;
        }

        public void setIv_sli_cover(ImageView iv_sli_cover) {
            this.iv_sli_cover = iv_sli_cover;
        }

        public TextView getTv_sli_title() {
            return tv_sli_title;
        }

        public void setTv_sli_title(TextView tv_sli_title) {
            this.tv_sli_title = tv_sli_title;
        }

        public RatingBar getRb_sli_star() {
            return rb_sli_star;
        }

        public void setRb_sli_star(RatingBar rb_sli_star) {
            this.rb_sli_star = rb_sli_star;
        }

        public TextView getTv_sli_cnum() {
            return tv_sli_cnum;
        }

        public void setTv_sli_cnum(TextView tv_sli_cnum) {
            this.tv_sli_cnum = tv_sli_cnum;
        }

        public TextView getTv_sli_rate() {
            return tv_sli_rate;
        }

        public void setTv_sli_rate(TextView tv_sli_rate) {
            this.tv_sli_rate = tv_sli_rate;
        }

        public TextView getTv_sli_abstract() {
            return tv_sli_abstract;
        }

        public void setTv_sli_abstract(TextView tv_sli_abstract) {
            this.tv_sli_abstract = tv_sli_abstract;
        }

        public TextView getTv_sli_abstract2() {
            return tv_sli_abstract2;
        }

        public void setTv_sli_abstract2(TextView tv_sli_abstract2) {
            this.tv_sli_abstract2 = tv_sli_abstract2;
        }

    }

    public interface OnDpadKeyListener {
        public void OnDpadKeyPress(View v, int keyCode, KeyEvent event);
    }

    private OnDpadKeyListener mOnDpadKeyListener;

    public void setmOnDpadKeyListener(OnDpadKeyListener mOnDpadKeyListener) {
        this.mOnDpadKeyListener = mOnDpadKeyListener;
    }
}
