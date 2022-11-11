package com.hphtv.movielibrary.ui.settings.fragment;

import android.app.Application;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.NextFocusModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.FragmentSettingsPreferenceBinding;
import com.hphtv.movielibrary.ui.BaseFragment2;
import com.hphtv.movielibrary.ui.common.RadioGroupDViewModel;
import com.hphtv.movielibrary.ui.common.RadioGroupDialog;
import com.hphtv.movielibrary.ui.settings.SettingsViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    private void bindDatas() {
        mViewModel.readDefaultSearchModeString();
        mBinding.setStateName(mViewModel.getDefaultSearchMode());
        mBinding.setPlayerName(mViewModel.getPlayerName());
        mBinding.setRecentlyVideoAction(mViewModel.getRecentlyVideoAction());
        NextFocusModel model = new NextFocusModel();
        model.setNextFocusLeft(R.id.tab_preference);
        mBinding.setNextFocus(model);
    }

    private void initView() {

        mBinding.viewAutosearch.view.setOnClickListener(mViewModel::toggleAutoSearchState);
        mBinding.viewDefaultSearchMode.view.setOnClickListener(mViewModel::changeDefaultSearchState);
        mBinding.viewDefaultRecentlyVideoAction.view.setOnClickListener(mViewModel::changeRecentlyVideoAction);
        mBinding.viewDefalutPlayer.view.setOnClickListener(v -> {
            SelectPlayerDialog dialog=new SelectPlayerDialog(mViewModel.getPlayerNames());
            dialog.setCheckPos(mViewModel.getSelectPlayerPos());
            dialog.show(getChildFragmentManager(),"");
        });
    }

    public static class SelectPlayerViewModel extends RadioGroupDViewModel {
        public SelectPlayerViewModel(@NonNull @NotNull Application application) {
            super(application);
        }
    }

    public static class SelectPlayerDialog extends RadioGroupDialog<SelectPlayerViewModel>{

        public SelectPlayerDialog(List<String> datas) {
            super(datas);
        }
    }


}
