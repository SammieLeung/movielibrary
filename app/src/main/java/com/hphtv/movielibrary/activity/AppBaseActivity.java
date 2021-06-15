package com.hphtv.movielibrary.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.firelfy.util.LogUtil;
import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.fragment.dialog.LoadingDialogFragment;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author lxp
 * @date 19-3-26
 */
public abstract class AppBaseActivity<VM extends AndroidViewModel, VDB extends ViewDataBinding> extends BaseActivity {
    protected VDB mBinding;
    protected VM mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, getContentViewId());
        mBinding.setLifecycleOwner(this);
        createAndroidViewModel();
        processLogic();
        init();
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


}
