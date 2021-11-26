package com.hphtv.movielibrary.fragment.newdialoag;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;


import com.station.kit.view.mvvm.ViewDataBindingHelper;
import com.station.kit.view.mvvm.ViewModelHelper;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * author: Sam Leung
 * date:  2021/8/31
 */
public abstract class BaseDialogFragment<VM extends AndroidViewModel,VDB extends ViewDataBinding> extends DialogFragment {
    protected VDB mBinding;
    protected VM mViewModel;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = ViewDataBindingHelper.inflateVDB(getContext());
        mViewModel= ViewModelHelper.createAndroidViewModel(getContext(),this);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setLifecycleOwner(this);
        createAndroidViewModel();

    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onResume() {
        super.onResume();
    }



    private void createAndroidViewModel() {
        if (mViewModel == null) {
            Class modelClass;
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                modelClass = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                //如果没有指定泛型参数，则默认使用BaseViewModel
                modelClass = AndroidViewModel.class;
            }
            mViewModel = (VM) new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()).create(modelClass);
        }
    }

}
