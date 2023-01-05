package com.hphtv.movielibrary.ui.homepage;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.ui.homepage.fragment.homepage.HomePageFragment;
import com.hphtv.movielibrary.ui.homepage.fragment.theme.ThemeFragment;
import com.hphtv.movielibrary.ui.homepage.fragment.unknown.UnknownFileFragment;
import com.hphtv.movielibrary.ui.homepage.genretag.IRefreshGenre;
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
    public static final int UNKNOWN=5;
    protected List<Fragment> mList = new ArrayList<>(5);
    protected List<IRefreshGenre> mIRefreshGenreList=new ArrayList<>();

    public HomePageTabAdapter(NoScrollAutofitHeightViewPager viewPager, FragmentManager fm) {
        super(fm,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);


        HomePageFragment homePageFragment = HomePageFragment.newInstance(viewPager, HOME);
        ThemeFragment movieThemeFragment = ThemeFragment.newInstance(viewPager, MOVIE, Constants.VideoType.movie);
        ThemeFragment tvThemeFragment = ThemeFragment.newInstance(viewPager, TV, Constants.VideoType.tv);
        ThemeFragment childThemeFragment=ThemeFragment.newInstance(viewPager,CHILD,Constants.VideoType.child);
        ThemeFragment varietyShowThemeFragment=ThemeFragment.newInstance(viewPager,VARIETY_SHOW,Constants.VideoType.variety_show);
        UnknownFileFragment unknowFileFragment = UnknownFileFragment.newInstance(viewPager, UNKNOWN);

        mList.add(homePageFragment);
        mList.add(movieThemeFragment);
        mList.add(tvThemeFragment);
        mList.add(childThemeFragment);
        mList.add(varietyShowThemeFragment);
        mList.add(unknowFileFragment);

        mIRefreshGenreList.add(homePageFragment);
        mIRefreshGenreList.add(movieThemeFragment);
        mIRefreshGenreList.add(tvThemeFragment);
        mIRefreshGenreList.add(childThemeFragment);
        mIRefreshGenreList.add(varietyShowThemeFragment);

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

    public void updateUserFragments(){
        for (IRefreshGenre refreshGenre : mIRefreshGenreList) {
            refreshGenre.updateUserFavorite();
        }
    }

    public List<IRefreshGenre> getIRefreshGenreList() {
        return mIRefreshGenreList;
    }
}
