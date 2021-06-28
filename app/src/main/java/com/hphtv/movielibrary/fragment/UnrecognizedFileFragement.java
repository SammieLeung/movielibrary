package com.hphtv.movielibrary.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firelfy.util.LogUtil;
import com.hphtv.movielibrary.activity.MovieDetailActivity;
import com.hphtv.movielibrary.adapter.BaseAdapter;
import com.hphtv.movielibrary.adapter.UnrecognizedFileListAdapter;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.databinding.FLayoutFavoriteBinding;
import com.hphtv.movielibrary.roomdb.entity.UnrecognizedFileDataView;
import com.hphtv.movielibrary.viewmodel.fragment.UnrecognizeFileViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * author: Sam Leung
 * date:  2021/6/25
 */
public class UnrecognizedFileFragement extends BaseFragment<UnrecognizeFileViewModel, FLayoutFavoriteBinding> {
    public static final int COLUMS = 6;
    public static final String TAG = UnrecognizedFileFragement.class.getSimpleName();
    private UnrecognizedFileListAdapter mAdapter;
    private List<UnrecognizedFileDataView> mUnrecognizedFileDataViewList;

    private ActivityResultLauncher mActivityResultLauncher;

    public static UnrecognizedFileFragement newInstance() {
        Bundle args = new Bundle();
        UnrecognizedFileFragement fragment = new UnrecognizedFileFragement();
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


    @Override
    protected void onViewCreated() {
        mUnrecognizedFileDataViewList = new ArrayList<>();
        mAdapter = new UnrecognizedFileListAdapter(getContext(), mUnrecognizedFileDataViewList);
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getContext(), COLUMS, GridLayoutManager.VERTICAL, false);
        mBinding.rvMovies.setLayoutManager(mGridLayoutManager);
        mBinding.rvMovies.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((BaseAdapter.OnRecyclerViewItemClickListener<UnrecognizedFileDataView>) (view, data) -> {
            Intent intent = new Intent(getContext(),
                    MovieDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(ConstData.IntentKey.KEY_UNRECOGNIZE_FILE_KEYWORD, data.keyword);
            bundle.putInt(ConstData.IntentKey.KEY_MODE, ConstData.MovieDetailMode.MODE_UNRECOGNIZEDFILE);
            intent.putExtras(bundle);
            mActivityResultLauncher.launch(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.v(TAG, "onResume");
        getUnrecognizedFiles();
    }

    private void getUnrecognizedFiles() {
        mViewModel.prepareUnrecognizedFile(unrecognizedFileDataViewList -> {
            if (unrecognizedFileDataViewList != null && unrecognizedFileDataViewList.size() > 0) {
                mAdapter.addAll(unrecognizedFileDataViewList);
                mBinding.tipsEmpty.setVisibility(View.GONE);
            } else {
                mBinding.tipsEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}
