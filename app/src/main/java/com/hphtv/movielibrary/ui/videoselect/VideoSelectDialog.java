package com.hphtv.movielibrary.ui.videoselect;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.databinding.DialogSelectVideosourceBinding;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * Created by tchip on 17-11-23.
 */

public class VideoSelectDialog extends BaseDialogFragment2<VideoSelectViewModel, DialogSelectVideosourceBinding> {
    public static final String FILE = "file";
    public static final String MOVIE_WRAPPER = "movie_wrapper";
    private VideoSelectListAdapter mVideoSelectListAdapter;
    private PlayVideoListener mPlayVideoListener;

    public static VideoSelectDialog newInstance(Object data) {

        Bundle args = new Bundle();
        if(data!=null) {
            if(data instanceof UnrecognizedFileDataView) {
                args.putSerializable(FILE, (UnrecognizedFileDataView)data);
            }else if(data instanceof MovieWrapper){
                args.putSerializable(MOVIE_WRAPPER, (MovieWrapper)data);
            }
        }
        VideoSelectDialog fragment = new VideoSelectDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected VideoSelectViewModel createViewModel() {
        return null;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        createList();
    }

    private void createList() {
        mBinding.btnClose.setOnClickListener(v -> dismiss());
        mVideoSelectListAdapter = new VideoSelectListAdapter(getContext(), new ArrayList<>());
        mBinding.rvSource.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        mBinding.rvSource.setAdapter(mVideoSelectListAdapter);
        mVideoSelectListAdapter.setOnItemClickListener((view, postion, data) -> {
            if (mPlayVideoListener != null) {
                mPlayVideoListener.playVideo(mViewModel.playVideo(data.path, data.filename));
            } else {
                mViewModel.playVideo(data.path, data.filename).subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                        dismiss();
                    }
                });
            }
        });


        Object obj = getArguments().getSerializable(FILE);
        if (obj != null && obj instanceof UnrecognizedFileDataView) {
            UnrecognizedFileDataView file = (UnrecognizedFileDataView) obj;
            mViewModel.getVideoList(file.keyword).subscribe(new SimpleObserver<List<VideoFile>>() {
                @Override
                public void onAction(List<VideoFile> videoFileList) {
                    mVideoSelectListAdapter.addAll(videoFileList);
                }
            });
        }

        obj = getArguments().getSerializable(MOVIE_WRAPPER);
        if (obj != null && obj instanceof MovieWrapper) {
            MovieWrapper wrapper = (MovieWrapper) obj;
            mVideoSelectListAdapter.addAll(wrapper.videoFiles);
        }
    }

    public interface PlayVideoListener {
        void playVideo(Observable<String> observable);
    }

    public void setPlayingVideo(PlayVideoListener listener) {
        mPlayVideoListener = listener;
    }
}
