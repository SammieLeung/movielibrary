package com.hphtv.movielibrary.ui.homepage.fragment.homepage;

import android.os.Bundle;

import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment;
import com.hphtv.movielibrary.ui.homepage.fragment.BaseHomeFragment;
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;

/**
 * author: Sam Leung
 * date:  2021/11/5
 */
public class HomePageFragment extends BaseHomeFragment<HomeFragmentViewModel> {

    public HomePageFragment() {
        super();
    }

    public static HomePageFragment newInstance(NoScrollAutofitHeightViewPager viewPager, int position) {
        Bundle args = new Bundle();
        HomePageFragment fragment = new HomePageFragment();
        args.putInt(BaseAutofitHeightFragment.POSITION, position);
        fragment.setArguments(args);
        fragment.setAutoFitHeightViewPager(viewPager);
        return fragment;
    }

    @Override
    protected String getVideoTagName() {
        return null;
    }
}
