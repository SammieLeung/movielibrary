package com.hphtv.movielibrary.ui.homepage;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseAdapter2;
import com.hphtv.movielibrary.adapter.CircleItemAdapter;
import com.hphtv.movielibrary.adapter.GenreTagAdapter;
import com.hphtv.movielibrary.adapter.HistoryListAdapter;
import com.hphtv.movielibrary.adapter.RecentlyAddListAdapter;
import com.hphtv.movielibrary.databinding.ActivityNewpageBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.ui.AppBaseActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/11/5
 */
public class NewPageAcitivity extends AppBaseActivity<NewpageViewModel, ActivityNewpageBinding> {
    private HistoryListAdapter mHistoryListAdapter;
    private CircleItemAdapter mCircleItemAdapter;
    private GenreTagAdapter mGenreTagAdapter;
    private RecentlyAddListAdapter mRecentlyAddListAdapter;
    private List<UnrecognizedFileDataView> mRecentlyPlayedList = new ArrayList<>();
    private List<String> mGenreTagList = new ArrayList<>();
    private List<MovieDataView> mRecentlyAddedList = new ArrayList<>();

    @Override
    protected void onCreate() {
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepareHistoryData();
        prepareMovieGenreTagData();
        prepareRecentlyAddedMovie();
    }

    private void initViews() {
        initRecentlyPlayedList();
        initCategoryList();
        initGenreList();
        initRecentlyAddedList();
    }

    /**
     * 初始化最近观看
     */
    private void initRecentlyPlayedList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvHistoryList.setLayoutManager(mLayoutManager);
        mHistoryListAdapter = new HistoryListAdapter(this, mRecentlyPlayedList);
        mHistoryListAdapter.setOnItemClickListener(new BaseAdapter2.OnRecyclerViewItemActionListener<UnrecognizedFileDataView>() {

            @Override
            public void onItemClick(View view, int postion, UnrecognizedFileDataView data) {
                mViewModel.playingVideo(data.path, data.filename, list -> {
                    updateRecentlyPlayed((List<UnrecognizedFileDataView>) list);
//                notifyStopLoading();
                });
            }

            @Override
            public void onItemFocus(View view, boolean hasFocus) {
                if (hasFocus) {
                    mBinding.scrollView.smoothScrollTo(0, 0);
                }
            }
        });
        mBinding.rvHistoryList.setAdapter(mHistoryListAdapter);
    }


    /**
     * 初始化分类列表
     */
    private void initCategoryList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvCategoryList.setLayoutManager(mLayoutManager);
        List<String> categoryList = Arrays.asList(getResources().getStringArray(R.array.category_title_array).clone());
        mCircleItemAdapter = new CircleItemAdapter(this, categoryList);
        mCircleItemAdapter.setOnItemClickListener(new BaseAdapter2.OnRecyclerViewItemActionListener<String>() {
            @Override
            public void onItemClick(View view, int postion, String data) {

            }

            @Override
            public void onItemFocus(View view, boolean hasFocus) {
                if (hasFocus) {
                    mBinding.scrollView.smoothScrollTo(0, 200);
                }
            }
        });
        mBinding.rvCategoryList.setAdapter(mCircleItemAdapter);
    }


    /**
     * 初始化电影类型分类列表
     */
    private void initGenreList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvGenreList.setLayoutManager(mLayoutManager);
        mGenreTagAdapter = new GenreTagAdapter(this, mGenreTagList);
        mGenreTagAdapter.setOnItemClickListener(new BaseAdapter2.OnRecyclerViewItemActionListener<String>() {
            @Override
            public void onItemClick(View view, int postion, String data) {
                if(postion==mGenreTagAdapter.getItemCount()-1){

                }
            }

            @Override
            public void onItemFocus(View view, boolean hasFocus) {
                if (hasFocus) {
                    mBinding.scrollView.smoothScrollTo(0, 400);
                }
            }
        });

        mBinding.rvGenreList.setAdapter(mGenreTagAdapter);

    }

    /**
     * 初始化最新添加列表
     */
    private void initRecentlyAddedList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvRecentlyAdded.setLayoutManager(mLayoutManager);
        mRecentlyAddListAdapter = new RecentlyAddListAdapter(this, mRecentlyAddedList);
        mRecentlyAddListAdapter.setOnItemClickListener(new BaseAdapter2.OnRecyclerViewItemActionListener<MovieDataView>() {

            @Override
            public void onItemClick(View view, int postion, MovieDataView data) {

            }

            @Override
            public void onItemFocus(View view, boolean hasFocus) {
                if (hasFocus) {
                    mBinding.scrollView.smoothScrollTo(0, 600);
                }
            }
        });
        mBinding.rvRecentlyAdded.setAdapter(mRecentlyAddListAdapter);

    }


    /**
     * 读取历史记录数据
     */
    public void prepareHistoryData() {
        mViewModel.prepareHistory(list -> updateRecentlyPlayed((List<UnrecognizedFileDataView>) list));
    }

    private void updateRecentlyPlayed(List<UnrecognizedFileDataView> unrecognizedFileDataViews) {
        if (unrecognizedFileDataViews.size() > 0) {
            mBinding.rvHistoryList.setVisibility(View.VISIBLE);
            mBinding.tvHistoryEmptyTips.setVisibility(View.GONE);
        } else {
            mBinding.tvHistoryEmptyTips.setVisibility(View.VISIBLE);
            mBinding.rvHistoryList.setVisibility(View.GONE);
        }
        mHistoryListAdapter.addAll(unrecognizedFileDataViews);
    }

    private void prepareMovieGenreTagData() {
        mViewModel.prepareGenreList(list -> mGenreTagAdapter.addAll(list));
    }

    private void prepareRecentlyAddedMovie() {
        mViewModel.prepareRecentlyAddedMovie(list -> {
            mRecentlyAddListAdapter.addAll(list);
        });
    }
}
