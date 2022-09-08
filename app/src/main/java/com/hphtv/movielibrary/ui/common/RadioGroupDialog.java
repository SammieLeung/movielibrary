package com.hphtv.movielibrary.ui.common;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableInt;

import com.hphtv.movielibrary.databinding.CommonRadioDialogLayoutBinding;
import com.hphtv.movielibrary.databinding.CommonRadiogroupItemBinding;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/9/8
 */
public class RadioGroupDialog<VM extends RadioGroupDViewModel> extends BaseDialogFragment2<VM, CommonRadioDialogLayoutBinding> {
    public static final String TAG = RadioGroupDialog.class.getSimpleName();
    private List<String> mDatas;
    private ObservableInt mSelectPos;

    public RadioGroupDialog(List<String> datas) {
        mDatas = datas;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.setCheckPos(mSelectPos);
        int i = 0;
        for (String data : mDatas) {
            mBinding.radioGroup.addView(buildRadioButton(data, i, mViewModel.getCheckPos()),
                    i,
                    new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DensityUtil.dip2px(getContext(), 66)));
            i++;
        }
        mBinding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int pos = group.indexOfChild(group.findViewById(checkedId));
            mViewModel.getCheckPos().set(pos);
        });
    }

    public View buildRadioButton(String data, int pos, ObservableInt checkPos) {
        CommonRadiogroupItemBinding binding = CommonRadiogroupItemBinding.inflate(LayoutInflater.from(getContext()));
        binding.setTitle(data);
        binding.setPos(pos);
        binding.setCheckPos(checkPos);
        binding.getRoot().setId(View.generateViewId());
        return binding.getRoot();
    }


    @Override
    protected VM createViewModel() {
        return null;
    }

    public void setCheckPos(ObservableInt checkPos) {
        mSelectPos = checkPos;
    }

}
