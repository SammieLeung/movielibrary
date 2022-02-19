package com.hphtv.movielibrary.ui.homepage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.adapter.BaseApater2;
import com.hphtv.movielibrary.adapter.GenreTagAdapter;
import com.hphtv.movielibrary.adapter.HistoryListAdapter;
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.databinding.ActivityNewpageBinding;
import com.hphtv.movielibrary.effect.SpacingItemDecoration;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;
import com.station.kit.util.DensityUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/11/5
 */
public class NewPageFragment extends BaseAutofitHeightFragment<NewpageViewModel, ActivityNewpageBinding> {
    private HistoryListAdapter mHistoryListAdapter;
    private GenreTagAdapter mGenreTagAdapter;
    private NewMovieItemListAdapter mRecentlyAddListAdapter;
    private NewMovieItemListAdapter mFavoriteListAdapter;
    private NewMovieItemListAdapter mRecommandListAdapter;

    private List<HistoryMovieDataView> mRecentlyPlayedList = new ArrayList<>();
    private List<String> mGenreTagList = new ArrayList<>();
    private List<MovieDataView> mRecentlyAddedList = new ArrayList<>();
    private List<MovieDataView> mFavoriteList = new ArrayList<>();
    private List<MovieDataView> mRecommandList = new ArrayList<>();

    public NewPageFragment(IAutofitHeight autofitHeight,int postion) {
        super(autofitHeight, postion);
    }

    public static NewPageFragment newInstance(IAutofitHeight autofitHeight, int positon) {
        Bundle args = new Bundle();
        NewPageFragment fragment = new NewPageFragment(autofitHeight,positon);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
    }

    @Override
    protected boolean createViewModel() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareHistoryData();
        prepareMovieGenreTagData();
        prepareRecentlyAddedMovie();
        prepareFavorite();
        prepareRecommand();
    }

    private void initViews() {
        initRecentlyPlayedList();
        initGenreList();
        initRecentlyAddedList();
        initFavoriteList();
        initRecommandList();
    }

    /**
     * 初始化最近观看
     */
    private void initRecentlyPlayedList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvHistoryList.setLayoutManager(mLayoutManager);
        mBinding.rvHistoryList.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 15)));
        mHistoryListAdapter = new HistoryListAdapter(getContext(), mRecentlyPlayedList);
        mBinding.rvHistoryList.setAdapter(mHistoryListAdapter);
        mHistoryListAdapter.setOnItemClickListener(new BaseApater2.OnRecyclerViewItemActionListener<HistoryMovieDataView>() {
            @Override
            public void onItemClick(View view, int postion, HistoryMovieDataView data) {
                mViewModel.playingVideo(data.path, data.filename, list -> {

                });
            }
        });
    }

    /**
     * 初始化电影类型分类列表
     */
    private void initGenreList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvGenreList.setLayoutManager(mLayoutManager);
        mBinding.rvGenreList.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 12), DensityUtil.dip2px(getContext(), 12)));
        mGenreTagAdapter = new GenreTagAdapter(getContext(), mGenreTagList);
        mBinding.rvGenreList.setAdapter(mGenreTagAdapter);
        mGenreTagAdapter.setOnGenreListener(new GenreTagAdapter.GenreListener() {
            @Override
            public void addGenre() {

            }
        });
        mGenreTagAdapter.setOnItemClickListener((view, postion, data) -> {

        });
    }

    /**
     * 初始化最新添加列表
     */
    private void initRecentlyAddedList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvRecentlyAdded.setLayoutManager(mLayoutManager);
        mBinding.rvRecentlyAdded.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 30)));
        mRecentlyAddListAdapter = new NewMovieItemListAdapter(getContext(), mRecentlyAddedList);
//        mRecentlyAddListAdapter.setOnItemClickListener(new BaseAdapter2.OnRecyclerViewItemActionListener<MovieDataView>() {
//
//            @Override
//            public void onItemClick(View view, int postion, MovieDataView data) {
//
//            }
//
//            @Override
//            public void onItemFocus(View view, boolean hasFocus) {
//                if (hasFocus) {
//                    mBinding.scrollView.smoothScrollTo(0, 600);
//                }
//            }
//        });
        mBinding.rvRecentlyAdded.setAdapter(mRecentlyAddListAdapter);

    }

    /**
     * 初始化我的收藏
     */
    private void initFavoriteList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvFavorite.setLayoutManager(mLayoutManager);
        mBinding.rvFavorite.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 30)));
        mFavoriteListAdapter = new NewMovieItemListAdapter(getContext(), mFavoriteList);
        mBinding.rvFavorite.setAdapter(mFavoriteListAdapter);
    }

    private void initRecommandList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvRecommand.setLayoutManager(mLayoutManager);
        mBinding.rvRecommand.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 30)));
        mRecommandListAdapter = new NewMovieItemListAdapter(getContext(), mRecommandList);
        mBinding.rvRecommand.setAdapter(mRecommandListAdapter);
    }


    /**
     * 读取历史记录数据
     */
    public void prepareHistoryData() {
        mViewModel.prepareHistory(list -> updateRecentlyPlayed((List<HistoryMovieDataView>) list));
    }

    private void updateRecentlyPlayed(List<HistoryMovieDataView> historyList) {
        if (historyList.size() > 0) {
            mBinding.rvHistoryList.setVisibility(View.VISIBLE);
            mBinding.tvHistoryEmptyTips.setVisibility(View.GONE);
        } else {
            mBinding.tvHistoryEmptyTips.setVisibility(View.VISIBLE);
            mBinding.rvHistoryList.setVisibility(View.GONE);
        }
        mHistoryListAdapter.addAll(historyList);
    }

    private void prepareMovieGenreTagData() {
        mViewModel.prepareGenreList(list -> mGenreTagAdapter.addAll(list));
    }

    private void prepareRecentlyAddedMovie() {
        mViewModel.prepareRecentlyAddedMovie(list -> {
            if (list.size() > 0) {
                mRecentlyAddListAdapter.addAll(list);
                mBinding.setRecentAdd(true);
            } else {
                mBinding.setRecentAdd(false);
            }
        });
    }

    private void prepareFavorite() {
        mViewModel.prepareFavorite(list -> {
            if (list.size() > 0) {
                mFavoriteListAdapter.addAll(list);
                mBinding.setFavorite(true);
            } else {
                mBinding.setFavorite(false);
            }
        });
    }

    private void prepareRecommand() {
        mViewModel.prepareRecommand(list -> {
            if (list.size() > 0) {
                mRecommandListAdapter.addAll(list);
                mBinding.setRecommand(true);
            } else {
                mBinding.setRecommand(false);
            }
        });
    }

}
