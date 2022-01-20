package com.hphtv.movielibrary.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by tchip on 17-11-14.
 */

public class QuickFragmentPageAdapter<T extends Fragment> extends FragmentPagerAdapter {
    protected List<T> mList=new ArrayList<>();
    protected List<String> mTitleList=new ArrayList<>();

    public QuickFragmentPageAdapter(FragmentManager fm){
        super(fm);
    }

    public void addAllFragments(List list){
        mList.clear();
        mList.addAll(list);
    }

    public void addAllTitles(List list){
        mTitleList.clear();
        mTitleList.addAll(list);
    }
    @Override
    public Fragment getItem(int position) {
        return mList.get(position);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitleList == null ? super.getPageTitle(position) : mTitleList.get(position);
    }


}
