package com.hphtv.movielibrary.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.firelfy.util.LogUtil;
import com.hphtv.movielibrary.activity.MovieDetailActivity;
import com.hphtv.movielibrary.adapter.BaseAdapter;
import com.hphtv.movielibrary.adapter.MovieLibraryAdapter;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.databinding.FLayoutFavoriteBinding;
import com.hphtv.movielibrary.roomdb.entity.MovieDataView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lxp
 * @date 19-5-15
 */
public class HomePageFragment extends Fragment {
    private static final int COLUMS = 6;

    public static final String TAG = HomePageFragment.class.getSimpleName();

    private MovieLibraryAdapter mMovieLibraryAdapter;// 电影列表适配器
    private List<MovieDataView> mMovieDataViewList = new ArrayList<>();// 电影数据

    private FLayoutFavoriteBinding mFLayoutFavoriteBinding;
    private ActivityResultLauncher mActivityResultLauncher;

    public static HomePageFragment newInstance() {

        Bundle args = new Bundle();

        HomePageFragment fragment = new HomePageFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityResultContracts.StartActivityForResult startActivityForResult = new ActivityResultContracts.StartActivityForResult();
        mActivityResultLauncher = registerForActivityResult(startActivityForResult, result -> {
            Log.v(TAG, "onActivityResult resultCode=" + result.getResultCode());
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mFLayoutFavoriteBinding = FLayoutFavoriteBinding.inflate(inflater);
        return mFLayoutFavoriteBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    public void onResume() {
        LogUtil.v(TAG,"OnResume");
        super.onResume();
    }

    /**
     * 初始化
     */
    private void initView() {
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getContext(), COLUMS, GridLayoutManager.VERTICAL, false);
        mFLayoutFavoriteBinding.rvMovies.setLayoutManager(mGridLayoutManager);
        mMovieLibraryAdapter = new MovieLibraryAdapter(getContext(), mMovieDataViewList);
        mFLayoutFavoriteBinding.rvMovies.setAdapter(mMovieLibraryAdapter);
        mMovieLibraryAdapter
                .setOnItemClickListener((BaseAdapter.OnRecyclerViewItemClickListener<MovieDataView>) (view, data) -> {
                    Intent intent = new Intent(HomePageFragment.this.getContext(),
                            MovieDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong(ConstData.IntentKey.KEY_MOVIE_ID, data.id);
                    bundle.putInt("mode", ConstData.MovieDetailMode.MODE_WRAPPER);
                    intent.putExtras(bundle);
                    mActivityResultLauncher.launch(intent);
                });
    }

    public void updateMovie(List<MovieDataView> movieDataViews) {
        if (movieDataViews.size() > 0) {
            mFLayoutFavoriteBinding.tipsEmpty.setVisibility(View.GONE);
        } else {
            mFLayoutFavoriteBinding.tipsEmpty.setVisibility(View.VISIBLE);
        }
        mMovieLibraryAdapter.addAll(movieDataViews);
    }
}
