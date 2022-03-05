package com.hphtv.movielibrary.ui.filterpage;

import android.content.Intent;
import android.os.Bundle;
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
import com.hphtv.movielibrary.listener.OnMovieLoadListener;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.homepage.NewpageViewModel;
import com.hphtv.movielibrary.ui.view.TvRecyclerView;
import com.hphtv.movielibrary.util.ActivityHelper;
import com.station.kit.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/2/22
 */
public class FilterPageAcitvity extends AppBaseActivity<FilterPageViewModel, ActivityFilterpageBinding> {
    public static final String EXTRA_GENRE = "extra_genre";

    private NewMovieItemListAdapter mMovieItemListAdapter;
    private NewpageViewModel mNewpageViewModel;

    View.OnClickListener mOnClickListener = v -> {
        switch (v.getId()) {
            case R.id.btn_home:
                ActivityHelper.startHomePageActivity(FilterPageAcitvity.this);
                break;
            case R.id.btn_filter:
                FilterBoxDialogFragment filterBoxDialogFragment = FilterBoxDialogFragment.newInstance();
                filterBoxDialogFragment.show(getSupportFragmentManager(), "");
                break;
        }
    };

    BaseApater2.OnRecyclerViewItemActionListener mActionListener=new BaseApater2.OnRecyclerViewItemActionListener<MovieDataView>() {
        @Override
        public void onItemClick(View view, int postion, MovieDataView data) {
            mNewpageViewModel.startDetailActivity(FilterPageAcitvity.this, data);
        }

        @Override
        public void onItemFocus(View view, int postion, MovieDataView data) {
            mBinding.setCount(getString(R.string.genre_all) + " " + (postion + 1) + "/" + mViewModel.getTotal());
        }
    };

    TvRecyclerView.OnKeyPressListener mOnKeyPressListener=new TvRecyclerView.OnKeyPressListener() {
        @Override
        public void processKeyEvent(int keyCode) {

        }

        @Override
        public void onBackPress() {
            finish();
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel.setOnRefresh(mOnRefresh);
        mNewpageViewModel = new ViewModelProvider(this).get(NewpageViewModel.class);
        initView();
        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String genreName = getIntent().getStringExtra(EXTRA_GENRE);
        if (!TextUtils.isEmpty(genreName)) {
            mViewModel.setGenre(genreName);
        }
        reloadMoiveDataViews();

    }

    private void initView() {
        mBinding.btnHome.setOnClickListener(mOnClickListener);
        mBinding.btnFilter.setOnClickListener(mOnClickListener);
        FilterGridLayoutManager gridLayoutManager = new FilterGridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false);
        gridLayoutManager.setVisibleItemListener(v -> {
            if (v != null) {
                mBinding.btnFilter.setNextFocusDownId(v.getId());
                mBinding.btnHome.setNextFocusDownId(v.getId());
            }
        });
        mBinding.recyclerview.setLayoutManager(gridLayoutManager);

        mMovieItemListAdapter = new NewMovieItemListAdapter(this, new ArrayList<>());
        mMovieItemListAdapter.setOnItemClickListener(mActionListener);

        mBinding.recyclerview.addItemDecoration(new GridSpacingItemDecorationVertical(
                getResources().getDimensionPixelSize(R.dimen.poster_item_1_w),
                DensityUtil.dip2px(this, 43),
                DensityUtil.dip2px(this, 32),
                DensityUtil.dip2px(this, 40),
                DensityUtil.dip2px(this, 30),
                5)
        );
        mBinding.recyclerview.setAdapter(mMovieItemListAdapter);
        mBinding.recyclerview.addOnScrollListener(new OnMovieLoadListener() {
            @Override
            protected void onLoading(int countItem, int lastItem) {
                loadAMoreAllMovies();
            }
        });
        mBinding.recyclerview.setOnKeyPressListener(mOnKeyPressListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        super.onActivityResultCallback(result);
        if (result.getResultCode() == RESULT_OK) {
            reloadMoiveDataViews();//TODO 优化方向:按照详情页的具体操作加载内容
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
        }

        @Override
        public void appendMovieDataViews(List<MovieDataView> movieDataViews) {
            if (movieDataViews.size() > 0)
                mMovieItemListAdapter.appendAll(movieDataViews);
        }
    };
}
