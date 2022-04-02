package com.hphtv.movielibrary.ui.homepage.fragment.unknow;

import android.os.Bundle;

import com.hphtv.movielibrary.databinding.FragmentUnknowfileBinding;
import com.hphtv.movielibrary.ui.homepage.BaseAutofitHeightFragment;
import com.hphtv.movielibrary.ui.homepage.IAutofitHeight;

/**
 * author: Sam Leung
 * date:  2022/4/2
 */
public class UnknowFileFragment  extends BaseAutofitHeightFragment<UnknowFileViewModel, FragmentUnknowfileBinding> {

    public UnknowFileFragment(IAutofitHeight autofitHeight, int position) {
        super(autofitHeight, position);
    }

    public static UnknowFileFragment newInstance(IAutofitHeight autofitHeight, int positon) {
        Bundle args = new Bundle();
        UnknowFileFragment fragment = new UnknowFileFragment(autofitHeight, positon);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected UnknowFileViewModel createViewModel() {
        return null;
    }

    @Override
    public void forceRefresh() {

    }
}
