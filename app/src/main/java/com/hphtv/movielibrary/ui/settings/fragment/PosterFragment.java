package com.hphtv.movielibrary.ui.settings.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.NextFocusModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.databinding.FragmentSettingsPosterBinding;
import com.hphtv.movielibrary.ui.BaseFragment2;
import com.hphtv.movielibrary.ui.settings.SettingsViewModel;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/3/17
 */
public class PosterFragment extends BaseFragment2<SettingsViewModel, FragmentSettingsPosterBinding> {
    public static PosterFragment newInstance() {
        PosterFragment fragment = new PosterFragment();
        return fragment;
    }

    @Override
    protected SettingsViewModel createViewModel() {
        return new ViewModelProvider(getActivity()).get(SettingsViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        bindDatas();
    }

    private void bindDatas() {
        NextFocusModel model = new NextFocusModel();
        model.setNextFocusLeft(R.id.tab_poster);
        mBinding.setNextFocus(model);
    }

    private void initView() {
        mBinding.optionTitle.view.setOnClickListener(mViewModel::toggleTitle);
        mBinding.optionPoster.view.setOnClickListener(mViewModel::togglePoster);
        mBinding.optionLike.view.setOnClickListener(mViewModel::toggleLike);
        mBinding.optionRating.view.setOnClickListener(mViewModel::toggleRating);
        mBinding.optionCornermark.view.setOnClickListener(mViewModel::toggleCornermark);
        mBinding.setCornerMarkState(Config.getShowCornerMark());
        mBinding.setPosterState(Config.getShowPoster());
        mBinding.setLikeState(Config.getShowLike());
        mBinding.setRatingState(Config.getShowRating());
        mBinding.setTitleState(Config.getShowTitle());
    }

}
