package com.hphtv.movielibrary.ui.homepage;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.adapter.BaseScaleApater;
import com.hphtv.movielibrary.adapter.HistoryListAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FLayoutMovieBinding;
import com.hphtv.movielibrary.effect.GridSpacingItemDecorationVertical;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.ui.BaseFragment;
import com.station.kit.util.DensityUtil;

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
    private List<HistoryMovieDataView> mUnrecognizedFileDataViewList =new ArrayList<>();

    public static HistoryFragment newInstance(int pos) {
        Bundle args = new Bundle();
        args.putInt(Constants.Extras.CURRENT_FRAGMENT, pos);
        HistoryFragment fragment = new HistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onViewCreated() {
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false);
        mBinding.rvMovies.setLayoutManager(mGridLayoutManager);
        mHistoryListAdapter = new HistoryListAdapter(getContext(), mUnrecognizedFileDataViewList);
//        mBinding.rvMovies.addItemDecoration(new GridSpacingItemDecorationVertical(DensityUtil.dip2px(getContext(),40),DensityUtil.dip2px(getContext(),30),3));

        mHistoryListAdapter.setOnItemClickListener(new BaseScaleApater.OnRecyclerViewItemActionListener<HistoryMovieDataView>() {
            @Override
            public void onItemClick(View view, int postion, HistoryMovieDataView data) {
                mViewModel.playingVideo(data.path, data.filename, dataViewList -> {
                    updateMovie(dataViewList);
                    notifyStopLoading();
                });
            }

        });
        mBinding.rvMovies.setAdapter(mHistoryListAdapter);
    }

    public void notifyUpdate(){
        if (mViewModel != null)
            mViewModel.prepareHistory(unrecognizedFileDataViews -> {
            updateMovie(unrecognizedFileDataViews);
            notifyStopLoading();
        });
    }

    private void updateMovie(List<HistoryMovieDataView> historyList) {
        if (historyList.size() > 0) {
            mBinding.tipsEmpty.setVisibility(View.GONE);
        } else {
            mBinding.tipsEmpty.setVisibility(View.VISIBLE);
        }
        mHistoryListAdapter.addAll(historyList);
    }
}
