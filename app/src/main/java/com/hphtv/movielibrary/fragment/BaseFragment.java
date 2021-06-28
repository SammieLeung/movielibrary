package com.hphtv.movielibrary.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.MovieLibraryAdapter;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by tchip on 18-5-25.
 */

public abstract class BaseFragment<VM extends AndroidViewModel, VDB extends ViewDataBinding> extends Fragment {
    protected VDB mBinding;
    protected VM mViewModel;


    private MovieLibraryAdapter mAdapter;// 电影列表适配器
    private static final int COLUMS = 8;
    private Handler mHandler = new Handler(Looper.getMainLooper());


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding= DataBindingUtil.inflate(inflater,R.layout.f_layout_favorite,container,false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.setLifecycleOwner(this);
        createAndroidViewModel();
        onViewCreated();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    /**
     * 处理onCreate()
     */
    protected abstract void onViewCreated();

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
            mViewModel = (VM)   new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()).create(modelClass);

        }
    }


}
