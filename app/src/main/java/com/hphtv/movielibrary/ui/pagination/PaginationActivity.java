package com.hphtv.movielibrary.ui.pagination;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseAdapter2;
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.ActivityPaginationBinding;
import com.hphtv.movielibrary.effect.FilterGridLayoutManager;
import com.hphtv.movielibrary.effect.GridSpacingItemDecorationVertical;
import com.hphtv.movielibrary.listener.OnMovieChangeListener;
import com.hphtv.movielibrary.listener.OnMovieLoadListener;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.homepage.fragment.homepage.HomeFragmentViewModel;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;
import com.hphtv.movielibrary.util.ActivityHelper;
import com.station.kit.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/4/22
 */
public class PaginationActivity extends AppBaseActivity<PaginationViewModel, ActivityPaginationBinding> implements OnMovieChangeListener {
    public static final String EXTRA_PAGE_TYPE = "extra_page_type";
    public static final String EXTRA_VIDEO_TAG = "extra_video_tag";
    private NewMovieItemListAdapter mMovieItemListAdapter;
    private HomeFragmentViewModel mNewpageViewModel;
    private Handler mHandler = new Handler();
    private Runnable mBottomMaskFadeInTask;

    BaseAdapter2.OnRecyclerViewItemClickListener mActionListener = new BaseAdapter2.OnRecyclerViewItemClickListener<MovieDataView>() {
        @Override
        public void onItemClick(View view, int position, MovieDataView data) {
            mNewpageViewModel.startDetailActivity(PaginationActivity.this, data);
        }
    };

    PaginationViewModel.OnRefresh mOnRefresh = new PaginationViewModel.OnRefresh() {
        @Override
        public void newSearch(List<MovieDataView> newMovieDataView) {
            mMovieItemListAdapter.addAll(newMovieDataView);
            if (mMovieItemListAdapter.getItemCount() <= 5)
                mBinding.bottomMask.setAlpha(0);
            else
                mBinding.bottomMask.setAlpha(1);
        }

        @Override
        public void appendMovieDataViews(List<MovieDataView> movieDataViews) {
            if (movieDataViews.size() > 0)
                mMovieItemListAdapter.appendAll(movieDataViews);
        }
    };

    TvRecyclerView.OnBackPressListener mOnBackPressListener = new TvRecyclerView.OnBackPressListener() {

        @Override
        public void onBackPress() {
            mBinding.tvTitle.requestFocus();
        }
    };

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
        if (getIntent().getSerializableExtra(EXTRA_VIDEO_TAG) != null) {
            Constants.SearchType searchType = (Constants.SearchType) getIntent().getSerializableExtra(EXTRA_VIDEO_TAG);
            mViewModel.setSearchType(searchType);
        }
        reload();
    }

    private void initView() {
        mBinding.tvTitle.setOnClickListener(v -> finish());
        FilterGridLayoutManager gridLayoutManager = new FilterGridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false);
        mBinding.recyclerview.setLayoutManager(gridLayoutManager);
        mMovieItemListAdapter = new NewMovieItemListAdapter(this, new ArrayList<>());
        mMovieItemListAdapter.setOnItemClickListener(mActionListener);
        mMovieItemListAdapter.setOnItemLongClickListener((view, postion, data) -> {
            ActivityHelper.showPosterMenuDialog(getSupportFragmentManager(), postion, data);
            return true;
        });

        mBinding.recyclerview.addItemDecoration(new GridSpacingItemDecorationVertical(
                getResources().getDimensionPixelSize(R.dimen.poster_item_1_w),
                DensityUtil.dip2px(this, 43),
                DensityUtil.dip2px(this, 27),
                DensityUtil.dip2px(this, 40),
                DensityUtil.dip2px(this, 28),
                5)
        );
        mBinding.recyclerview.setAdapter(mMovieItemListAdapter);
        mBinding.recyclerview.addOnScrollListener(new OnMovieLoadListener() {
            @Override
            protected void onLoading(int countItem, int lastItem) {
                loadMore();
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

    private void reload() {
        mViewModel.reload();
    }

    private void loadMore() {
        mViewModel.loadMore();
    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        super.onActivityResultCallback(result);
        if (result.getResultCode() == RESULT_OK) {
            setResult(RESULT_OK);
            reload();
        }
    }

    @Override
    public void OnRematchPoster(MovieDataView movieDataView, int pos) {
        mMovieItemListAdapter.replace(movieDataView, pos);
    }

    @Override
    public void OnMovieChange(MovieDataView movieDataView, int pos) {

    }

    @Override
    public void OnMovieRemove(String movie_id, String type, int pos) {

    }

    @Override
    public void OnMovieInsert(MovieDataView movieDataView, int pos) {

    }
}
