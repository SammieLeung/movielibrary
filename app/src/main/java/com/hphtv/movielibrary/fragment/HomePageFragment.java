package com.hphtv.movielibrary.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;

import com.firelfy.util.LogUtil;
import com.hphtv.movielibrary.activity.MovieDetailActivity;
import com.hphtv.movielibrary.adapter.BaseAdapter;
import com.hphtv.movielibrary.adapter.MovieLibraryAdapter;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.databinding.FLayoutFavoriteBinding;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.MovieDataView;
import com.hphtv.movielibrary.viewmodel.fragment.HomePageFragementViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lxp
 * @date 19-5-15
 */
public class HomePageFragment extends BaseFragment<HomePageFragementViewModel, FLayoutFavoriteBinding> {
    private int mColums = 6;

    public static final String TAG = HomePageFragment.class.getSimpleName();

    private MovieLibraryAdapter mMovieLibraryAdapter;// 电影列表适配器
    private List<MovieDataView> mMovieDataViewList = new ArrayList<>();// 电影数据


    public static HomePageFragment newInstance(int pos) {

        Bundle args = new Bundle();
        args.putInt(ConstData.IntentKey.KEY_CUR_FRAGMENT, pos);
        HomePageFragment fragment = new HomePageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        LogUtil.v(TAG, "OnResume");
        super.onResume();
    }

    @Override
    protected void onViewCreated() {
        initView();
    }

    /**
     * 初始化
     */
    private void initView() {
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getContext(), mColums, GridLayoutManager.VERTICAL, false);
        mBinding.rvMovies.setLayoutManager(mGridLayoutManager);
        mMovieLibraryAdapter = new MovieLibraryAdapter(getContext(), mMovieDataViewList);
        mBinding.rvMovies.setAdapter(mMovieLibraryAdapter);
        mMovieLibraryAdapter
                .setOnItemClickListener((BaseAdapter.OnRecyclerViewItemClickListener<MovieDataView>) (view, data) -> {
                    Intent intent = new Intent(HomePageFragment.this.getContext(),
                            MovieDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong(ConstData.IntentKey.KEY_MOVIE_ID, data.id);
                    bundle.putInt("mode", ConstData.MovieDetailMode.MODE_WRAPPER);
                    intent.putExtras(bundle);
                    mActivityResultLauncher.launch(intent);
                });
    }

    public void notifyUpdate(Device device, String year, String genre, int sortType, boolean isDesc) {
        mViewModel.prepareMovies(device, year, genre, sortType, isDesc, args -> {
            List<MovieDataView> movieDataViews = (List<MovieDataView>) args[0];
            updateMovie(movieDataViews);
            notifyStopLoading();
        });
    }

    private void updateMovie(List<MovieDataView> movieDataViews) {
        if (movieDataViews.size() > 0) {
            mBinding.tipsEmpty.setVisibility(View.GONE);
        } else {
            mBinding.tipsEmpty.setVisibility(View.VISIBLE);
        }
        mMovieLibraryAdapter.addAll(movieDataViews);
    }

}
