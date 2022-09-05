package com.hphtv.movielibrary.ui.filterpage;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseAdapter2;
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.databinding.ActivityFilterpageBinding;
import com.hphtv.movielibrary.effect.FilterGridLayoutManager;
import com.hphtv.movielibrary.effect.GridSpacingItemDecorationVertical;
import com.hphtv.movielibrary.listener.OnMovieChangeListener;
import com.hphtv.movielibrary.listener.OnMovieLoadListener;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.homepage.fragment.homepage.HomeFragmentViewModel;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;
import com.hphtv.movielibrary.util.ActivityHelper;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/2/22
 */
public class FilterPageActivity extends AppBaseActivity<FilterPageViewModel, ActivityFilterpageBinding> implements OnMovieChangeListener {
    public static final String EXTRA_GENRE = "extra_genre";
    public static final String EXTRA_VIDEO_TYPE = "extra_video_type";

    private NewMovieItemListAdapter mMovieItemListAdapter;
    private HomeFragmentViewModel mNewpageViewModel;
    private Handler mHandler = new Handler();
    private Runnable mBottomMaskFadeInTask;

    View.OnClickListener mOnClickListener = v -> {
        switch (v.getId()) {
            case R.id.btn_home:
            case R.id.tv_title:
                finish();
                break;
            case R.id.btn_filter:
                FilterBoxDialogFragment filterBoxDialogFragment = FilterBoxDialogFragment.newInstance();
                filterBoxDialogFragment.show(getSupportFragmentManager(), "");
                break;
        }
    };


    BaseAdapter2.OnRecyclerViewItemClickListener mActionListener = new BaseAdapter2.OnRecyclerViewItemClickListener<MovieDataView>() {
        @Override
        public void onItemClick(View view, int postion, MovieDataView data) {
            mNewpageViewModel.startDetailActivity(FilterPageActivity.this, data);
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
        bindDatas();
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String genreName = getIntent().getStringExtra(EXTRA_GENRE);
        String videoType = getIntent().getStringExtra(EXTRA_VIDEO_TYPE);
        if (!TextUtils.isEmpty(videoType)) {
            mViewModel.setGenreAndVideoTag(genreName, videoType);
        } else {
            mViewModel.setGenre(genreName);
            reloadMovieDataViews();
        }
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        startBottomMaskAnimate();
        return super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        startBottomMaskAnimate();
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        startBottomMaskAnimate();
        return super.dispatchTouchEvent(ev);
    }

    private void initView() {
        mBinding.btnHome.setOnClickListener(mOnClickListener);
        mBinding.btnFilter.setOnClickListener(mOnClickListener);
        mBinding.tvTitle.setOnClickListener(mOnClickListener);
        FilterGridLayoutManager gridLayoutManager = new FilterGridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false);
        mBinding.recyclerview.setLayoutManager(gridLayoutManager);

        mMovieItemListAdapter = new NewMovieItemListAdapter(this, new ArrayList<>());
        mMovieItemListAdapter.setOnItemClickListener(mActionListener);
        mMovieItemListAdapter.setOnItemLongClickListener((view, position, data) -> {
            ActivityHelper.showPosterMenuDialog(getSupportFragmentManager(), position, data);
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
                loadAMoreAllMovies();
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

    private void bindDatas() {
        mBinding.setEmptyType(mViewModel.getEmptyType());
        mBinding.setConditions(mViewModel.getConditionStr());
        mBinding.setRow(mViewModel.getRowStr());
        mBinding.setTotal(mViewModel.getMovieCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        super.onActivityResultCallback(result);
        if (result.getResultCode() == RESULT_OK) {
            setResult(RESULT_OK);
            reloadMovieDataViews();
        }
    }

    private void reloadMovieDataViews() {
        mViewModel.reloadMovieDataViews();
    }

    private void loadAMoreAllMovies() {
        mViewModel.loadMoiveDataViews();
    }

    FilterPageViewModel.OnRefresh mOnRefresh = new FilterPageViewModel.OnRefresh() {
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

    @Override
    public void remoteUpdateMovie(long o_id, long n_id) {
        super.remoteUpdateMovie(o_id, n_id);
        reloadMovieDataViews();
    }

    @Override
    public void remoteUpdateFavorite(String movie_id, String type, boolean isFavorite) {
        mViewModel.getUpdatingFavorite(movie_id, type)
                .subscribe(new SimpleObserver<MovieDataView>() {
                    @Override
                    public void onAction(MovieDataView movieDataView) {
                        if (mMovieItemListAdapter.getDatas().contains(movieDataView)) {
                            mMovieItemListAdapter.updateStatus(movieDataView);
                        }
                    }
                });
    }

    @Override
    public void remoteRemoveMovie(String movie_id, String type) {
        mMovieItemListAdapter.remove(movie_id, type);
    }

    @Override
    public void OnRematchPoster(MovieDataView dataView, int pos) {
        mMovieItemListAdapter.replace(dataView, pos);
        setResult(RESULT_OK);
    }

    @Override
    public void OnMovieChange(MovieDataView movieDataView, int pos) {
        mMovieItemListAdapter.replace(movieDataView, pos);
        setResult(RESULT_OK);
    }


    @Override
    public void OnMovieRemove(String movieId, String type, int pos) {
        mMovieItemListAdapter.remove(movieId, type, pos);
        mViewModel.checkEmpty(mViewModel.decreaseTotal());
        setResult(RESULT_OK);
    }

    @Override
    public void OnMovieInsert(MovieDataView movieDataView, int pos) {
        mMovieItemListAdapter.insert(movieDataView, pos);
        mViewModel.checkEmpty(mViewModel.increaseTotal());
        setResult(RESULT_OK);
    }

    /**
     * 蒙版隱藏動畫
     */
    private void startBottomMaskAnimate() {
        mHandler.removeCallbacks(mBottomMaskFadeInTask);
        if (mBinding.bottomMask.getAlpha() > 0) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mBinding.bottomMask, "alpha", mBinding.bottomMask.getAlpha(), 0).setDuration(200);
            objectAnimator.start();
        }
        mBottomMaskFadeInTask = () -> {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mBinding.bottomMask, "alpha", mBinding.bottomMask.getAlpha(), 1).setDuration(500);
            objectAnimator.start();
        };
        mHandler.postDelayed(mBottomMaskFadeInTask, 800);
    }
}
