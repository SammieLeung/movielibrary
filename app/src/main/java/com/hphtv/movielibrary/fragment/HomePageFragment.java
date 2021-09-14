package com.hphtv.movielibrary.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.hphtv.movielibrary.databinding.FLayoutMovieBinding;
import com.hphtv.movielibrary.activity.MovieDetailActivity;
import com.hphtv.movielibrary.adapter.BaseAdapter;
import com.hphtv.movielibrary.adapter.MovieAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.viewmodel.fragment.HomePageFragementViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lxp
 * @date 19-5-15
 */
public class HomePageFragment extends BaseFragment<HomePageFragementViewModel, FLayoutMovieBinding> {

    public static final String TAG = HomePageFragment.class.getSimpleName();

    private MovieAdapter mMovieAdapter;// 电影列表适配器
    private List<MovieDataView> mMovieDataViewList = new ArrayList<>();// 电影数据


    public static HomePageFragment newInstance(int pos) {

        Bundle args = new Bundle();
        args.putInt(Constants.IntentKey.KEY_CUR_FRAGMENT, pos);
        HomePageFragment fragment = new HomePageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onViewCreated() {
        initView();
    }

    /**
     * 初始化
     */
    private void initView() {
        StaggeredGridLayoutManager mGridLayoutManager = new StaggeredGridLayoutManager( mColums, GridLayoutManager.VERTICAL);
        mBinding.rvMovies.setLayoutManager(mGridLayoutManager);
        mMovieAdapter = new MovieAdapter(getContext(), mMovieDataViewList);
        mMovieAdapter.setOnItemClickListener((view, data) -> {
            Intent intent = new Intent(HomePageFragment.this.getContext(),
                    MovieDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putLong(Constants.IntentKey.KEY_MOVIE_ID, data.id);
            bundle.putInt(Constants.IntentKey.KEY_MODE, Constants.MovieDetailMode.MODE_WRAPPER);
            intent.putExtras(bundle);
            startActivityForResultFromParent(intent);
        });
        mBinding.rvMovies.setAdapter(mMovieAdapter);

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
        mMovieAdapter.addAll(movieDataViews);
    }

}
