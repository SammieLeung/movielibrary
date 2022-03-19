package com.hphtv.movielibrary.ui.settings;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.ComponentPasswordDialogFragmentBinding;
import com.hphtv.movielibrary.databinding.PswEdittextItemBinding;
import com.hphtv.movielibrary.ui.BaseDialogFragment;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.jakewharton.rxbinding4.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

import static com.hphtv.movielibrary.ui.settings.PasswordDialogFragmentViewModel.IS_NOT_VAILD;
import static com.hphtv.movielibrary.ui.settings.PasswordDialogFragmentViewModel.NOT_A_NEW_PASSWORD;
import static com.hphtv.movielibrary.ui.settings.PasswordDialogFragmentViewModel.PASSWORD_ERROR;
import static com.hphtv.movielibrary.ui.settings.PasswordDialogFragmentViewModel.PASSWORD_INCONSISTENT;

/**
 * Created by tchip on 18-3-20.
 */

public class PasswordDialogFragment extends BaseDialogFragment<PasswordDialogFragmentViewModel, ComponentPasswordDialogFragmentBinding> implements View.OnClickListener {
    public static final String TAG = PasswordDialogFragment.class.getSimpleName();
    public static final int ENTER_PASSWORD_DIALOG = 1;
    public static final int SET_INITAL_PASSWORD = 2;
    public static final int CHANGE_PASSWORD = 3;
    private int mDialogType = ENTER_PASSWORD_DIALOG;
    private List<EditText> mEditTextList;
    private List<Integer> mHintStringResList;
    private int mTitleRes;
    private List<Integer> mErrorMsgResList;

