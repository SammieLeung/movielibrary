package com.hphtv.movielibrary.ui.homepage.fragment.theme;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment;
import com.hphtv.movielibrary.ui.homepage.fragment.BaseHomeFragment;
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/6/7
 */
public class ThemeFragment extends BaseHomeFragment<ThemeFragmentViewModel> {

    public ThemeFragment() {
        super();
    }

    public static ThemeFragment newInstance(NoScrollAutofitHeightViewPager viewPager, int position, Constants.VideoType type) {
        Bundle args = new Bundle();
        ThemeFragment fragment = new ThemeFragment();
        args.putSerializable("type", type);
        args.putInt(BaseAutofitHeightFragment.POSITION, position);
        fragment.setArguments(args);
        fragment.setAutoFitHeightViewPager(viewPager);
        return fragment;
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

}
