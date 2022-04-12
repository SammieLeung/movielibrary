package com.hphtv.movielibrary.ui.settings.fragment.childmode;

import static com.hphtv.movielibrary.ui.settings.SettingsViewModel.CONFIRM_PSW_ERROR;
import static com.hphtv.movielibrary.ui.settings.SettingsViewModel.NEW_PSW_ERROR;
import static com.hphtv.movielibrary.ui.settings.SettingsViewModel.OK;
import static com.hphtv.movielibrary.ui.settings.SettingsViewModel.PSW_ERROR;
import static com.hphtv.movielibrary.ui.settings.SettingsViewModel.PSW_NOT_CHANGE;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.NextFocusModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.databinding.FragmentSettingsChildmodeChangepwdBinding;
import com.hphtv.movielibrary.ui.BaseFragment2;
import com.hphtv.movielibrary.ui.settings.SettingsViewModel;
import com.station.kit.util.ToastUtil;

import org.jetbrains.annotations.NotNull;


/**
 * author: Sam Leung
 * date:  2022/3/16
 */
public class ChangePasswordFragment extends BaseFragment2<SettingsViewModel, FragmentSettingsChildmodeChangepwdBinding> {

    public static ChangePasswordFragment newInstance() {
        ChangePasswordFragment fragment = new ChangePasswordFragment();
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

    private void initView() {
        mBinding.includePwd.etPsw.requestFocus();
        mBinding.includeConfirmPwd.etPsw.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                mBinding.btnConfirm.performClick();
                return true;
            }
            return false;
        });
        mBinding.btnConfirm.setOnClickListener(this::changePassword);
        mBinding.btnCancel.setOnClickListener(this::fallback);
    }

    private void bindDatas(){
        updateOldHint();
        NextFocusModel model=new NextFocusModel();
        model.setNextFocusLeft(R.id.btn_confirm);
        mBinding.setNextFocus(model);
    }

    private void updateOldHint(){
        if(Config.getChildModePassword().equals(Config.DEAULT_PASSWORD)){
            mBinding.setOldHint(getString(R.string.hint_enter_psw_2));
        }
    }

    private void changePassword(View v) {
        int flag = mViewModel.checkPasswords(mBinding.includePwd.etPsw.getText().toString(),
                mBinding.includeNewPwd.etPsw.getText().toString(),
                mBinding.includeConfirmPwd.etPsw.getText().toString());
        if (flag == OK) {
            mViewModel.submitPassword(mBinding.includeNewPwd.etPsw.getText().toString());
            ToastUtil.newInstance(getContext()).toast(getString(R.string.psw_set_success));
            fallback(v);
        } else {
            if ((flag & PSW_ERROR) == PSW_ERROR)
                mBinding.includePwd.etPsw.setError(getString(R.string.errtips_psw_error));
            if ((flag & PSW_NOT_CHANGE) == PSW_NOT_CHANGE)
                mBinding.includeNewPwd.etPsw.setError(getString(R.string.errtips_psw_not_new));
            if ((flag & NEW_PSW_ERROR) == NEW_PSW_ERROR)
                mBinding.includeNewPwd.etPsw.setError(getString(R.string.errtips_psw_invaild));
            if ((flag & CONFIRM_PSW_ERROR) == CONFIRM_PSW_ERROR)
                mBinding.includeConfirmPwd.etPsw.setError(getString(R.string.errtips_psw_inconsistent));
        }
    }

    private void fallback(View v) {
        getParentFragmentManager().popBackStack();
    }
}
