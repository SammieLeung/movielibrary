package com.hphtv.movielibrary.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.activity.MovieDetailActivity;
import com.hphtv.movielibrary.adapter.BaseAdapter;
import com.hphtv.movielibrary.adapter.UnrecognizedFileListAdapter;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.databinding.FLayoutMovieBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.viewmodel.fragment.UnrecognizeFileFragmentViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/6/25
 */
public class UnrecognizedFileFragement extends BaseFragment<UnrecognizeFileFragmentViewModel, FLayoutMovieBinding> {
    public static final String TAG = UnrecognizedFileFragement.class.getSimpleName();
    private UnrecognizedFileListAdapter mAdapter;
    private List<UnrecognizedFileDataView> mUnrecognizedFileDataViewList;


    public static UnrecognizedFileFragement newInstance(int pos) {
        Bundle args = new Bundle();
        args.putInt(ConstData.IntentKey.KEY_CUR_FRAGMENT, pos);
        UnrecognizedFileFragement fragment = new UnrecognizedFileFragement();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onViewCreated() {
        mUnrecognizedFileDataViewList = new ArrayList<>();
        mAdapter = new UnrecognizedFileListAdapter(getContext(), mUnrecognizedFileDataViewList);
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getContext(), mColums, GridLayoutManager.VERTICAL, false);
        mBinding.rvMovies.setLayoutManager(mGridLayoutManager);
        mBinding.rvMovies.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((BaseAdapter.OnRecyclerViewItemClickListener<UnrecognizedFileDataView>) (view, data) -> {
            Intent intent = new Intent(getContext(),
                    MovieDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(ConstData.IntentKey.KEY_UNRECOGNIZE_FILE_KEYWORD, data.keyword);
            bundle.putInt(ConstData.IntentKey.KEY_MODE, ConstData.MovieDetailMode.MODE_UNRECOGNIZEDFILE);
            intent.putExtras(bundle);
            startActivityForResultFromParent(intent);
        });
    }

    /**
     * 刷新未识别内容
     */
    public void notifyUpdate() {
        mViewModel.prepareUnrecognizedFile(unrecognizedFileDataViewList -> {
            if (unrecognizedFileDataViewList != null && unrecognizedFileDataViewList.size() > 0) {
                mBinding.tipsEmpty.setVisibility(View.GONE);
                mAdapter.addAll(unrecognizedFileDataViewList);
            } else {
                mAdapter.addAll(unrecognizedFileDataViewList);
                mBinding.tipsEmpty.setVisibility(View.VISIBLE);
            }
            notifyStopLoading();
        });
    }
}
