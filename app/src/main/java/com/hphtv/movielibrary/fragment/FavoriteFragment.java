package com.hphtv.movielibrary.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hphtv.movielibrary.activity.HomePageActivity;
import com.hphtv.movielibrary.activity.MovieDetailActivity;
import com.hphtv.movielibrary.adapter.MovieLibraryAdapter;
import com.hphtv.movielibrary.adapter.MovieLibraryAdapter.OnRecyclerViewItemClickListener;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.sqlite.bean.Favorite;
import com.hphtv.movielibrary.sqlite.bean.MovieWrapper;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.sqlite.dao.FavoriteDao;
import com.hphtv.movielibrary.sqlite.dao.MovieWrapperDao;
import com.hphtv.movielibrary.sqlite.dao.VideoFileDao;
import com.hphtv.movielibrary.view.RecyclerViewWithMouseScroll;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by tchip on 18-5-9.
 */

public class FavoriteFragment extends Fragment {
    public static final String TAG = FavoriteFragment.class.getSimpleName();
    private RecyclerViewWithMouseScroll mRVMovies;
    private TextView mTips;

    private HomePageActivity mContext;
    private MovieLibraryAdapter mAdapter;// 电影列表适配器
    private List<MovieWrapper> mWrapperList = new ArrayList<>();// 电影数据
    private static final int COLUMS = 8;
    private Handler handler = new Handler();

    private FavoriteDao mFarvoriteDao;
    private MovieWrapperDao mMovieWrapperDao;
    private static AtomicBoolean atomicBoolean = new AtomicBoolean();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_layout_favorite, container, false);
        initView(view);
        return view;
    }


    @Override
    public void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
        initMovie();
    }

    /**
     * 初始化电影
     */
    public void initMovie() {
        Log.v(TAG, "initMovie");
        getFavoritMovie();
    }

    /**
     * 初始化
     */
    private void initView(View view) {
        mContext = (HomePageActivity) getActivity();
        mFarvoriteDao = new FavoriteDao(mContext);
        mMovieWrapperDao = new MovieWrapperDao(mContext);
        mTips = (TextView) view.findViewById(R.id.tips_empty);
        mRVMovies = (RecyclerViewWithMouseScroll) view.findViewById(R.id.rv_movies);
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(mContext, COLUMS, GridLayoutManager.VERTICAL, false);
        mRVMovies.setLayoutManager(mGridLayoutManager);
        mAdapter = new MovieLibraryAdapter(mContext, mWrapperList);
        mRVMovies.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, MovieWrapper wrapper) {
                Intent intent = new Intent(mContext,
                        MovieDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("wrapper", wrapper);
                bundle.putInt("mode", ConstData.MovieDetailMode.MODE_WRAPPER);
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
            }
        });

    }

    /**
     *
     */
    private void getFavoritMovie() {
        if (!atomicBoolean.get()) {
            atomicBoolean.set(true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HomePageActivity ac = (HomePageActivity) mContext;
                    long dev_id = ac.getFilterDeviceId();
                    String dir_id = ac.getFilterDirId();
                    StringBuffer buffer = new StringBuffer();
                    if (!TextUtils.isEmpty(dir_id)) {
                        buffer.append("dir_ids like '%" + dir_id.trim() + "%' and ");
                    } else if (dev_id != -1) {
                        buffer.append("(dev_ids like \"%" + dev_id + ",%\" or dev_ids like \"%" + dev_id + "]%\") and ");
                    }
                    buffer.append("id=?");

                    mWrapperList.clear();
                    Cursor favoriteCursor = mFarvoriteDao.select(null, null, null, null, null, "id desc", null);
                    if (favoriteCursor.getCount() > 0) {
                        List<Favorite> favoriteList = mFarvoriteDao.parseList(favoriteCursor);
                        for (int i = 0; i < favoriteList.size(); i++) {
                            long wrapper_id = favoriteList.get(i).getWrapper_id();
                            Cursor cursor = mMovieWrapperDao.select(buffer.toString(), new String[]{String.valueOf(wrapper_id)}, null);
                            if (cursor.getCount() > 0) {
                                MovieWrapper wrapper = mMovieWrapperDao.parseList(cursor).get(0);
                                mWrapperList.add(wrapper);
                            }
                        }

                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshMovie();
                        }
                    });
                    atomicBoolean.set(false);
                }
            }).start();
        }
    }

    private void refreshMovie() {
        if (mWrapperList.size() > 0) {
            mTips.setVisibility(View.GONE);
        } else {
            mTips.setVisibility(View.VISIBLE);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult resultCode=" + resultCode);
    }
}
