package com.hphtv.movielibrary.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.activity.MovieDetailActivity;
import com.hphtv.movielibrary.adapter.BaseAdapter;
import com.hphtv.movielibrary.adapter.MovieAdapter;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.databinding.FLayoutMovieBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.viewmodel.fragment.HistoryFragmentViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tchip on 18-5-25.
 */

public class HistoryFragment extends BaseFragment<HistoryFragmentViewModel, FLayoutMovieBinding> {
    private MovieAdapter mMovieAdapter;
    private List<MovieDataView> mMovieDataViewList=new ArrayList<>();

    public static HistoryFragment newInstance(int pos) {
        Bundle args = new Bundle();
        args.putInt(ConstData.IntentKey.KEY_CUR_FRAGMENT, pos);
        HistoryFragment fragment = new HistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onViewCreated() {
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getContext(), mColums, GridLayoutManager.VERTICAL, false);
        mBinding.rvMovies.setLayoutManager(mGridLayoutManager);
        mMovieAdapter = new MovieAdapter(getContext(), mMovieDataViewList);
        mMovieAdapter.setOnItemClickListener((BaseAdapter.OnRecyclerViewItemClickListener<MovieDataView>) (view, data) -> {
            Intent intent = new Intent(getContext(),
                    MovieDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putLong(ConstData.IntentKey.KEY_MOVIE_ID, data.id);
            bundle.putInt(ConstData.IntentKey.KEY_MODE, ConstData.MovieDetailMode.MODE_WRAPPER);
            intent.putExtras(bundle);
            startActivityForResultFromParent(intent);
        });
        mBinding.rvMovies.setAdapter(mMovieAdapter);
    }

    public void notifyUpdate(){
        mViewModel.prepareHistory(dataViewList -> {
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
