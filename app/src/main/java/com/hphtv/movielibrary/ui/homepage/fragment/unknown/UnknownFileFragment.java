package com.hphtv.movielibrary.ui.homepage.fragment.unknown;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.FocusFinder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseAdapter2;
import com.hphtv.movielibrary.adapter.UnknownRootItemListAdapter;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.data.pagination.BasePaginationCallback;
import com.hphtv.movielibrary.data.pagination.PaginationCallback;
import com.hphtv.movielibrary.databinding.ActivityNewHomepageBinding;
import com.hphtv.movielibrary.databinding.FragmentUnknowfileBinding;
import com.hphtv.movielibrary.effect.FilterGridLayoutManager;
import com.hphtv.movielibrary.effect.GridSpacingItemDecorationVertical;
import com.hphtv.movielibrary.listener.OnMovieLoadListener;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnknownRootDataView;
import com.hphtv.movielibrary.ui.detail.MovieDetailActivity;
import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment;
import com.hphtv.movielibrary.ui.ILoadingState;
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;
import com.hphtv.movielibrary.util.ActivityHelper;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.PackageTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/4/2
 */
public class UnknownFileFragment extends BaseAutofitHeightFragment<UnknownFileViewModel, FragmentUnknowfileBinding> implements ILoadingState {
    private UnknownRootItemListAdapter mUnknownRootItemListAdapter;
    private PlayVideoReceiver mPlayVideoReceiver;
    public AtomicInteger atomicState = new AtomicInteger();
    private Handler mUIHandler = new Handler();
    private BaseAdapter2.OnRecyclerViewItemClickListener mActionListener = (BaseAdapter2.OnRecyclerViewItemClickListener<UnknownRootDataView>) (view, position, data) -> {
        switch (data.type) {
            case FILE:
                playVideo(data.root);
                break;
            case FOLDER:
                reloadUnknownFiles(data.root);
                break;
            case BACK:
                OnBackPress();
                break;
        }

    };

    public UnknownFileFragment() {
        super();
    }

    public static UnknownFileFragment newInstance(NoScrollAutofitHeightViewPager viewPager, int position) {
        Bundle args = new Bundle();
        UnknownFileFragment fragment = new UnknownFileFragment();
        args.putInt(BaseAutofitHeightFragment.POSITION, position);
        fragment.setArguments(args);
        fragment.setAutoFitHeightViewPager(viewPager);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reloadUnknownFiles(mViewModel.getCurrentPath());
    }

    @Override
    protected UnknownFileViewModel createViewModel() {
        return null;
    }

    @Override
    public void forceRefresh() {
        reloadUnknownFiles(UnknownFileViewModel.ROOT);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterPlayReceiver();
    }

    /**
     * 初始化UI
     */
    private void initViews() {
        mUnknownRootItemListAdapter = new UnknownRootItemListAdapter(getContext(), mViewModel.getUnknownRootDataViews());
        FilterGridLayoutManager gridLayoutManager = new FilterGridLayoutManager(getContext(), 6, GridLayoutManager.VERTICAL, false);
        mBinding.rvUnknowsfile.setLayoutManager(gridLayoutManager);
        mBinding.rvUnknowsfile.setAdapter(mUnknownRootItemListAdapter);
        mBinding.rvUnknowsfile.addOnScrollListener(new OnMovieLoadListener() {
            @Override
            protected void onLoadingNext(int countItem, int lastItem) {
                if (mViewModel != null)
                    mViewModel.loadMoreUnknownRoots(new BasePaginationCallback<UnknownRootDataView>() {
                        @Override
                        public void onResult(@NonNull List<? extends UnknownRootDataView> roots) {
                            mUnknownRootItemListAdapter.appendAll(roots);
                        }
                    });
            }
        });
        mBinding.rvUnknowsfile.addItemDecoration(new GridSpacingItemDecorationVertical(
                getResources().getDimensionPixelSize(R.dimen.unknown_root_width),
                DensityUtil.dip2px(getContext(), 43),
                DensityUtil.dip2px(getContext(), 30),
                DensityUtil.dip2px(getContext(), 40),
                DensityUtil.dip2px(getContext(), 46),
                6)
        );
        mBinding.rvUnknowsfile.setOnBackPressListener(() -> {
            ActivityNewHomepageBinding binding = getBaseActivity().getBinding();
            int pos = binding.tabLayout.getSelectedTabPosition();
            binding.tabLayout.getTabAt(pos).view.requestFocus();
        });
        mUnknownRootItemListAdapter.setOnItemClickListener(mActionListener);
        mUnknownRootItemListAdapter.setOnItemLongClickListener(this::showUnknownsFileMenuDialog);
    }

