package com.hphtv.movielibrary.ui.homepage.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseAdapter2;
import com.hphtv.movielibrary.adapter.GenreTagAdapter;
import com.hphtv.movielibrary.adapter.HistoryListAdapter;
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.adapter.NewMovieItemWithMoreListAdapter;
import com.hphtv.movielibrary.bean.PlayList;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FragmentHomepageBinding;
import com.hphtv.movielibrary.effect.SpacingItemDecoration;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.ILoadingState;
import com.hphtv.movielibrary.ui.detail.MovieDetailActivity;
import com.hphtv.movielibrary.ui.filterpage.FilterPageActivity;
import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment;
import com.hphtv.movielibrary.ui.homepage.genretag.AddGenreDialogFragment;
import com.hphtv.movielibrary.ui.homepage.genretag.IRefreshGenre;
import com.hphtv.movielibrary.ui.pagination.PaginationActivity;
import com.hphtv.movielibrary.ui.pagination.PaginationViewModel;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;
import com.hphtv.movielibrary.util.ActivityHelper;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;
import com.station.kit.util.PackageTools;
import com.station.kit.util.ToastUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;

/**
 * author: Sam Leung
 * date:  2022/10/26
 */
public abstract class BaseHomeFragment<VM extends BaseHomePageViewModel> extends BaseAutofitHeightFragment<VM, FragmentHomepageBinding> implements IRefreshGenre, ILoadingState {
    private HistoryListAdapter mHistoryListAdapter;
    private GenreTagAdapter mGenreTagAdapter;
    private NewMovieItemWithMoreListAdapter mRecentlyAddListAdapter;
    private NewMovieItemWithMoreListAdapter mFavoriteListAdapter;
    private NewMovieItemListAdapter mRecommendListAdapter;
    public AtomicInteger atomicState = new AtomicInteger();
    private PlayVideoReceiver mPlayVideoReceiver;

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
            intent.putExtra(FilterPageActivity.EXTRA_VIDEO_TYPE, getVideoTagName());
            startActivityForResult(intent);
        }
    };

    //动态主题动作监听
    private BaseAdapter2.OnRecyclerViewItemClickListener mGenreItemClickListener = (view, postion, data) -> {
        Intent intent = new Intent(getContext(), FilterPageActivity.class);
        intent.putExtra(FilterPageActivity.EXTRA_GENRE, data.toString());
        intent.putExtra(FilterPageActivity.EXTRA_VIDEO_TYPE, getVideoTagName());
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
        intent.putExtra(PaginationActivity.EXTRA_VIDEO_TAG, getVideoTagName());
        startActivityForResult(intent);
    };

    //TvRecyclerView的按键处理(按键均被监听)
    private TvRecyclerView.OnBackPressListener mOnBackPressListener = () -> getActivity().finish();

    protected abstract String getVideoTagName();

    public BaseHomeFragment(String tag) {
        super(tag);
    }

    @Override
    protected VM createViewModel() {
        return null;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        prepareAll();
    }

    private void initViews() {
        mBinding.btnQuickAddShortcut.setOnClickListener(getBaseActivity()::startShortcutManager);
        initRecentlyPlayedList();
        initGenreList();
        initRecentlyAddedList();
        initFavoriteList();
        initRecommendList();
    }

    public void prepareAll() {
        prepareHistoryData();
        prepareMovieGenreTagData();
        prepareRecentlyAddedMovie();
        prepareFavorite();
    }

    /**
     * 初始化最近观看
     */
    private void initRecentlyPlayedList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvHistoryList.setLayoutManager(mLayoutManager);
        mBinding.rvHistoryList.setOnBackPressListener(mOnBackPressListener);
        mBinding.rvHistoryList.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 15)));
        mHistoryListAdapter = new HistoryListAdapter(getContext(), mViewModel.getRecentlyPlayedList());
        mBinding.rvHistoryList.setAdapter(mHistoryListAdapter);
        mHistoryListAdapter.setOnItemClickListener((view, position, data) -> {
            if (data._mid < 0 || Constants.RecentlyVideoAction.playNow
                    .equals(Constants.RecentlyVideoAction.valueOf(Config.getRecentlyVideoAction()))) {

                if (PackageTools.getPackageVersionCode(getContext(), Config.SYSTEM_PLAYER_PACKAGE) >= 40) {
                    registerPlayReceiver();
                    mViewModel.playingVideo(data.path, data.filename)
                            .subscribe(new SimpleObserver<PlayList>() {
                                @Override
                                public void onAction(PlayList playList) {
                                }
                            });
                } else {
                    mViewModel.playingVideo(data.path, data.filename)
                            .flatMap((Function<PlayList, ObservableSource<String>>) playList -> MovieHelper.updateHistory(data.path))
                            .subscribe(new SimpleObserver<String>() {
                                @Override
                                public void onAction(String s) {
                                    prepareHistoryData();
                                }
                            });
                }
            } else {
                Intent intent = new Intent(getContext(), MovieDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong(Constants.Extras.MOVIE_ID, data._mid);
                bundle.putInt(Constants.Extras.SEASON, data.season);
                intent.putExtras(bundle);
                startActivityForResult(intent);
            }

        });
    }

    /**
     * 初始化电影类型分类列表
     */
    private void initGenreList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvGenreList.setLayoutManager(mLayoutManager);
        mBinding.rvGenreList.setOnBackPressListener(mOnBackPressListener);
        mBinding.rvGenreList.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 12), DensityUtil.dip2px(getContext(), 12)));
        mGenreTagAdapter = new GenreTagAdapter(getContext(), mViewModel.getGenreTagList());
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
        mRecentlyAddListAdapter = new NewMovieItemWithMoreListAdapter(getContext(), mViewModel.getRecentlyAddedList(), PaginationViewModel.OPEN_RECENTLY_ADD);
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
        mFavoriteListAdapter = new NewMovieItemWithMoreListAdapter(getContext(), mViewModel.getFavoriteList(), PaginationViewModel.OPEN_FAVORITE);
        mBinding.rvFavorite.setAdapter(mFavoriteListAdapter);
        mFavoriteListAdapter.setOnItemClickListener(mMovieDataViewEventListener);
        mFavoriteListAdapter.setOnMoreItemClickListener(mOnMoreItemClickListener);

