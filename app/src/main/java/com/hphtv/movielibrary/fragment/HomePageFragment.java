package com.hphtv.movielibrary.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.activity.MovieDetailActivity;
import com.hphtv.movielibrary.adapter.MovieLibraryAdapter;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.databinding.FLayoutFavoriteBinding;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.MovieWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author lxp
 * @date 19-5-15
 */
public class HomePageFragment extends Fragment {
    private static final int COLUMS = 6;

    public static final String TAG = HomePageFragment.class.getSimpleName();

    private MovieLibraryAdapter mLibraryAdapter;// 电影列表适配器
    private List<MovieWrapper> mWrapperList = new ArrayList<>();// 电影数据

    private MovieDao mMovieDao;
    private DeviceDao mDeviceDao;
    private static AtomicBoolean atomicBoolean = new AtomicBoolean();

    private FLayoutFavoriteBinding mFLayoutFavoriteBinding;

    public static HomePageFragment newInstance() {

        Bundle args = new Bundle();

        HomePageFragment fragment = new HomePageFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDao();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mFLayoutFavoriteBinding=FLayoutFavoriteBinding.inflate(inflater);
        return mFLayoutFavoriteBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        initMovie();
    }

    /**
     * 初始化数据库
     */
    private void initDao() {
        MovieLibraryRoomDatabase movieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(getContext());
        mMovieDao = movieLibraryRoomDatabase.getMovieDao();
        mDeviceDao = movieLibraryRoomDatabase.getDeviceDao();
    }

    public void initMovie() {
    }

    /**
     * 初始化
     */
    private void initView() {

        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getContext(), COLUMS, GridLayoutManager.VERTICAL, false);
        mFLayoutFavoriteBinding.rvMovies.setLayoutManager(mGridLayoutManager);
        mLibraryAdapter = new MovieLibraryAdapter(getContext(), mWrapperList);
        mFLayoutFavoriteBinding.rvMovies.setAdapter(mLibraryAdapter);
        mLibraryAdapter
                .setOnItemClickListener((view, wrapper) -> {

                    Intent intent = new Intent(getContext(),
                            MovieDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("movie_id", wrapper.movie.id);
                    bundle.putInt("mode", ConstData.MovieDetailMode.MODE_WRAPPER);
                    intent.putExtras(bundle);
                    ActivityResultContracts.StartActivityForResult startActivityForResult=new ActivityResultContracts.StartActivityForResult();
                    registerForActivityResult(startActivityForResult, result -> {
                        Log.v(TAG, "onActivityResult resultCode=" + result.getResultCode());
                    }).launch(intent);
                });

    }


    private void refreshMovie() {
        if (mWrapperList.size() > 0) {
            mFLayoutFavoriteBinding.tipsEmpty.setVisibility(View.GONE);
        } else {
            mFLayoutFavoriteBinding.tipsEmpty.setVisibility(View.VISIBLE);
        }
        mLibraryAdapter.notifyDataSetChanged();
    }

}
