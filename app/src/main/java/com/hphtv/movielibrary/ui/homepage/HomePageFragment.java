package com.hphtv.movielibrary.ui.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.databinding.FLayoutMovieBinding;
import com.hphtv.movielibrary.effect.GridSpacingItemDecorationVertical;
import com.hphtv.movielibrary.effect.SpacingItemDecoration;
import com.hphtv.movielibrary.ui.detail.MovieDetailActivity;
import com.hphtv.movielibrary.adapter.MovieAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.BaseFragment;
import com.station.kit.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lxp
 * @date 19-5-15
 */
public class HomePageFragment extends BaseFragment<HomePageFragementViewModel, FLayoutMovieBinding> {

    public static final String TAG = HomePageFragment.class.getSimpleName();

    private NewMovieItemListAdapter mMovieAdapter;// 电影列表适配器
    private List<MovieDataView> mMovieDataViewList = new ArrayList<>();// 电影数据


    public static HomePageFragment newInstance(int pos) {

        Bundle args = new Bundle();
        args.putInt(Constants.Extras.CURRENT_FRAGMENT, pos);
        HomePageFragment fragment = new HomePageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onViewCreated() {
        initView();
    }

    /**
     * 初始化
     */
    private void initView() {
        StaggeredGridLayoutManager mGridLayoutManager = new StaggeredGridLayoutManager(5, GridLayoutManager.VERTICAL);
//        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getContext(),5);

        mBinding.rvMovies.setLayoutManager(mGridLayoutManager);
        mBinding.rvMovies.addItemDecoration(new GridSpacingItemDecorationVertical(DensityUtil.dip2px(getContext(),40),DensityUtil.dip2px(getContext(),30),5));

        mMovieAdapter = new NewMovieItemListAdapter(getContext(), mMovieDataViewList);
        mMovieAdapter.setOnItemClickListener((view, postion, data) -> {
            Intent intent = new Intent(HomePageFragment.this.getContext(),
                    MovieDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putLong(Constants.Extras.MOVIE_ID, data.id);
            bundle.putInt(Constants.Extras.MODE, Constants.MovieDetailMode.MODE_WRAPPER);
            intent.putExtras(bundle);
            startActivityForResultFromParent(intent);
        });
        mBinding.rvMovies.setAdapter(mMovieAdapter);

    }

    public void notifyUpdate(Device device, String year, String genre, int sortType, boolean isDesc) {
        if (mViewModel != null)
            mViewModel.prepareMovies(device, year, genre, sortType, isDesc, args -> {
                List<MovieDataView> movieDataViews = (List<MovieDataView>) args[0];
                refresh(movieDataViews);
                notifyStopLoading();
            });
    }

    public void addMovie(String movie_id) {
        mViewModel.getMovieDataView(movie_id, args -> {
            if (args != null && args.length > 0) {
                mBinding.tipsEmpty.setVisibility(View.GONE);
                MovieDataView movieDataView = (MovieDataView) args[0];
                mMovieAdapter.put(movieDataView);
            }
        });
    }

    private void refresh(List<MovieDataView> movieDataViews) {
        if (movieDataViews.size() > 0) {
            mBinding.tipsEmpty.setVisibility(View.GONE);
        } else {
            mBinding.tipsEmpty.setVisibility(View.VISIBLE);
        }
        mMovieAdapter.addAll(movieDataViews);
    }


}
