package com.hphtv.movielibrary.ui.postermenu;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.DialogPosterItemMenuBinding;
import com.hphtv.movielibrary.listener.OnMovieChangeListener;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.ui.common.ConfirmDeleteDialog;
import com.hphtv.movielibrary.util.GlideTools;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/3/25
 */
public class PosterMenuDialog extends BaseDialogFragment2<PosterMenuViewModel, DialogPosterItemMenuBinding> {

    public static PosterMenuDialog newInstance(int pos,MovieDataView movieDataView) {
        Bundle args = new Bundle();
        PosterMenuDialog fragment = new PosterMenuDialog();
        args.putSerializable("movie", movieDataView);
        args.putInt("position",pos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mViewModel.loadMovieProperty((MovieDataView) getArguments().getSerializable("movie"))
                .subscribe(movieDataView ->
                        GlideTools.GlideWrapper(getContext(), movieDataView.poster)
                                .placeholder(R.mipmap.default_poster)
                                .into(mBinding.image));
    }

    private void initView() {
        mBinding.includeAp.view.setOnClickListener(v -> mViewModel.toggleAccessRights());
        mBinding.includeFavorite.view.setOnClickListener(v -> mViewModel.toggleLike());
        mBinding.includeAddVideotag.view.setOnClickListener(v -> {
        });
        mBinding.includeSelectPoster.view.setOnClickListener(v -> {
        });
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
        ConfirmDeleteDialog confirmDeleteDialog = ConfirmDeleteDialog.newInstance(mViewModel.getMovieDataView().movie_id);
        confirmDeleteDialog.setMessage(getString(R.string.remove_confirm));
        confirmDeleteDialog.setConfirmDeleteListener(new ConfirmDeleteDialog.ConfirmDeleteListener() {
            @Override
            public void confirmDelete(String movie_id) {
                PosterMenuDialog.this.dismiss();
                if(getActivity() instanceof OnMovieChangeListener){
                    OnMovieChangeListener listener= (OnMovieChangeListener) getActivity();
                    listener.OnMovieChange();
                }
            }

            @Override
            public void onDismiss() {
                PosterMenuDialog.this.getDialog().show();
            }
        });
        confirmDeleteDialog.show(getChildFragmentManager(),null);
        getDialog().hide();
    }
}
