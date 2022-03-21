package com.hphtv.movielibrary.ui.settings.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.databinding.FragmentSettingsPreferenceBinding;
import com.hphtv.movielibrary.ui.BaseFragment2;
import com.hphtv.movielibrary.ui.settings.SettingsViewModel;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/3/19
 */
public class PreferenceFragment extends BaseFragment2<SettingsViewModel, FragmentSettingsPreferenceBinding> {

    public static PreferenceFragment newInstance() {
        PreferenceFragment fragment = new PreferenceFragment();
        return fragment;
    }

    @Override
    protected SettingsViewModel createViewModel() {
        return new ViewModelProvider(getActivity()).get(SettingsViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindDatas();
        initView();
    }

    private void bindDatas(){
        mViewModel.getDeafutSearchModeString();
        mBinding.setStateName(mViewModel.getDefaultSearchMode());
    }
    private void initView(){

        mBinding.viewAutosearch.view.setOnClickListener(mViewModel::toggleAutoSearchState);
        mBinding.viewDefaultSearchMode.view.setOnClickListener(mViewModel::changeDefaultSearchState);
    }


}
