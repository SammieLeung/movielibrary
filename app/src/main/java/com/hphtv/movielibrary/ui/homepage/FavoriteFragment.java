package com.hphtv.movielibrary.ui.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.effect.GridSpacingItemDecorationVertical;
import com.hphtv.movielibrary.ui.detail.MovieDetailActivity;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FLayoutMovieBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.BaseFragment;
import com.station.kit.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tchip on 18-5-25.
 */

public class FavoriteFragment extends BaseFragment<FavoriteFragmentViewModel, FLayoutMovieBinding> {
    private NewMovieItemListAdapter mMovieAdapter;
    private List<MovieDataView> mMovieDataViewList=new ArrayList<>();

    public static FavoriteFragment newInstance(int pos) {
        Bundle args = new Bundle();
        args.putInt(Constants.Extras.CURRENT_FRAGMENT, pos);
        FavoriteFragment fragment = new FavoriteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onViewCreated() {
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getContext(), 5, GridLayoutManager.VERTICAL, false);
        mBinding.rvMovies.setLayoutManager(mGridLayoutManager);
        mMovieAdapter = new NewMovieItemListAdapter(getContext(), mMovieDataViewList);
        mBinding.rvMovies.addItemDecoration(new GridSpacingItemDecorationVertical(DensityUtil.dip2px(getContext(),40),DensityUtil.dip2px(getContext(),30),5));
        mMovieAdapter.setOnItemClickListener((view, postion,data) -> {
            Intent intent = new Intent(getContext(),
                    MovieDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putLong(Constants.Extras.MOVIE_ID, data.id);
            bundle.putInt(Constants.Extras.MODE, Constants.MovieDetailMode.MODE_WRAPPER);
            intent.putExtras(bundle);
            startActivityForResultFromParent(intent);
        });
        mBinding.rvMovies.setAdapter(mMovieAdapter);
    }

    public void notifyUpdate(){
        if (mViewModel != null)
            mViewModel.prepareFavorite(dataViewList -> {
            updateMovie(dataViewList);
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
