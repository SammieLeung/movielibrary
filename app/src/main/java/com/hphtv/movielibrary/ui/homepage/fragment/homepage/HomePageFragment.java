package com.hphtv.movielibrary.ui.homepage.fragment.homepage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.adapter.BaseApater2;
import com.hphtv.movielibrary.adapter.GenreTagAdapter;
import com.hphtv.movielibrary.adapter.HistoryListAdapter;
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.databinding.FragmentHomepageBinding;
import com.hphtv.movielibrary.effect.SpacingItemDecoration;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.filterpage.FilterPageActivity;
import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment;
import com.hphtv.movielibrary.ui.homepage.IActivityResult;
import com.hphtv.movielibrary.ui.homepage.IAutofitHeight;
import com.hphtv.movielibrary.ui.homepage.genretag.AddGenreDialogFragment;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;
import com.hphtv.movielibrary.util.ActivityHelper;
import com.station.kit.util.DensityUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/11/5
 */
public class HomePageFragment extends BaseAutofitHeightFragment<HomeFragmentViewModel, FragmentHomepageBinding> {
    public static final String TAG = HomePageFragment.class.getSimpleName();
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
    //电影点击监听
    private BaseApater2.OnRecyclerViewItemActionListener<MovieDataView> mMovieDataViewEventListener = new BaseApater2.OnRecyclerViewItemActionListener<MovieDataView>() {
        @Override
        public void onItemClick(View view, int postion, MovieDataView data) {
            mViewModel.startDetailActivity((AppBaseActivity) getActivity(), data);
        }

        @Override
        public void onItemFocus(View view, int postion, MovieDataView data) {

        }
    };
    //固定主题动作监听
    private GenreTagAdapter.GenreListener mGenreListener = new GenreTagAdapter.GenreListener() {
        @Override
        public void addGenre() {
            AddGenreDialogFragment fragment = AddGenreDialogFragment.newInstance();
            fragment.show(getChildFragmentManager(), "Add Genres");
        }

        @Override
        public void browseAll() {
            Intent intent = new Intent(getContext(), FilterPageActivity.class);
            startActivityForResult(intent);
        }
    };

    //动态主题动作监听
    private BaseApater2.OnRecyclerViewItemActionListener mGenreItemClickListener = new BaseApater2.OnRecyclerViewItemActionListener() {
        @Override
        public void onItemClick(View view, int postion, Object data) {
            Intent intent = new Intent(getContext(), FilterPageActivity.class);
            intent.putExtra(FilterPageActivity.EXTRA_GENRE, data.toString());
            startActivityForResult(intent);
        }

        @Override
        public void onItemFocus(View view, int postion, Object data) {

        }
    };

    private BaseApater2.OnItemLongClickListener<MovieDataView> mPosterItemLongClickListener = (view, postion, data) -> {
        ActivityHelper.showPosterMenuDialog(getChildFragmentManager(), postion, data);
        return false;
    };

    //TvRecyclerView的按键处理(按键均被监听)
    private TvRecyclerView.OnKeyPressListener mOnKeyPressListener = new TvRecyclerView.OnKeyPressListener() {
        @Override
        public void processKeyEvent(int keyCode) {
        }

        @Override
        public void onBackPress() {
            getActivity().finish();
        }
    };

    public HomePageFragment(IAutofitHeight autofitHeight, int postion) {
        super(autofitHeight, postion);
    }

    public static HomePageFragment newInstance(IAutofitHeight autofitHeight, int positon) {
        Bundle args = new Bundle();
        HomePageFragment fragment = new HomePageFragment(autofitHeight, positon);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        prepareAll();
    }

