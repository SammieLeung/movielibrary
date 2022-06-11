package com.hphtv.movielibrary.ui.homepage.fragment.theme;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.adapter.BaseAdapter2;
import com.hphtv.movielibrary.adapter.GenreTagAdapter;
import com.hphtv.movielibrary.adapter.HistoryListAdapter;
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.adapter.NewMovieItemWithMoreListAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FragmentHomepageBinding;
import com.hphtv.movielibrary.effect.SpacingItemDecoration;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.filterpage.FilterPageActivity;
import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment;
import com.hphtv.movielibrary.ui.homepage.IAutofitHeight;
import com.hphtv.movielibrary.ui.ILoadingState;
import com.hphtv.movielibrary.ui.homepage.fragment.SimpleLoadingObserver;
import com.hphtv.movielibrary.ui.homepage.genretag.AddGenreDialogFragment;
import com.hphtv.movielibrary.ui.homepage.genretag.IRefreshGenre;
import com.hphtv.movielibrary.ui.pagination.PaginationActivity;
import com.hphtv.movielibrary.ui.pagination.PaginationViewModel;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;
import com.hphtv.movielibrary.util.ActivityHelper;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * author: Sam Leung
 * date:  2022/6/7
 */
public class ThemeFragment extends BaseAutofitHeightFragment<ThemeFragmentViewModel, FragmentHomepageBinding> implements IRefreshGenre, ILoadingState {
    public static final String TAG = ThemeFragment.class.getSimpleName();

    private HistoryListAdapter mHistoryListAdapter;
    private GenreTagAdapter mGenreTagAdapter;
    private NewMovieItemWithMoreListAdapter mRecentlyAddListAdapter;
    private NewMovieItemWithMoreListAdapter mFavoriteListAdapter;
    private NewMovieItemListAdapter mRecommendListAdapter;

    private List<HistoryMovieDataView> mRecentlyPlayedList = new ArrayList<>();
    private List<String> mGenreTagList = new ArrayList<>();
    private List<MovieDataView> mRecentlyAddedList = new ArrayList<>();
    private List<MovieDataView> mFavoriteList = new ArrayList<>();
    private List<MovieDataView> mRecommandList = new ArrayList<>();

    public AtomicInteger atomicState=new AtomicInteger();

    //电影点击监听
    private BaseAdapter2.OnRecyclerViewItemClickListener<MovieDataView> mMovieDataViewEventListener = (view, postion, data) -> mViewModel.startDetailActivity((AppBaseActivity) getActivity(), data);
    //固定主题动作监听
    private GenreTagAdapter.GenreListener mGenreListener = new GenreTagAdapter.GenreListener() {
        @Override
        public void addGenre() {
            AddGenreDialogFragment fragment = AddGenreDialogFragment.newInstance();
            fragment.addAllIRefreshGenreList(getBaseActivity().getAllRefreshGenreList());
            fragment.show(getChildFragmentManager(), AddGenreDialogFragment.TAG);
        }

        @Override
        public void browseAll() {
            Intent intent = new Intent(getContext(), FilterPageActivity.class);
            intent.putExtra(FilterPageActivity.EXTRA_VIDEO_TYPE, mViewModel.getSearchType().name());
            startActivityForResult(intent);
        }
    };

    //动态主题动作监听
    private BaseAdapter2.OnRecyclerViewItemClickListener mGenreItemClickListener = (view, postion, data) -> {
        Intent intent = new Intent(getContext(), FilterPageActivity.class);
        intent.putExtra(FilterPageActivity.EXTRA_GENRE, data.toString());
        intent.putExtra(FilterPageActivity.EXTRA_VIDEO_TYPE, mViewModel.getSearchType().name());
        startActivityForResult(intent);
    };

    private BaseAdapter2.OnItemLongClickListener<MovieDataView> mPosterItemLongClickListener = (view, postion, data) -> {
        ActivityHelper.showPosterMenuDialog(getChildFragmentManager(), postion, data);
        return false;
    };

