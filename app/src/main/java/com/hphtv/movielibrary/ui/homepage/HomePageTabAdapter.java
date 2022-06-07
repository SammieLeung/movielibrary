package com.hphtv.movielibrary.ui.homepage;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.ui.homepage.fragment.homepage.HomePageFragment;
import com.hphtv.movielibrary.ui.homepage.fragment.theme.ThemeFragment;
import com.hphtv.movielibrary.ui.homepage.fragment.unknow.UnknowFileFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/1/14
 */
public class HomePageTabAdapter extends FragmentStatePagerAdapter {
    protected List<Fragment> mList=new ArrayList<>();
    public HomePageTabAdapter(IAutofitHeight autofitHeight, FragmentManager fm) {
        super(fm);

        mList.add(HomePageFragment.newInstance(autofitHeight,0));
        mList.add(ThemeFragment.newInstance(autofitHeight,1, Constants.SearchType.movie));
        mList.add(ThemeFragment.newInstance(autofitHeight,2, Constants.SearchType.tv));
        mList.add(UnknowFileFragment.newInstance(autofitHeight,3));

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

    public void refreshFragments(){
        for(Fragment fragment:mList){
            if(fragment instanceof IActivityResult){
                IActivityResult activityResult= (IActivityResult) fragment;
                activityResult.forceRefresh();
            }
        }
    }
}
