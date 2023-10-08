package com.hphtv.movielibrary.ui.pagination;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseAdapter2;
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.databinding.ActivityPaginationBinding;
import com.hphtv.movielibrary.effect.FilterGridLayoutManager;
import com.hphtv.movielibrary.effect.GridSpacingItemDecorationVertical;
import com.hphtv.movielibrary.effect.GridSpacingItemDecorationVertical2;
import com.hphtv.movielibrary.listener.OnMovieChangeListener;
import com.hphtv.movielibrary.listener.OnMovieLoadListener;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.homepage.fragment.homepage.HomeFragmentViewModel;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;
import com.hphtv.movielibrary.util.ActivityHelper;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function3;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/4/22
 */
public class PaginationActivity extends AppBaseActivity<PaginationViewModel, ActivityPaginationBinding> implements OnMovieChangeListener, DialogInterface.OnCancelListener {
    public static final String EXTRA_PAGE_TYPE = "extra_page_type";
    public static final String EXTRA_VIDEO_TAG = "extra_video_tag";
    private NewMovieItemListAdapter mMovieItemListAdapter;
    private HomeFragmentViewModel mNewpageViewModel;
    private Handler mHandler = new Handler();
    private Runnable mBottomMaskFadeInTask;

    BaseAdapter2.OnRecyclerViewItemClickListener<MovieDataView> mActionListener = new BaseAdapter2.OnRecyclerViewItemClickListener<MovieDataView>() {
        @Override
        public void onItemClick(View view, int position, MovieDataView data) {
            mNewpageViewModel.startDetailActivity(PaginationActivity.this, data);
        }
    };

    PaginationViewModel.OnRefresh mOnRefresh = new PaginationViewModel.OnRefresh() {
        @Override
        public void beforeLoad() {
            startLoading();
        }

        @Override
        public void newSearch(List<MovieDataView> newMovieDataView) {
            mMovieItemListAdapter.addAll(newMovieDataView);
            if (mMovieItemListAdapter.getItemCount() <= 5)
                mBinding.bottomMask.setAlpha(0);
            else
                mBinding.bottomMask.setAlpha(1);
            stopLoading();
        }

        @Override
        public void appendMovieDataViews(List<MovieDataView> movieDataViews) {
            if (movieDataViews.size() > 0)
                mMovieItemListAdapter.appendAll(movieDataViews);
        }
    };

    TvRecyclerView.OnBackPressListener mOnBackPressListener = () -> mBinding.tvTitle.requestFocus();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel.setOnRefresh(mOnRefresh);
        mNewpageViewModel = new ViewModelProvider(this).get(HomeFragmentViewModel.class);
        initView();
        bindData();
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int type = getIntent().getIntExtra(EXTRA_PAGE_TYPE, -1);
        mViewModel.setType(type);
        if (!TextUtils.isEmpty(getIntent().getStringExtra(EXTRA_VIDEO_TAG))) {
            String video_tag = getIntent().getStringExtra(EXTRA_VIDEO_TAG);
            mViewModel.setVideoTag(video_tag);
        } else {
            mViewModel.setVideoTag(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        forceStopLoading();
        mViewModel.cancel();
    }

    private void initView() {
        super.setLoadingCancelable(true);
        super.setLoadingCancelListener(this);
        mBinding.tvTitle.setOnClickListener(v -> finish());
        FilterGridLayoutManager gridLayoutManager = new FilterGridLayoutManager(this, 1920, GridLayoutManager.VERTICAL, false);
        mBinding.recyclerview.setLayoutManager(gridLayoutManager);

        mMovieItemListAdapter = new NewMovieItemListAdapter(this, new ArrayList<>());
        mMovieItemListAdapter.setZoomRatio(1.2088888f);

        mMovieItemListAdapter.setOnItemClickListener(mActionListener);
        mMovieItemListAdapter.setOnItemLongClickListener((view, position, data) -> {
            if (!data.is_user_fav)
                ActivityHelper.showPosterMenuDialog(getSupportFragmentManager(), position, data);
            return true;
        });

        mBinding.recyclerview.addItemDecoration(new GridSpacingItemDecorationVertical2(
                getResources().getDimensionPixelSize(R.dimen.poster_item_1_w),
                DensityUtil.dip2px(getBaseContext(), 47),
                /* edgeSpacing = */ 80,
                /* rowSpacing = */  DensityUtil.dip2px(getBaseContext(), 65),
                /* columnSpacing = */ 16,
                /* spanCount = */ 5)
        );

        GridLayoutManager.SpanSizeLookup lookup = new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position % 5 == 0) {
                    return 429;
                } else if (position % 5 == 4) {
                    return 428;
                } else {
                    return 354;
                }
            }
        };
        gridLayoutManager.setSpanSizeLookup(lookup);
        mBinding.recyclerview.setAdapter(mMovieItemListAdapter);
        mBinding.recyclerview.addOnScrollListener(new OnMovieLoadListener() {
            @Override
            protected void onLoadingNext(int countItem, int lastItem) {
                mViewModel.loadMore();
            }

            @Override
            protected void onScrollStart() {
                super.onScrollStart();
                mHandler.removeCallbacks(mBottomMaskFadeInTask);
                if (mBinding.bottomMask.getAlpha() > 0) {
                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mBinding.bottomMask, "alpha", mBinding.bottomMask.getAlpha(), 0).setDuration(200);
                    objectAnimator.start();
                }
            }

            @Override
            protected void onScrolledEnd() {
                super.onScrolledEnd();
                mHandler.removeCallbacks(mBottomMaskFadeInTask);
                mBottomMaskFadeInTask = () -> {
                    ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mBinding.bottomMask, "alpha", mBinding.bottomMask.getAlpha(), 1).setDuration(500);
                    objectAnimator.start();
                };
                mHandler.postDelayed(mBottomMaskFadeInTask, 800);
            }
        });
        mBinding.recyclerview.setOnBackPressListener(mOnBackPressListener);
    }

    private void bindData() {
        mBinding.setTitle(mViewModel.getTitle());
    }


    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        super.onActivityResultCallback(result);
        if (result.getResultCode() == RESULT_OK) {
            setResult(RESULT_OK);
            mViewModel.reload();
        }
    }


    @Override
    public void OnRematchPoster(MovieDataView movieDataView, int pos) {
        mMovieItemListAdapter.replace(movieDataView, pos);
    }

    @Override
    public void OnMovieChange(MovieDataView movieDataView, int pos) {
        if (mViewModel.getType() == PaginationViewModel.OPEN_FAVORITE) {
            if (movieDataView.is_favorite) {
                mMovieItemListAdapter.insert(movieDataView, pos);
            } else {
                mMovieItemListAdapter.remove(movieDataView);
            }
        } else {
            mMovieItemListAdapter.replace(movieDataView, pos);
        }
    }

    @Override
    public void OnMovieRemove(String movie_id, String type, int pos) {
        mMovieItemListAdapter.remove(movie_id, type, pos);
    }

    @Override
    public void OnMovieInsert(MovieDataView movieDataView, int pos) {
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }
}