    private NewMovieItemWithMoreListAdapter.OnMoreItemClickListener mOnMoreItemClickListener = type -> {
        LogUtil.v("mOnMoreItemClickListener click " + type);
        Intent intent = new Intent(getBaseActivity(), PaginationActivity.class);
        intent.putExtra(PaginationActivity.EXTRA_PAGE_TYPE, type);
        intent.putExtra(PaginationActivity.EXTRA_VIDEO_TAG, mViewModel.getSearchType());
        startActivityForResult(intent);
    };

    //TvRecyclerView的按键处理(按键均被监听)
    private TvRecyclerView.OnBackPressListener mOnBackPressListener = () -> getActivity().finish();

    public ThemeFragment(IAutofitHeight autofitHeight, int position) {
        super(autofitHeight, position);
    }

    public static ThemeFragment newInstance(IAutofitHeight autofitHeight, int position, Constants.SearchType type) {
        Bundle args = new Bundle();
        ThemeFragment fragment = new ThemeFragment(autofitHeight, position);
        args.putSerializable("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel.setSearchType((Constants.SearchType) getArguments().getSerializable("type"));
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        Log.e(TAG, "onViewCreated " + mViewModel.getSearchType().name());

//        prepareAll();
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume " + mViewModel.getSearchType().name());

        super.onResume();
        prepareAll();
    }

    @Override
    protected ThemeFragmentViewModel createViewModel() {
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
        prepareRecommend();
    }

    /**
     * 初始化最近观看
     */
    private void initRecentlyPlayedList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvHistoryList.setLayoutManager(mLayoutManager);
        mBinding.rvHistoryList.setOnBackPressListener(mOnBackPressListener);
        mBinding.rvHistoryList.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 15)));
        mHistoryListAdapter = new HistoryListAdapter(getContext(), mRecentlyPlayedList);
        mBinding.rvHistoryList.setAdapter(mHistoryListAdapter);
        mHistoryListAdapter.setOnItemClickListener((view, position, data) -> mViewModel.playingVideo(data.path, data.filename, list -> {
            mHistoryListAdapter.addAll(list);
        }));
    }

    /**
     * 初始化电影类型分类列表
     */
    private void initGenreList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvGenreList.setLayoutManager(mLayoutManager);
        mBinding.rvGenreList.setOnBackPressListener(mOnBackPressListener);
        mBinding.rvGenreList.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 12), DensityUtil.dip2px(getContext(), 12)));
        mGenreTagAdapter = new GenreTagAdapter(getContext(), mGenreTagList);
        mBinding.rvGenreList.setAdapter(mGenreTagAdapter);
        mGenreTagAdapter.setOnGenreListener(mGenreListener);
        mGenreTagAdapter.setOnItemClickListener(mGenreItemClickListener);
    }

    /**
     * 初始化最新添加列表
     */
    private void initRecentlyAddedList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvRecentlyAdded.setLayoutManager(mLayoutManager);
        mBinding.rvRecentlyAdded.setOnBackPressListener(mOnBackPressListener);
        mBinding.rvRecentlyAdded.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 30)));
        mRecentlyAddListAdapter = new NewMovieItemWithMoreListAdapter(getContext(), mRecentlyAddedList, PaginationViewModel.OPEN_RECENTLY_ADD);
        mRecentlyAddListAdapter.setOnItemClickListener(mMovieDataViewEventListener);
        mRecentlyAddListAdapter.setOnMoreItemClickListener(mOnMoreItemClickListener);
        mBinding.rvRecentlyAdded.setAdapter(mRecentlyAddListAdapter);