    public static PasswordDialogFragment newInstance() {
        Bundle args = new Bundle();
        PasswordDialogFragment fragment = new PasswordDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onViewCreated() {
        prepareErrorTips();
        prepareTitleAndHints();
        mBinding.tvTitle.setText(getContext().getString(mTitleRes));
        List<Observable<CharSequence>> observableList = new ArrayList<>();
        for (int i = 0; i < mHintStringResList.size(); i++) {
            PswEdittextItemBinding pswBinding = PswEdittextItemBinding.inflate(getLayoutInflater(), mBinding.viewEdittextGroup, false);
            pswBinding.editText.setHint(mHintStringResList.get(i));
            Observable<CharSequence> observable = RxTextView.textChanges(pswBinding.editText).skip(1);
            mBinding.viewEdittextGroup.addView(pswBinding.editText);
            mEditTextList.add(pswBinding.editText);
            observableList.add(observable);
        }
        mBinding.btnYes.setOnClickListener(this);
        mBinding.btnNo.setOnClickListener(this);
        mViewModel.verifyPasswords(observableList, new SimpleObserver<Integer>() {
            @Override
            public void onAction(Integer flag) {
                if ((flag & IS_NOT_VAILD) == IS_NOT_VAILD) {
                    passwordInvaild();
                } else if ((flag & PASSWORD_ERROR) == PASSWORD_ERROR) {
                    passwordError();
                } else if ((flag & NOT_A_NEW_PASSWORD) == NOT_A_NEW_PASSWORD) {
                    passwordNotNew();
                } else if ((flag & PASSWORD_INCONSISTENT) == PASSWORD_INCONSISTENT) {
                    passwordInconsistent();
                } else {
                    passwordPassed();
                }
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(getResources().getDimensionPixelOffset(R.dimen.dialogfragment_w), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.component_password_dialog_fragment;
    }

    @Override
    public void dismiss() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
        super.dismiss();
    }

    public void initSharePreferenceSavePasswordKey(String sharePreferenceSavePasswordKey) {
        mViewModel.initSharePreferenceSavePasswordKey(sharePreferenceSavePasswordKey);
    }

    public PasswordDialogFragment newEnterPassword() {
        mDialogType = ENTER_PASSWORD_DIALOG;
        return this;
    }

    public PasswordDialogFragment newSetInitialPassword() {
        mDialogType = SET_INITAL_PASSWORD;
        return this;
    }

    public PasswordDialogFragment newChangePassword() {
        mDialogType = CHANGE_PASSWORD;
        return this;
    }

    private void prepareTitleAndHints() {
        switch (mDialogType) {
            case ENTER_PASSWORD_DIALOG:
                if (mViewModel.isPasswordEmpty()) {
                    mDialogType = SET_INITAL_PASSWORD;
                    prepareTitleAndHints();
                } else {
                    setTitle(R.string.title_input_psw)
                            .addHint(R.string.hint_enter_psw);
                }
                break;
            case SET_INITAL_PASSWORD:
                setTitle(R.string.title_set_initial_psw)
                        .addHint(R.string.hint_input4digit_psw)
                        .addHint(R.string.hint_confirm_new_psw);
                break;
            case CHANGE_PASSWORD:
                if(mViewModel.isPasswordEmpty()){
                    mDialogType = SET_INITAL_PASSWORD;
                    prepareTitleAndHints();
                }else{
                    setTitle(R.string.title_change_psw)
                            .addHint(R.string.hint_enter_psw)
                            .addHint(R.string.hint_input4digit_psw)
                            .addHint(R.string.hint_confirm_new_psw);
                }
                break;
        }
    }

    private PasswordDialogFragment prepareErrorTips() {
        if (mErrorMsgResList == null) {
            mErrorMsgResList = new ArrayList<>();
        }
        mErrorMsgResList.add(R.string.errtips_psw_invaild);
        mErrorMsgResList.add(R.string.errtips_psw_error);
        mErrorMsgResList.add(R.string.errtips_psw_not_new);
        mErrorMsgResList.add(R.string.errtips_psw_inconsistent);
        return this;
    }

    private void passwordInvaild() {
        showTips(0);

    }

    private void passwordError() {
        showTips(1);
    }

    private void passwordNotNew() {
        showTips(2);

    }

    private void passwordInconsistent() {
        showTips(3);
    }

    private void passwordPassed() {
        hideTips();
    }

    public PasswordDialogFragment addHint(int hint) {
        if (mHintStringResList == null) {
            mHintStringResList = new ArrayList<>();
            mEditTextList = new ArrayList<>();
        }
        mHintStringResList.add(hint);
        return this;
    }


    public PasswordDialogFragment setTitle(int res) {
        this.mTitleRes = res;
        return this;
    }

    public void showTips(int i) {
        if (mErrorMsgResList != null && i < mErrorMsgResList.size()) {
            mBinding.tvTips.setText(mErrorMsgResList.get(i));
        }
        mBinding.btnYes.setEnabled(false);
    }

    public void hideTips() {
        try {
            mBinding.tvTips.setText(null);
            mBinding.btnYes.setEnabled(true);
        } catch (Exception e) {

        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                if (mOnClickListener != null) {
                    int count = mEditTextList.size();
                    switch (count) {
                        case 1:
                            mOnClickListener.isVaildPassword();
                            break;
                        case 2:
                            mViewModel.setPassword(mEditTextList.get(0).getText().toString());
                            mOnClickListener.updatePassword(mEditTextList.get(0).getText().toString());
                            break;
                        case 3:
                            mViewModel.setPassword(mEditTextList.get(0).getText().toString());
                            mOnClickListener.updatePassword(mEditTextList.get(1).getText().toString());
                            break;
                    }

                }
                break;
            case R.id.btn_no:
                if (mOnClickListener != null)
                    mOnClickListener.onCancel();
                break;
        }
        dismiss();
    }

    public interface OnClickListener {
        void isVaildPassword();

        void updatePassword(String text);

        void onCancel();
    }

    private OnClickListener mOnClickListener;

    public void setOnClickListener(OnClickListener listener) {
        this.mOnClickListener = listener;
    }
}
