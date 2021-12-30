package com.hphtv.movielibrary.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by tchip on 18-5-25.
 */

public abstract class BaseFragment<VM extends AndroidViewModel, VDB extends ViewDataBinding> extends Fragment {
    protected VDB mBinding;
    protected VM mViewModel;
    protected int mColums = 6;

    protected Handler mHandler = new Handler(Looper.getMainLooper());
    private ActivityResultLauncher mActivityResultLauncher;
    protected int mPosition = 0;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mPosition = bundle.getInt(Constants.Extras.CURRENT_FRAGMENT, 0);
        }
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
    public void onResume() {
        super.onResume();
    }

    private void registerForActivityResult() {
        ActivityResultContracts.StartActivityForResult startActivityForResult = new ActivityResultContracts.StartActivityForResult();
        mActivityResultLauncher = registerForActivityResult(startActivityForResult, result -> {
            Log.v(BaseFragment.this.getClass().getSimpleName(), "onActivityResult resultCode=" + result.getResultCode());
            onActivityResultCallback(result);
        });
    }


    protected int getLayoutId(){
        return R.layout.f_layout_movie;
    }
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

    protected void notifyStartLoading() {
        Intent intent = new Intent();
        intent.setAction(Constants.BroadCastMsg.START_LOADING);
        intent.putExtra(Constants.Extras.CURRENT_FRAGMENT, mPosition);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    protected void notifyStopLoading() {
        getAppBaseActivity().stopLoading();
//        Intent intent = new Intent();
//        intent.setAction(ConstData.BroadCastMsg.STOP_LOADING);
//        intent.putExtra(ConstData.IntentKey.KEY_CUR_FRAGMENT, mPosition);
//        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    protected AppBaseActivity getAppBaseActivity() {
        if (getActivity() instanceof AppBaseActivity) {
            return (AppBaseActivity) getActivity();
        }
        return null;
    }
}