//        mFavoriteListAdapter.setOnItemLongClickListener(mPosterItemLongClickListener);

    }

    private void initRecommendList() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.rvRecommand.setLayoutManager(mLayoutManager);
        mBinding.rvRecommand.setOnBackPressListener(mOnBackPressListener);
        mBinding.rvRecommand.addItemDecoration(new SpacingItemDecoration(DensityUtil.dip2px(getContext(), 72), DensityUtil.dip2px(getContext(), 15), DensityUtil.dip2px(getContext(), 30)));
        mRecommendListAdapter = new NewMovieItemListAdapter(getContext(), mViewModel.getRecommendList());
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
                        updateRecentlyPlayed(historyMovieDataViews);
                        prepareRecommand();
                    }
                });
    }

    private void updateRecentlyPlayed(List<HistoryMovieDataView> historyList) {
        if (historyList.size() > 0) {
            mBinding.setHasHistory(true);
            mBinding.rvHistoryList.setVisibility(View.VISIBLE);
            mBinding.tvHistoryEmptyTips.setVisibility(View.GONE);
        } else {
            mBinding.setHasHistory(false);
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

    private void prepareRecommand() {
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
        if (mViewModel != null)
            prepareAll();
    }

    @Override
    public void remoteUpdateFavorite(String movie_id, String type, boolean isFavorite) {
        if (mViewModel != null) {
            mViewModel.getUpdatingFavorite(movie_id, type)
                    .subscribe(new SimpleObserver<MovieDataView>() {
                        @Override
                        public void onAction(MovieDataView movieDataView) {
                            if (isFavorite) {
                                if (!mFavoriteListAdapter.getDatas().contains(movieDataView)) {
                                    mFavoriteListAdapter.add(movieDataView);
                                    mBinding.setFavorite(true);
                                }
                            } else {
                                mFavoriteListAdapter.remove(movieDataView);
                                if (mFavoriteListAdapter.getItemCount() == 0) {
                                    mBinding.setFavorite(false);
                                }
                            }
                            if (mRecentlyAddListAdapter.getDatas().contains(movieDataView)) {
                                mRecentlyAddListAdapter.updateStatus(movieDataView);
                            }
                            if (mRecommendListAdapter.getDatas().contains(movieDataView)) {
                                mRecommendListAdapter.updateStatus(movieDataView);
                            }
                            ToastUtil.newInstance(getContext()).toast(getString(R.string.remote_movie_sync_tips));

                        }
                    });
        }
    }

    @Override
    public void remoteUpdateMovie(long o_id, long n_id) {
        if (mViewModel != null) {
            prepareAll();
            ToastUtil.newInstance(getContext()).toast(getString(R.string.remote_movie_sync_tips));

        }
    }

    @Override
    public void remoteRemoveMovie(String movie_id, String type) {
        if (mViewModel != null) {
            prepareAll();
            ToastUtil.newInstance(getContext()).toast(getString(R.string.remote_remove_movie_sync_tips));
        }
    }


    @Override
    public void refreshGenreUI() {
        prepareMovieGenreTagData();
    }

    @Override
    public void updateUserFavorite() {

    }

    @Override
    public void startLoading() {
        int i = atomicState.incrementAndGet();
        mBinding.setIsLoading(true);
    }

    @Override
    public void finishLoading() {
        int i = atomicState.decrementAndGet();
        if (i <= 0) {
            mBinding.setIsLoading(false);
            atomicState.set(0);
        }
    }

    private void registerPlayReceiver() {
        try {
            unregisterPlayReceiver();
            if (mPlayVideoReceiver == null)
                mPlayVideoReceiver = new PlayVideoReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.firefly.video.player");
            getContext().registerReceiver(mPlayVideoReceiver, intentFilter);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void unregisterPlayReceiver() {
        try {
            if (mPlayVideoReceiver != null) {
                getContext().unregisterReceiver(mPlayVideoReceiver);
                mPlayVideoReceiver = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterPlayReceiver();
    }

    private class PlayVideoReceiver extends BroadcastReceiver {
        public static final String ACTION_PLAYER_CALLBACK = "com.firefly.video.player";
        public static final String EXTRA_PATH = "video_address";
        public static final String EXTRA_POSITION = "video_position";
        public static final String EXTRA_DURATION = "video_duration";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_PLAYER_CALLBACK.equals(action)) {
                String path = null;
                long position = 0;
                long duration = 0;
                if (intent.hasExtra(EXTRA_PATH)) {
                    path = intent.getStringExtra(EXTRA_PATH);
                }
                if (intent.hasExtra(EXTRA_POSITION)) {
                    position = intent.getLongExtra(EXTRA_POSITION, 0);
                }
                if (intent.hasExtra(EXTRA_DURATION)) {
                    duration = intent.getLongExtra(EXTRA_DURATION, 0);
                }
                Log.w(TAG, "onReceive: " + path + " " + position + "/" + duration);
                MovieHelper.updateHistory(path, position, duration)
                        .subscribe(new SimpleObserver<String>() {
                            @Override
                            public void onAction(String s) {
                                prepareHistoryData();
                            }
                        });
            }
            unregisterPlayReceiver();
        }
    }
}
