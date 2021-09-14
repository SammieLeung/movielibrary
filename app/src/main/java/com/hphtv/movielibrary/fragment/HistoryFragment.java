package com.hphtv.movielibrary.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.activity.MovieDetailActivity;
import com.hphtv.movielibrary.adapter.BaseAdapter;
import com.hphtv.movielibrary.adapter.HistoryListAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FLayoutMovieBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.viewmodel.MovieDetailViewModel;
import com.hphtv.movielibrary.viewmodel.fragment.HistoryFragmentViewModel;
import com.station.kit.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tchip on 18-5-25.
 *
 *
 */
//TODO 截图效果参考hphplayer项目
// file:///home/lxp/expand/Project/StationOSProject/hphplayer/app/src/main/java/com/hph/videoplayer/other/VideoSearchAdapter
// getThumbnail方法
public class HistoryFragment extends BaseFragment<HistoryFragmentViewModel, FLayoutMovieBinding> {
    private HistoryListAdapter mHistoryListAdapter;
    private List<UnrecognizedFileDataView> mUnrecognizedFileDataViewList =new ArrayList<>();

    public static HistoryFragment newInstance(int pos) {
        Bundle args = new Bundle();
        args.putInt(Constants.IntentKey.KEY_CUR_FRAGMENT, pos);
        HistoryFragment fragment = new HistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onViewCreated() {
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getContext(), mColums, GridLayoutManager.VERTICAL, false);
        mBinding.rvMovies.setLayoutManager(mGridLayoutManager);
        mHistoryListAdapter = new HistoryListAdapter(getContext(), mUnrecognizedFileDataViewList);
        mHistoryListAdapter.setOnItemClickListener((view, data) -> {
            mViewModel.playingVideo(data.path,data.filename);
        });
        mBinding.rvMovies.setAdapter(mHistoryListAdapter);
    }

    public void notifyUpdate(){
        mViewModel.prepareHistory(unrecognizedFileDataViews -> {
            updateMovie(unrecognizedFileDataViews);
            notifyStopLoading();
        });
    }

    private void updateMovie(List<UnrecognizedFileDataView> unrecognizedFileDataViews) {
        if (unrecognizedFileDataViews.size() > 0) {
            mBinding.tipsEmpty.setVisibility(View.GONE);
        } else {
            mBinding.tipsEmpty.setVisibility(View.VISIBLE);
        }
        mHistoryListAdapter.addAll(unrecognizedFileDataViews);
    }
}
