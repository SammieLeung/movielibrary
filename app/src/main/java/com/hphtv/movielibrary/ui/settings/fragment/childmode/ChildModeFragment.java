package com.hphtv.movielibrary.ui.settings.fragment.childmode;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.NextFocusModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.FragmentSettingsChildmodeBinding;
import com.hphtv.movielibrary.ui.BaseFragment2;
import com.hphtv.movielibrary.ui.settings.PasswordDialogFragment;
import com.hphtv.movielibrary.ui.settings.PasswordDialogFragmentViewModel;
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
        initView();
        bindDatas();

    }
    private void initView(){
        mBinding.viewChildmode.view.setOnClickListener(this::toggleChildMode);
        mBinding.tvChangepsw.setOnClickListener(this::showChangePassword);
        getParentFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (ChildModeFragment.this.isVisible()) {
                    //当前fragment可见时(如果是界面的跟新在onstart或onresume就好了,isVisible()方法更大的用处是在于可以做些操作避免当前Fragment不被重复加入返回栈).....
                    mBinding.tvChangepsw.requestFocus();
                }
            }
        });
    }
    private void bindDatas(){
        NextFocusModel model=new NextFocusModel();
        model.setNextFocusLeft(R.id.tab_childmode);
        mBinding.setNextFocus(model);
        mBinding.setChildmode(mViewModel.getChildModeState());
    }

    private void showChangePassword(View v) {
        getParentFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .addToBackStack(ChildModeFragment.class.getName())
                .replace(R.id.view_content, ChangePasswordFragment.newInstance())
                .commit();
    }

    private void toggleChildMode(View v) {
        if (mViewModel.getChildModeState().get()) {
            showPasswordDialog();
        } else {
            mViewModel.toggleChildMode(v);
        }
    }

    private void showPasswordDialog() {
        PasswordDialogFragmentViewModel viewModel = new ViewModelProvider(this).get(PasswordDialogFragmentViewModel.class);
        PasswordDialogFragment passwordDialogFragment=PasswordDialogFragment.newInstance();
        passwordDialogFragment.setOnConfirmListener(() -> mViewModel.toggleChildMode(null));
        passwordDialogFragment.setViewModel(viewModel);
        passwordDialogFragment.show(getChildFragmentManager(),"");
    }
}
