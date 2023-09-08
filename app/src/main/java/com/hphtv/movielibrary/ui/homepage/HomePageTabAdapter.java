package com.hphtv.movielibrary.ui.homepage;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.ui.homepage.fragment.allfile.AllFileFragment;
import com.hphtv.movielibrary.ui.homepage.fragment.filter.FilterFragment;
import com.hphtv.movielibrary.ui.homepage.fragment.homepage.HomePageFragment;
import com.hphtv.movielibrary.ui.homepage.fragment.unknown.UnknownFileFragment;
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/1/14
 */
public class HomePageTabAdapter extends FragmentPagerAdapter {
    public static final int HOME=0;
    public static final int MOVIE=1;
    public static final int TV=2;
    public static final int CHILD=3;
    public static final int VARIETY_SHOW=4;
    public static final int ALL_FILE =5;
    protected List<Fragment> mList = new ArrayList<>(5);

    public HomePageTabAdapter(NoScrollAutofitHeightViewPager viewPager, FragmentManager fm) {
        super(fm,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        mList.add( HomePageFragment.newInstance(viewPager, HOME));
        mList.add(FilterFragment.newInstance(Constants.VideoType.movie,viewPager, MOVIE));
        mList.add(FilterFragment.newInstance(Constants.VideoType.tv,viewPager, TV));
        mList.add(FilterFragment.newInstance(Constants.VideoType.child,viewPager, CHILD));
        mList.add(FilterFragment.newInstance(Constants.VideoType.variety_show,viewPager, VARIETY_SHOW));
        mList.add(AllFileFragment.newInstance(viewPager, ALL_FILE));

    }

    @NonNull
    @NotNull
    @Override
    public Fragment getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    public void refreshFragments() {
        for (Fragment fragment : mList) {
            if (fragment instanceof IActivityResult) {
                IActivityResult activityResult = (IActivityResult) fragment;
                activityResult.forceRefresh();
            }
        }
    }


}
