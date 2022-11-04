package com.hphtv.movielibrary.ui.postermenu;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.DialogUnknowfileItemMenuBinding;
import com.hphtv.movielibrary.listener.OnMovieChangeListener;
import com.hphtv.movielibrary.roomdb.entity.Season;
import com.hphtv.movielibrary.roomdb.entity.dataview.ConnectedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnknownRootDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.ui.moviesearch.online.MovieSearchDialog;
import com.hphtv.movielibrary.ui.moviesearch.online.SeasonSelectDialog;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.ToastUtil;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/4/6
 */
public class UnknownsFileMenuDialog extends BaseDialogFragment2<UnknownsFileMenuViewModel, DialogUnknowfileItemMenuBinding> {

    private SimpleObserver<MovieDataView> mFileObserver=new SimpleObserver<MovieDataView>() {
        @Override
        public void onAction(MovieDataView movieDataView) {
            if (getActivity() instanceof OnMovieChangeListener) {
                OnMovieChangeListener listener = (OnMovieChangeListener) getActivity();
                listener.OnMovieRemove(movieDataView.movie_id,movieDataView.type.name(), getArguments().getInt("position"));
            }
        }

        @Override
        public void onComplete() {
            super.onComplete();
            if (getActivity() instanceof AppBaseActivity)
                ((AppBaseActivity) getActivity()).stopLoading();
            getDialog().dismiss();

        }

        @Override
        public void onError(Throwable e) {
            super.onError(e);
            if (getActivity() instanceof AppBaseActivity)
                ((AppBaseActivity) getActivity()).stopLoading();
            ToastUtil.newInstance(getContext()).toast(getString(R.string.toast_selectmovie_faild));
        }
    };

    public static UnknownsFileMenuDialog newInstance(int pos, UnknownRootDataView dataView) {

        Bundle args = new Bundle();
        args.putInt("pos", pos);
        args.putSerializable("data", dataView);
        UnknownsFileMenuDialog fragment = new UnknownsFileMenuDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected UnknownsFileMenuViewModel createViewModel() {
        return null;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        bindDatas();
    }

    private void initView() {
        mBinding.includePlaymovie.view.setOnClickListener(this::playVideo);
        mBinding.includeSelectPoster.view.setOnClickListener(v -> {
            UnknownRootDataView rootDataView = mViewModel.getUnknownRootDataView();
            switch (rootDataView.type) {
                case FOLDER:
                    String root=rootDataView.root.substring(0,rootDataView.root.length()-1);
                    root=root.substring(root.lastIndexOf("/")+1);
                    showSelectPosterForFolder(root);
                    break;
                case FILE:
                    showSelectPoster(rootDataView.connectedFileView.keyword);
                    break;
            }
        });
    }


    private void bindDatas() {
        UnknownRootDataView dataView = (UnknownRootDataView) getArguments().getSerializable("data");
        int pos = getArguments().getInt("pos");
        mViewModel.setItemPosition(pos);
        mViewModel.setUnknownRootDataView(dataView);
        mBinding.setType(dataView.type);
        switch (dataView.type) {
            case FOLDER:
                String folderName = dataView.root.substring(0, dataView.root.length() - 1);
                folderName = folderName.substring(folderName.lastIndexOf("/") + 1, folderName.length());
                mBinding.setFilename(folderName);
                mBinding.setFilePath(dataView.root);
                break;
            case FILE:
                mBinding.setFilename(dataView.connectedFileView.filename);
                break;
        }
    }

    private void playVideo(View view) {
        ConnectedFileDataView connectedFileDataView = mViewModel.getUnknownRootDataView().connectedFileView;
        MovieHelper.playingMovie(connectedFileDataView.path, connectedFileDataView.filename)
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {

                    }
                });
    }

    /**
     * 为图片匹配
     * @param keyword
     */
    private void showSelectPoster(String keyword) {
        MovieSearchDialog movieSearchFragment = MovieSearchDialog.newInstance(keyword);
        movieSearchFragment.setOnSelectPosterListener((wrapper) -> {
            if(Constants.VideoType.tv.equals(wrapper.movie.type)){
                showSeasonDialog(wrapper, (wrapper1, season) -> {
                    mViewModel.rematchMovieFile(wrapper,season.seasonNumber)
                            .subscribe(mFileObserver);
                });
            }else{
                mViewModel.rematchMovieFile(wrapper)
                        .subscribe(mFileObserver);
            }

        });
        movieSearchFragment.show(getChildFragmentManager(), "");
    }


    /**
     * 为文件匹配
     * @param keyword
     */
    private void showSelectPosterForFolder(String keyword) {
        MovieSearchDialog movieSearchFragment = MovieSearchDialog.newInstance(keyword);
        movieSearchFragment.setOnSelectPosterListener((wrapper) -> {
            if(Constants.VideoType.tv.equals(wrapper.movie.type)){
                showSeasonDialog(wrapper, (wrapper1, season) -> {
                    mViewModel.rematchMovieFolder(wrapper,season.seasonNumber)
                            .subscribe(mFileObserver);
                });
            }else{
                mViewModel.rematchMovieFolder(wrapper)
                        .subscribe(mFileObserver);
            }
        });
        movieSearchFragment.show(getChildFragmentManager(), "");
    }


    private void showSeasonDialog(MovieWrapper wrapper, SeasonSelectDialog.OnClickListener listener) {
        SeasonSelectDialog seasonSelectDialog = SeasonSelectDialog.newInstance(wrapper);
        seasonSelectDialog.setOnClickListener(listener);
        seasonSelectDialog.show(getChildFragmentManager(), "");
    }
}
