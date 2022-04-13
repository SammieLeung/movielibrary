package com.hphtv.movielibrary.ui.filterpage;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseApater2;
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
import com.station.kit.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/2/22
 */
public class FilterPageActivity extends AppBaseActivity<FilterPageViewModel, ActivityFilterpageBinding> implements OnMovieChangeListener {
    public static final String EXTRA_GENRE = "extra_genre";

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


    BaseApater2.OnRecyclerViewItemActionListener mActionListener = new BaseApater2.OnRecyclerViewItemActionListener<MovieDataView>() {
        @Override
        public void onItemClick(View view, int postion, MovieDataView data) {
            mNewpageViewModel.startDetailActivity(FilterPageActivity.this, data);
        }

        @Override
        public void onItemFocus(View view, int position, MovieDataView data) {
//            mViewModel.refreshRowStr(position);
        }
    };

    TvRecyclerView.OnKeyPressListener mOnKeyPressListener = new TvRecyclerView.OnKeyPressListener() {
        @Override
        public void processKeyEvent(int keyCode) {

        }

        @Override
        public void onBackPress() {
            mBinding.btnFilter.requestFocus();
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
        mViewModel.setGenre(genreName);
        reloadMoiveDataViews();
    }

    private void initView() {
        mBinding.btnHome.setOnClickListener(mOnClickListener);
        mBinding.btnFilter.setOnClickListener(mOnClickListener);
        mBinding.tvTitle.setOnClickListener(mOnClickListener);
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
        mBinding.recyclerview.setOnKeyPressListener(mOnKeyPressListener);
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
            reloadMoiveDataViews();
        }
    }

    private void reloadMoiveDataViews() {
        mViewModel.reloadMoiveDataViews();
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
    public void OnMovieRemove(String movieId, int pos) {
        mMovieItemListAdapter.remove(movieId, pos);
        mViewModel.checkEmpty(mViewModel.decreaseTotal());
        setResult(RESULT_OK);
    }

    @Override
    public void OnMovieInsert(MovieDataView movieDataView, int pos) {
        mMovieItemListAdapter.insert(movieDataView, pos);
        mViewModel.checkEmpty(mViewModel.increaseTotal());
        setResult(RESULT_OK);
    }

}
