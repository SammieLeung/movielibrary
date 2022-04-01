package com.hphtv.movielibrary.ui.homepage;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.hphtv.movielibrary.ui.homepage.fragment.homepage.HomePageFragment;

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
//        mNewPageFragment=NewPageFragment.newInstance(autofitHeight,0);
//        mNewPageFragment=NewPageFragment.newInstance(autofitHeight,1);

        mList.add(HomePageFragment.newInstance(autofitHeight,0));
//        mList.add(NewPageFragment.newInstance(autofitHeight,1));
//        mList.add(NewPageFragment.newInstance(autofitHeight,2));

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
