package com.hphtv.movielibrary.ui.settings.fragment.childmode;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.FragmentSettingsChildmodeBinding;
import com.hphtv.movielibrary.ui.BaseFragment2;
import com.hphtv.movielibrary.ui.settings.SettingsViewModel;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/3/15
 */
public class ChildModeFragment extends BaseFragment2<SettingsViewModel, FragmentSettingsChildmodeBinding> {

    public static ChildModeFragment newInstance() {
        ChildModeFragment fragment = new ChildModeFragment();
        return fragment;
    }

    @Override
    protected SettingsViewModel createViewModel() {
        return new ViewModelProvider(getActivity()).get(SettingsViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setChildmode(mViewModel.getChildModeState());
        mBinding.viewChildmode.setOnClickListener(mViewModel::toggleChildMode);
        mBinding.tvChangepsw.setOnClickListener(this::showChangePassword);
    }

    private void showChangePassword(View v){
        getParentFragmentManager().beginTransaction().addToBackStack(ChildModeFragment.class.getName())
                .replace(R.id.view_content,ChangePasswordFragment.newInstance())
                .commit();
    }
}
