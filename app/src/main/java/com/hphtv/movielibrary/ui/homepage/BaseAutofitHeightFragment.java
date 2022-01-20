package com.hphtv.movielibrary.ui.homepage;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.ui.BaseFragment2;
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;

/**
 * author: Sam Leung
 * date:  2021/11/5
 */
public abstract class BaseAutofitHeightFragment<VM extends AndroidViewModel, VDB extends ViewDataBinding>  extends BaseFragment2<VM, VDB> implements IAutofitHeight{
    private NewHomePageActivity mNewHomePageActivity;
    public BaseAutofitHeightFragment(NewHomePageActivity activity){
        mNewHomePageActivity=activity;
    }

    @Override
    public NoScrollAutofitHeightViewPager getViewPager() {
        return mNewHomePageActivity.getViewPager();
    }
}
