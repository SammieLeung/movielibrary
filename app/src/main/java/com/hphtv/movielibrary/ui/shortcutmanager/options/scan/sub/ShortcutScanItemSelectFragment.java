package com.hphtv.movielibrary.ui.shortcutmanager.options.scan.sub;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.hphtv.movielibrary.databinding.ShortcutScanSubDialogItemBinding;
import com.hphtv.movielibrary.databinding.ShortcutScanSubDialogLayout1Binding;
import com.hphtv.movielibrary.ui.BaseFragment2;
import com.hphtv.movielibrary.ui.shortcutmanager.bean.ShortcutOptionsItem;
import com.hphtv.movielibrary.ui.shortcutmanager.options.ShortcutOptionsViewModel;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/1/4
 */
public class ShortcutScanItemSelectFragment extends BaseFragment2<ShortcutOptionsViewModel, ShortcutScanSubDialogLayout1Binding> {
    public static final String TAG = ShortcutScanItemSelectFragment.class.getSimpleName();
    private ViewModelStoreOwner mContext;
    private ShortcutOptionsItem mShortcutOptionsItem;

    public static ShortcutScanItemSelectFragment newInstance(ViewModelStoreOwner context) {
        Bundle args = new Bundle();
        ShortcutScanItemSelectFragment fragment = new ShortcutScanItemSelectFragment();
        fragment.mContext = context;
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setItem(mShortcutOptionsItem);
        mBinding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Log.d(TAG, "onCheckedChanged: " + checkedId);
            View v = group.findViewById(checkedId);
            v.requestFocus();
            int pos = group.indexOfChild(v);
            mShortcutOptionsItem.setPos(pos);
        });
        updateRadioButtons();
    }

    @Override
    protected ShortcutOptionsViewModel createViewModel() {
        return new ViewModelProvider(mContext).get(ShortcutOptionsViewModel.class);
    }

    public void setItem(ShortcutOptionsItem item) {
        mShortcutOptionsItem = item;
    }

    private void updateRadioButtons() {
        for (String text : mShortcutOptionsItem.getOptionList()) {
            newRadioButton(text).setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction().remove(this).commit();
                mViewModel.getShowSubDialogFlag().set(false);
            });
        }
    }

    private RadioButton newRadioButton(String text) {
        ShortcutScanSubDialogItemBinding databinding = ShortcutScanSubDialogItemBinding.inflate(getLayoutInflater(), mBinding.radioGroup, true);
        databinding.setText(text);
        databinding.getRoot().setId(View.generateViewId());
        return (RadioButton) databinding.getRoot();
    }

    @BindingAdapter("bindCheckPos")
    public static void bindCheckPos(RadioGroup radioGroup, int pos) {
        if (pos >= 0 && pos < radioGroup.getChildCount()) {
            if (radioGroup.getCheckedRadioButtonId() != radioGroup.getChildAt(pos).getId())
                radioGroup.check(radioGroup.getChildAt(pos).getId());
        }
    }
}
