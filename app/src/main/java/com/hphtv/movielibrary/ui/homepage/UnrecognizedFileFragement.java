package com.hphtv.movielibrary.ui.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.ui.detail.MovieDetailActivity;
import com.hphtv.movielibrary.adapter.UnrecognizedFileListAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.FLayoutMovieBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.ui.BaseFragment;
import com.hphtv.movielibrary.ui.videoselect.VideoSelectDialog;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;

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
        args.putInt(Constants.Extras.CURRENT_FRAGMENT, pos);
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
        mAdapter.setOnItemClickListener((view, data) -> {
//
//            Intent intent = new Intent(getContext(),
//                    MovieDetailActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putString(Constants.Extras.UNRECOGNIZE_FILE_KEYWORD, data.keyword);
//            bundle.putInt(Constants.Extras.MODE, Constants.MovieDetailMode.MODE_UNRECOGNIZEDFILE);
//            intent.putExtras(bundle);
//            startActivityForResultFromParent(intent);
            mViewModel.playingVideo(data)
                    .subscribe(videoFileList -> {
                        if(videoFileList.size()==1){
                            mViewModel.getApplication().playingMovie(videoFileList.get(0).path,videoFileList.get(0).filename)
                            .subscribe(s -> {

                            });
                        }else if(videoFileList.size()>1){
                            VideoSelectDialog videoSelectDialog=VideoSelectDialog.newInstance(data);
                            videoSelectDialog.show(getChildFragmentManager(),"");
                        }
                    });
        });
    }

    /**
     * 刷新未识别内容
     */
    public void notifyUpdate() {
        if (mViewModel != null)
            mViewModel.prepareUnrecognizedFile(unrecognizedFileDataViewList -> {
                if (unrecognizedFileDataViewList != null && unrecognizedFileDataViewList.size() > 0) {
                    mBinding.tipsEmpty.setVisibility(View.GONE);
                    mAdapter.addAll(unrecognizedFileDataViewList);
                } else {
                    mBinding.tipsEmpty.setVisibility(View.VISIBLE);
                }
                notifyStopLoading();
            });
    }
}
