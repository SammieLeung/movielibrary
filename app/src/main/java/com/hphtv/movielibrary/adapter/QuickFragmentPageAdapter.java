package com.hphtv.movielibrary.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


import java.util.List;

/**
 * Created by tchip on 17-11-14.
 */

public class QuickFragmentPageAdapter<T extends Fragment> extends FragmentPagerAdapter {
    private List<T> mList;
    private String[] mTitles;

    /**
     * @param list
     * @param titles PageTitles
     */
    public QuickFragmentPageAdapter(FragmentManager fm,List<T> list, String[] titles) {
        super(fm);
        mList = list;
        mTitles = titles;
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
        return mTitles == null ? super.getPageTitle(position) : mTitles[position];
    }


}