    @Override
    protected HomeFragmentViewModel createViewModel() {
        return null;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden)
            Log.e(TAG, "onHiddenChanged: " + this.toString());
    }


    private void initViews() {
        mBinding.btnQuickAddShortcut.setOnClickListener(getBaseActivity()::startShortcutManager);
        initRecentlyPlayedList();
        initGenreList();
        initRecentlyAddedList();
        initFavoriteList();
        initRecommandList();
    }

    public void prepareAll() {
        prepareHistoryData();
        prepareMovieGenreTagData();
        prepareRecentlyAddedMovie();
        prepareFavorite();
        prepareRecommand();
    }

    /**
     * 初始化最近观看
     */
    private void initRecentlyPlayedList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvHistoryList.setLayoutManager(mLayoutManager);
        mBinding.rvHistoryList.setOnKeyPressListener(mOnKeyPressListener);
        mBinding.rvHistoryList.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 15)));
        mHistoryListAdapter = new HistoryListAdapter(getContext(), mRecentlyPlayedList);
        mBinding.rvHistoryList.setAdapter(mHistoryListAdapter);
        mHistoryListAdapter.setOnItemClickListener(new BaseApater2.OnRecyclerViewItemActionListener<HistoryMovieDataView>() {
            @Override
            public void onItemClick(View view, int postion, HistoryMovieDataView data) {
                mViewModel.playingVideo(data.path, data.filename, list -> {
                    mHistoryListAdapter.addAll(list);
                });
            }

            @Override
            public void onItemFocus(View view, int postion, HistoryMovieDataView data) {

            }
        });
    }

    /**
     * 初始化电影类型分类列表
     */
    private void initGenreList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvGenreList.setLayoutManager(mLayoutManager);
        mBinding.rvGenreList.setOnKeyPressListener(mOnKeyPressListener);
        mBinding.rvGenreList.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 12), DensityUtil.dip2px(getContext(), 12)));
        mGenreTagAdapter = new GenreTagAdapter(getContext(), mGenreTagList);
        mBinding.rvGenreList.setAdapter(mGenreTagAdapter);
        mGenreTagAdapter.setOnGenreListener(mGenreListener);
        mGenreTagAdapter.setOnItemClickListener(mGenreItemClickListener);
        mViewModel.setGenreCallback(list -> mGenreTagAdapter.addAll(list));
    }

    /**
     * 初始化最新添加列表
     */
    private void initRecentlyAddedList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvRecentlyAdded.setLayoutManager(mLayoutManager);
        mBinding.rvRecentlyAdded.setOnKeyPressListener(mOnKeyPressListener);
        mBinding.rvRecentlyAdded.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 30)));
        mRecentlyAddListAdapter = new NewMovieItemListAdapter(getContext(), mRecentlyAddedList);
        mRecentlyAddListAdapter.setOnItemClickListener(mMovieDataViewEventListener);
        mBinding.rvRecentlyAdded.setAdapter(mRecentlyAddListAdapter);
//        mRecentlyAddListAdapter.setOnItemLongClickListener(mPosterItemLongClickListener);

    }

    /**
     * 初始化我的收藏
     */
    private void initFavoriteList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvFavorite.setLayoutManager(mLayoutManager);
        mBinding.rvFavorite.setOnKeyPressListener(mOnKeyPressListener);
        mBinding.rvFavorite.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 30)));
        mFavoriteListAdapter = new NewMovieItemListAdapter(getContext(), mFavoriteList);
        mBinding.rvFavorite.setAdapter(mFavoriteListAdapter);
        mFavoriteListAdapter.setOnItemClickListener(mMovieDataViewEventListener);
//        mFavoriteListAdapter.setOnItemLongClickListener(mPosterItemLongClickListener);

    }

    private void initRecommandList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvRecommand.setLayoutManager(mLayoutManager);
        mBinding.rvRecommand.setOnKeyPressListener(mOnKeyPressListener);
        mBinding.rvRecommand.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 30)));
        mRecommandListAdapter = new NewMovieItemListAdapter(getContext(), mRecommandList);
        mBinding.rvRecommand.setAdapter(mRecommandListAdapter);
        mRecommandListAdapter.setOnItemClickListener(mMovieDataViewEventListener);
//        mRecommandListAdapter.setOnItemLongClickListener(mPosterItemLongClickListener);

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
        mViewModel.prepareGenreList();
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

    public void prepareFavorite() {
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

    @Override
    public void forceRefresh() {
        prepareAll();
    }

}