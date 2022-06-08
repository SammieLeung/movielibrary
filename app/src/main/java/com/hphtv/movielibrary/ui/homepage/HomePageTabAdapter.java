package com.hphtv.movielibrary.ui.homepage;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.ui.homepage.fragment.homepage.HomePageFragment;
import com.hphtv.movielibrary.ui.homepage.fragment.theme.ThemeFragment;
import com.hphtv.movielibrary.ui.homepage.fragment.unknow.UnknowFileFragment;
import com.hphtv.movielibrary.ui.homepage.genretag.IRefreshGenre;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/1/14
 */
public class HomePageTabAdapter extends FragmentPagerAdapter {
    protected List<Fragment> mList = new ArrayList<>();
    protected List<IRefreshGenre> mIRefreshGenreList=new ArrayList<>();

    public HomePageTabAdapter(IAutofitHeight autofitHeight, FragmentManager fm) {
        super(fm,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);


        HomePageFragment homePageFragment = HomePageFragment.newInstance(autofitHeight, 0);
        ThemeFragment movieThemeFragment = ThemeFragment.newInstance(autofitHeight, 1, Constants.SearchType.movie);
        ThemeFragment tvThemeFragment = ThemeFragment.newInstance(autofitHeight, 2, Constants.SearchType.tv);
        UnknowFileFragment unknowFileFragment = UnknowFileFragment.newInstance(autofitHeight, 3);

        mList.add(homePageFragment);
        mList.add(movieThemeFragment);
        mList.add(tvThemeFragment);
        mList.add(unknowFileFragment);

        mIRefreshGenreList.add(homePageFragment);
        mIRefreshGenreList.add(movieThemeFragment);
        mIRefreshGenreList.add(tvThemeFragment);

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

    public List<IRefreshGenre> getIRefreshGenreList() {
        return mIRefreshGenreList;
    }
}
