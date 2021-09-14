package com.hphtv.movielibrary.fragment.dialog;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.activity.AppBaseActivity;

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

    private ActivityResultLauncher mActivityResultLauncher;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setLifecycleOwner(this);
        createAndroidViewModel();
        onViewCreated();
        registerForActivityResult();

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

    private void registerForActivityResult() {
        ActivityResultContracts.StartActivityForResult startActivityForResult = new ActivityResultContracts.StartActivityForResult();
        mActivityResultLauncher = registerForActivityResult(startActivityForResult, result -> {
            Log.v(BaseDialogFragment.this.getClass().getSimpleName(), "onActivityResult resultCode=" + result.getResultCode());
            onActivityResultCallback(result);
        });
    }


    protected abstract int getLayoutId();
    /**
     * 处理onCreate()
     */
    protected abstract void onViewCreated();

    protected void onActivityResultCallback(ActivityResult result) {
    }

    //以前的startActivityForResult
    protected void startActivityForResult(Intent intent) {
        mActivityResultLauncher.launch(intent);
    }

    protected void startActivityForResultFromParent(Intent intent) {
        if(getAppBaseActivity()!=null)
            getAppBaseActivity().startActivityForResult(intent);
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


    private AppBaseActivity getAppBaseActivity() {
        if (getActivity() instanceof AppBaseActivity) {
            return (AppBaseActivity) getActivity();
        }
        return null;
    }
}
