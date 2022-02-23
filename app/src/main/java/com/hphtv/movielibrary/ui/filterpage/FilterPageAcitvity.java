package com.hphtv.movielibrary.ui.filterpage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseApater2;
import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.ActivityFilterpageBinding;
import com.hphtv.movielibrary.effect.GridSpacingItemDecorationVertical;
import com.hphtv.movielibrary.listener.OnMovieLoadListener;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.detail.MovieDetailActivity;
import com.hphtv.movielibrary.ui.moviesearch.PinyinSearchActivity;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;

/**
 * author: Sam Leung
 * date:  2022/2/22
 */
public class FilterPageAcitvity extends AppBaseActivity<FilterPageViewModel, ActivityFilterpageBinding> {
    private NewMovieItemListAdapter mMovieItemListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        prepareMovies();
    }

    private void initView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5, GridLayoutManager.VERTICAL, false);
        mBinding.recyclerview.setLayoutManager(gridLayoutManager);
        mMovieItemListAdapter = new NewMovieItemListAdapter(this, new ArrayList<>());
        mMovieItemListAdapter .setOnItemClickListener(new BaseApater2.OnRecyclerViewItemActionListener<MovieDataView>() {
            @Override
            public void onItemClick(View view, int postion, MovieDataView data) {
                Intent intent = new Intent(FilterPageAcitvity.this,
                        MovieDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong(Constants.Extras.MOVIE_ID, data.id);
                bundle.putInt(Constants.Extras.MODE, Constants.MovieDetailMode.MODE_WRAPPER);
                intent.putExtras(bundle);
                startActivityForResult(intent);
            }
        });
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
                loadMoreMovies();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        super.onActivityResultCallback(result);
        if(result.getResultCode()==RESULT_OK){
            prepareMovies();//TODO 优化方向:按照详情页的具体操作加载内容
        }
    }

    private void prepareMovies() {
        mViewModel.reloadMoiveDataViews()
                .subscribe(new SimpleObserver<List<MovieDataView>>() {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViews) {
                        mMovieItemListAdapter.addAll(movieDataViews);
                    }
                });
    }

    private void loadMoreMovies() {
        mViewModel.loadMoiveDataViews()
                .subscribe(new SimpleObserver<List<MovieDataView>>() {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViews) {
                        if (movieDataViews.size() > 0)
                            mMovieItemListAdapter.appendAll(movieDataViews);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        startLoading();
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        stopLoading();
                    }
                });
    }
}
