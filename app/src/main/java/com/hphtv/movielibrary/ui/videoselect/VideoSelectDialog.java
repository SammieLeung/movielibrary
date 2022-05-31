package com.hphtv.movielibrary.ui.videoselect;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hphtv.movielibrary.adapter.BaseAdapter2;
import com.hphtv.movielibrary.databinding.DialogSelectVideosourceBinding;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * Created by tchip on 17-11-23.
 */

public class VideoSelectDialog extends BaseDialogFragment2<VideoSelectViewModel, DialogSelectVideosourceBinding> {
    public static final String FILE = "file";
    public static final String MOVIE_WRAPPER = "movie_wrapper";
    public static final String FILE_LIST = "file_list";

    private VideoSelectListAdapter mVideoSelectListAdapter;
    private PlayVideoListener mPlayVideoListener;

    public static VideoSelectDialog newInstance(UnrecognizedFileDataView data) {
        Bundle args = new Bundle();
        args.putSerializable(FILE, (UnrecognizedFileDataView) data);
        return newInstance(args);
    }

    public static VideoSelectDialog newInstance(MovieWrapper data) {
        Bundle args = new Bundle();
        args.putSerializable(MOVIE_WRAPPER, (MovieWrapper) data);
        return newInstance(args);
    }

    public static VideoSelectDialog newInstance(List<VideoFile> videoFileList) {
        Bundle args = new Bundle();
        args.putSerializable(FILE_LIST, (Serializable) videoFileList);
        VideoSelectDialog fragment = new VideoSelectDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private static VideoSelectDialog newInstance(Bundle args) {
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
        mVideoSelectListAdapter.setOnItemClickListener(new BaseAdapter2.OnRecyclerViewItemActionListener<VideoFile>() {
            @Override
            public void onItemClick(View view, int postion, VideoFile data) {
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
            }

            @Override
            public void onItemFocus(View view, int postion, VideoFile data) {

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
            return;
        }

        obj = getArguments().getSerializable(MOVIE_WRAPPER);
        if (obj != null && obj instanceof MovieWrapper) {
            MovieWrapper wrapper = (MovieWrapper) obj;
            wrapper.videoFiles.sort(Comparator.comparing(o -> o.filename));
            mVideoSelectListAdapter.addAll(wrapper.videoFiles);
            return;
        }

        obj = getArguments().getSerializable(FILE_LIST);
        if (obj != null && obj instanceof List) {
            List<VideoFile> videoFileList = (List<VideoFile>) obj;
            videoFileList.sort(Comparator.comparing(o -> o.filename));
            mVideoSelectListAdapter.addAll(videoFileList);
            return;
        }
    }

    public interface PlayVideoListener {
        void playVideo(Observable<String> observable);
    }

    public void setPlayingVideo(PlayVideoListener listener) {
        mPlayVideoListener = listener;
    }
}