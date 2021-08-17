package com.hphtv.movielibrary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.fragment.BaseFragment;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author lxp
 * @date 19-3-26
 */
public abstract class AppBaseActivity<VM extends AndroidViewModel, VDB extends ViewDataBinding> extends BaseActivity {
    protected VDB mBinding;
    protected VM mViewModel;
    private ActivityResultLauncher mActivityResultLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, getContentViewId());
        mBinding.setLifecycleOwner(this);
        createAndroidViewModel();
        processLogic();
        init();
        ActivityResultContracts.StartActivityForResult startActivityForResult = new ActivityResultContracts.StartActivityForResult();
        mActivityResultLauncher = registerForActivityResult(startActivityForResult, result -> {
            Log.v(AppBaseActivity.this.getClass().getSimpleName(), "onActivityResult resultCode=" + result.getResultCode());
           onActivityResultCallback(result);
        });
    }

    /**
     * 返回layout
     *
     * @return
     */
    protected abstract int getContentViewId();

    /**
     * 处理onCreate()
     */
    protected abstract void processLogic();
    protected void onActivityResultCallback(ActivityResult result){

    };
    public void startActivityForResult(Intent intent){
        mActivityResultLauncher.launch(intent);
    }
    /**
     * 创建ViewModel
     */
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
            mViewModel = (VM)   new ViewModelProvider.AndroidViewModelFactory(getApplication()).create(modelClass);

        }
    }

    /**
     * 初始化
     */
    private void init() {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public MovieApplication getApp() {
        return MovieApplication.getInstance();
    }

    public VM getViewModel() {
        return mViewModel;
    }
}
