package com.hphtv.movielibrary.ui.postermenu;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.DialogPosterItemMenuBinding;
import com.hphtv.movielibrary.listener.OnMovieChangeListener;
import com.hphtv.movielibrary.roomdb.entity.Season;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.ui.common.ConfirmDeleteDialog;
import com.hphtv.movielibrary.ui.moviesearch.online.MovieSearchDialog;
import com.hphtv.movielibrary.ui.moviesearch.online.SeasonSelectDialog;
import com.hphtv.movielibrary.util.GlideTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.ToastUtil;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/3/25
 */
public class PosterMenuDialog extends BaseDialogFragment2<PosterMenuViewModel, DialogPosterItemMenuBinding> {

    public static PosterMenuDialog newInstance(int pos, MovieDataView movieDataView) {
        Bundle args = new Bundle();
        PosterMenuDialog fragment = new PosterMenuDialog();
        args.putSerializable("movie", movieDataView);
        args.putInt("position", pos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel.setItemPosition(getArguments().getInt("position"));
    }

    @Override
    protected PosterMenuViewModel createViewModel() {
        return null;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindDatas();
        initView();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadMovieProperty((MovieDataView) getArguments().getSerializable("movie"));
    }

    private void initView() {
        mBinding.includeAp.view.setOnClickListener(v -> mViewModel.toggleAccessRights((OnMovieChangeListener) getActivity()));
        mBinding.includeAddVideotag.view.setOnClickListener(v -> {
        });
        mBinding.includeSelectPoster.view.setOnClickListener(v -> showSearchDialog());
        mBinding.includeClearPoster.view.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void bindDatas() {
        mBinding.setIsMatched(mViewModel.getMatchedFlag());
        mBinding.setAp(mViewModel.getAccessRights());
        mBinding.setLikeState(mViewModel.getLikeFlag());
        mBinding.setTags(mViewModel.getTagString());
        mBinding.setWatchedState(mViewModel.getWatchedFlag());
    }

    private void showDeleteConfirmDialog() {
        ConfirmDeleteDialog confirmDeleteDialog = ConfirmDeleteDialog.newInstance(mViewModel.getMovieDataView().movie_id, mViewModel.getMovieDataView().type);
        confirmDeleteDialog.setMessage(getString(R.string.remove_confirm));
        confirmDeleteDialog.setConfirmDeleteListener(new ConfirmDeleteDialog.ConfirmDeleteListener() {
            @Override
            public void confirmDelete(String movie_id, String type) {
                PosterMenuDialog.this.dismiss();
                if (getActivity() instanceof OnMovieChangeListener) {
                    OnMovieChangeListener listener = (OnMovieChangeListener) getActivity();
                    listener.OnMovieRemove(movie_id, type, getArguments().getInt("position"));
                }
            }

            @Override
            public void onDismiss() {
                PosterMenuDialog.this.getDialog().show();
            }
        });
        confirmDeleteDialog.show(getChildFragmentManager(), null);
        getDialog().hide();
    }

    private void showSearchDialog() {
        MovieSearchDialog movieSearchFragment = MovieSearchDialog.newInstance(mViewModel.getMovieDataView().title);
        movieSearchFragment.setOnSelectPosterListener((wrapper) -> {
            if (Constants.VideoType.tv.equals(wrapper.movie.type)&&wrapper.season!=null) {
                showSeasonDialog(wrapper, new SeasonSelectDialog.OnClickListener() {
                    @Override
                    public void onClick(MovieWrapper newWrapper, Season season) {
                        globalStartLoading();
                        mViewModel.rematchWithSeries(newWrapper, season.seasonNumber)
                                .subscribe(new SimpleObserver<MovieDataView>() {
                                    @Override
                                    public void onAction(MovieDataView movieDataView) {
                                        loadMovieProperty(movieDataView);
                                        if (getActivity() instanceof OnMovieChangeListener) {
                                            OnMovieChangeListener listener = (OnMovieChangeListener) getActivity();
                                            listener.OnRematchPoster(movieDataView, getArguments().getInt("position"));
                                        }
                                    }

                                    @Override
                                    public void onComplete() {
                                        super.onComplete();
                                        globalStopLoading();
                                        movieSearchFragment.dismiss();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        super.onError(e);
                                        globalStopLoading();
                                        ToastUtil.newInstance(getContext()).toast(getString(R.string.toast_selectmovie_faild));
                                    }

                                });
                    }
                });
            } else {
                mViewModel.rematchWithMovie(wrapper)
                        .subscribe(new SimpleObserver<MovieDataView>() {
                            @Override
                            public void onAction(MovieDataView movieDataView) {
                                loadMovieProperty(movieDataView);
                                if (getActivity() instanceof OnMovieChangeListener) {
                                    OnMovieChangeListener listener = (OnMovieChangeListener) getActivity();
                                    listener.OnRematchPoster(movieDataView, getArguments().getInt("position"));
                                }
                            }

                            @Override
                            public void onComplete() {
                                super.onComplete();
                                globalStopLoading();
                                movieSearchFragment.dismiss();
                            }

                            @Override
                            public void onError(Throwable e) {
                                super.onError(e);
                                globalStopLoading();
                                ToastUtil.newInstance(getContext()).toast(getString(R.string.toast_selectmovie_faild));
                            }
                        });
            }


        });
        movieSearchFragment.show(getChildFragmentManager(), "");
    }

    private void showSeasonDialog(MovieWrapper wrapper, SeasonSelectDialog.OnClickListener listener) {
        SeasonSelectDialog seasonSelectDialog = SeasonSelectDialog.newInstance(wrapper);
        seasonSelectDialog.setOnClickListener(listener);
        seasonSelectDialog.show(getChildFragmentManager(), "");
    }

    private void loadMovieProperty(MovieDataView dataView) {
        mViewModel.loadMovieProperty(dataView)
                .subscribe(movieDataView -> {
                    String poster=movieDataView.season_poster;
                    if(TextUtils.isEmpty(poster))
                        poster=movieDataView.poster;
                    GlideTools.GlideWrapper(getContext(),poster)
                            .into(mBinding.image);
                });
    }

    private void globalStartLoading() {
        if (getActivity() instanceof AppBaseActivity)
            ((AppBaseActivity) getActivity()).startLoading();
    }

    private void globalStopLoading() {
        if (getActivity() instanceof AppBaseActivity)
            ((AppBaseActivity) getActivity()).stopLoading();
    }

}