    /**
     * 显示菜单
     *
     * @param view
     * @param position
     * @param data
     * @return
     */
    private boolean showUnknownsFileMenuDialog(View view, int position, UnknownRootDataView data) {
        switch (data.type) {
            case FOLDER:
            case FILE:
                ActivityHelper.showUnknownsFileMenuDialog(getChildFragmentManager(), position, data);
                break;
            case BACK:
                break;
        }
        return true;
    }

    private void reloadUnknownFiles(String root) {
        if (mViewModel != null) {
            if (mViewModel.isPlayingVideo()) {
                mViewModel.setPlayingVideo(false);
                return;
            }

            mViewModel.reloadUnknownRoots(root, new BasePaginationCallback<UnknownRootDataView>() {
                @Override
                public void onResult(@NonNull List<? extends UnknownRootDataView> roots) {
                    if (roots.size() > 0) {
                        mBinding.setIsEmpty(false);
                        mUnknownRootItemListAdapter.addAll(roots);
                    } else {
                        mBinding.setIsEmpty(true);
                        mUnknownRootItemListAdapter.clearAll();
                    }
                }
            });
        }
    }

    public void playVideo(String path) {
        if (PackageTools.getPackageVersionCode(getContext(), Config.SYSTEM_PLAYER_PACKAGE) >= 40) {
            registerPlayReceiver();
            mViewModel.playVideo(path)
                    .subscribe(new SimpleObserver<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            mViewModel.setPlayingVideo(true);
                            super.onSubscribe(d);
                            getBaseActivity().startLoading();
                        }

                        @Override
                        public void onAction(String s) {
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            getBaseActivity().stopLoading();
                        }
                    });
        } else {
            mViewModel.playVideo(path)
                    .flatMap((Function<String, ObservableSource<String>>) MovieHelper::updateHistory)
                    .subscribe(new SimpleObserver<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            mViewModel.setPlayingVideo(true);
                            super.onSubscribe(d);
                            getBaseActivity().startLoading();
                        }

                        @Override
                        public void onAction(String path) {
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            getBaseActivity().stopLoading();
                        }
                    });
        }


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

    public boolean OnBackPress() {
        String root = mViewModel.pop();
        if (UnknownFileViewModel.ROOT.equals(root)) {
            reloadUnknownFiles(root);
            return true;
        }
        return false;
    }


    public void refreshCurrentPage(int pos) {
        View focusView = mBinding.rvUnknowsfile.findFocus();
        int curFocusIndex = 0;
        if (focusView != null) {
            curFocusIndex = mBinding.rvUnknowsfile.indexOfChild(focusView);
            if (curFocusIndex == mViewModel.getUnknownRootDataViews().size() - 1)
                curFocusIndex--;
        }
        final int lastCurFocusIndex = curFocusIndex;

        mViewModel.reloadUnknownRoots(mViewModel.getCurrentPath(),new BasePaginationCallback<UnknownRootDataView>() {

            @Override
            public void onResult(@NonNull List<? extends UnknownRootDataView> roots) {
                if (roots.size() > 0) {
                    mBinding.setIsEmpty(false);
                    mUnknownRootItemListAdapter.addAll(roots);
                } else {
                    mBinding.setIsEmpty(true);
                    mUnknownRootItemListAdapter.clearAll();
                }
                mUIHandler.postDelayed(() -> {
                    View newFocus = mBinding.rvUnknowsfile.getChildAt(lastCurFocusIndex);
                    if (newFocus != null)
                        newFocus.requestFocus();
                }, 200);
            }
        });
    }


    private void registerPlayReceiver() {
        try {
            unregisterPlayReceiver();
            if (mPlayVideoReceiver == null)
                mPlayVideoReceiver = new PlayVideoReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.firefly.video.player");
            getBaseActivity().registerReceiver(mPlayVideoReceiver, intentFilter);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void unregisterPlayReceiver() {
        try {
            if (mPlayVideoReceiver != null) {
                getBaseActivity().unregisterReceiver(mPlayVideoReceiver);
                mPlayVideoReceiver = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

                            }
                        });
            }
            unregisterPlayReceiver();

        }
    }
}
