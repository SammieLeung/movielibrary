package com.hphtv.movielibrary.ui.postermenu;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.DialogPosterItemMenuBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.hphtv.movielibrary.util.GlideTools;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.functions.Consumer;

/**
 * author: Sam Leung
 * date:  2022/3/25
 */
public class PosterMenuDialog extends BaseDialogFragment2<PosterMenuViewModel, DialogPosterItemMenuBinding> {

    public static PosterMenuDialog newInstance(MovieDataView movieDataView) {
        Bundle args = new Bundle();
        PosterMenuDialog fragment = new PosterMenuDialog();
        args.putSerializable("movie",movieDataView);
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
        .subscribe(new Consumer<MovieDataView>() {
            @Override
            public void accept(MovieDataView movieDataView) throws Throwable {
                GlideTools.GlideWrapper(getContext(),movieDataView.poster).placeholder(R.mipmap.default_poster).into(mBinding.image);
            }
        });
    }

    private void initView(){

    }

    private void bindDatas(){
        mBinding.setAp(mViewModel.getAccessRights());
        mBinding.setLikeState(mViewModel.getLikeFlag());
        mBinding.setTags(mViewModel.getTagString());
        mBinding.setWatchedState(mViewModel.getWatchedFlag());
    }
}
