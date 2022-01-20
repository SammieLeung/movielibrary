package com.hphtv.movielibrary.ui.homepage;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.QuickFragmentPageAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2022/1/14
 */
public class NewHomePageTabAdapter extends FragmentStatePagerAdapter {
    private NewPageFragment mNewPageFragment;
    protected List<Fragment> mList=new ArrayList<>();
    public NewHomePageTabAdapter(Context context, FragmentManager fm) {
        super(fm);
        mNewPageFragment=NewPageFragment.newInstance((NewHomePageActivity) context,0);

        mList.add(mNewPageFragment);

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
}
