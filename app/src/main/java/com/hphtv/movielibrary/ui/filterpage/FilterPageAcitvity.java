package com.hphtv.movielibrary.ui.filterpage;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.adapter.NewMovieItemListAdapter;
import com.hphtv.movielibrary.databinding.ActivityFilterpageBinding;
import com.hphtv.movielibrary.ui.AppBaseActivity;

import java.util.ArrayList;

/**
 * author: Sam Leung
 * date:  2022/2/22
 */
public class FilterPageAcitvity extends AppBaseActivity<FilterPageViewModel, ActivityFilterpageBinding> {
    private NewMovieItemListAdapter mMovieItemListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView(){
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,5);
        mBinding.recyclerview.setLayoutManager(gridLayoutManager);
        mMovieItemListAdapter=new NewMovieItemListAdapter(this,new ArrayList<>());

    }
}
