package com.hphtv.movielibrary.ui.homepage.fragment.theme;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.ui.homepage.IAutofitHeight;
import com.hphtv.movielibrary.ui.homepage.fragment.BaseHomeFragment;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/6/7
 */
public class ThemeFragment extends BaseHomeFragment<ThemeFragmentViewModel>  {
    public static final String TAG=ThemeFragment.class.getSimpleName();


    public static ThemeFragment newInstance(IAutofitHeight autofitHeight, int position, Constants.VideoType type) {
        Bundle args = new Bundle();
        ThemeFragment fragment = new ThemeFragment(autofitHeight, position);
        args.putSerializable("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    public ThemeFragment(IAutofitHeight autofitHeight, int position) {
        super(autofitHeight, position, TAG);
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel.setVideoType((Constants.VideoType) getArguments().getSerializable("type"));
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setType(mViewModel.getVideoType());
    }

    @Override
    protected ThemeFragmentViewModel createViewModel() {
        return null;
    }

    @Override
    protected String getVideoTagName() {
        return mViewModel.getVideoType().name();
    }

    @Override
    public void remoteUpdateFavorite(String movie_id, String type, boolean isFavorite) {
        if (mViewModel != null && mViewModel.getVideoType().name().equals(type)) {//TODO 检查是否生效
            super.remoteUpdateFavorite(movie_id, type, isFavorite);
        }
    }
}