//        mRecentlyAddListAdapter.setOnItemLongClickListener(mPosterItemLongClickListener);

    }

    /**
     * 初始化我的收藏
     */
    private void initFavoriteList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvFavorite.setLayoutManager(mLayoutManager);
        mBinding.rvFavorite.setOnBackPressListener(mOnBackPressListener);
        mBinding.rvFavorite.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 30)));
        mFavoriteListAdapter = new NewMovieItemWithMoreListAdapter(getContext(), mFavoriteList, PaginationViewModel.OPEN_FAVORITE);
        mBinding.rvFavorite.setAdapter(mFavoriteListAdapter);
        mFavoriteListAdapter.setOnItemClickListener(mMovieDataViewEventListener);
        mFavoriteListAdapter.setOnMoreItemClickListener(mOnMoreItemClickListener);

//        mFavoriteListAdapter.setOnItemLongClickListener(mPosterItemLongClickListener);

    }

    private void initRecommandList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvRecommand.setLayoutManager(mLayoutManager);
        mBinding.rvRecommand.setOnBackPressListener(mOnBackPressListener);
        mBinding.rvRecommand.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 30)));
        mRecommendListAdapter = new NewMovieItemListAdapter(getContext(), mRecommandList);
        mBinding.rvRecommand.setAdapter(mRecommendListAdapter);
        mRecommendListAdapter.setOnItemClickListener(mMovieDataViewEventListener);

//        mRecommandListAdapter.setOnItemLongClickListener(mPosterItemLongClickListener);

    }

    /**
     * 读取历史记录数据
     */
    public void prepareHistoryData() {
        mViewModel.prepareHistory()
                .subscribe(new SimpleLoadingObserver<List<HistoryMovieDataView>>(this) {
                    @Override
                    public void onAction(List<HistoryMovieDataView> historyMovieDataViews) {
                        updateRecentlyPlayed((List<HistoryMovieDataView>) historyMovieDataViews);
                    }
                });
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
        if (mViewModel != null)
            mViewModel.prepareGenreList()
                    .subscribe(new SimpleLoadingObserver<List<String>>(this) {
                        @Override
                        public void onAction(List<String> list) {
                            mGenreTagAdapter.addAll(list);
                        }
                    });
    }

    private void prepareRecentlyAddedMovie() {
        mViewModel.prepareRecentlyAddedMovie()
                .subscribe(new SimpleLoadingObserver<List<MovieDataView>>(this) {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViews) {
                        if (movieDataViews.size() > 0) {
                            mRecentlyAddListAdapter.addAll(movieDataViews);
                            mBinding.setRecentAdd(true);
                        } else {
                            mBinding.setRecentAdd(false);
                        }
                    }
                });
    }

    public void prepareFavorite() {
        mViewModel.prepareFavorite()
                .subscribe(new SimpleLoadingObserver<List<MovieDataView>>(this) {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViews) {
                        if (movieDataViews.size() > 0) {
                            mFavoriteListAdapter.addAll(movieDataViews);
                            mBinding.setFavorite(true);
                        } else {
                            mBinding.setFavorite(false);
                        }
                    }
                });
    }

    private void prepareRecommend() {
        mViewModel.prepareRecommend()
                .subscribe(new SimpleLoadingObserver<List<MovieDataView>>(this) {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViews) {
                        if (movieDataViews.size() > 0) {
                            mRecommendListAdapter.addAll(movieDataViews);
                            mBinding.setRecommand(true);
                        } else {
                            mBinding.setRecommand(false);
                        }
                    }
                });
    }

    @Override
    public void forceRefresh() {
        Log.e(TAG, "forceRefresh ");
        if (mViewModel != null)
            prepareAll();
    }

    @Override
    public void refreshGenreUI() {
        prepareMovieGenreTagData();
    }

    @Override
    public void startLoading() {
        int i = atomicState.incrementAndGet();
        LogUtil.v(TAG, "start-Loading " + i);
        mBinding.setIsLoading(true);
    }

    @Override
    public void finishLoading() {
        int i=atomicState.decrementAndGet();
        LogUtil.v(TAG, "stop-Loading "+i);
        if(i<=0) {
            LogUtil.v(TAG, "finishLoading ");
            mBinding.setIsLoading(false);
            atomicState.set(0);
        }
    }
}
