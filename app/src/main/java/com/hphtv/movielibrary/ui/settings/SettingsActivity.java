package com.hphtv.movielibrary.ui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.Observable;

import com.hphtv.movielibrary.databinding.ActivitySettingsBinding;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.settings.fragment.childmode.ChildModeFragment;
import com.hphtv.movielibrary.ui.settings.fragment.poster.PosterFragment;
import com.hphtv.movielibrary.ui.settings.fragment.preference.PreferenceFragment;

/**
 * author: Sam Leung
 * date:  2022/3/14
 */
public class SettingsActivity extends AppBaseActivity<SettingsViewModel, ActivitySettingsBinding> implements View.OnHoverListener {
    private ChildModeFragment mChildModeFragment;
    private PosterFragment mPosterFragment;
    private PreferenceFragment mPreferenceFragment;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFragments();
        initView();
    }

    private void initFragments(){
        mChildModeFragment=ChildModeFragment.newInstance();
        mPosterFragment=PosterFragment.newInstance();
        mPreferenceFragment=PreferenceFragment.newInstance();
    }

    private void initView() {

        mBinding.tabChildmode.view.setOnClickListener(this::childMode);
        mBinding.tabPoster.view.setOnClickListener(this::poster);
        mBinding.tabPreference.view.setOnClickListener(this::preference);
        mBinding.tabAbout.view.setOnClickListener(this::about);
        mBinding.btnExit.setOnClickListener(v->finish());
        mBinding.setSelectPos(mViewModel.getSelectPos());
//        requestFocusOnHover(
//                mBinding.tabChildmode.view,
//                mBinding.tabPoster.view,
//                mBinding.tabPreference.view,
//                mBinding.tabAbout.view);

        mBinding.tabChildmode.view.performClick();
        mBinding.tabChildmode.view.requestFocus();
        mViewModel.getFlagRefresh().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if(mViewModel.getFlagRefresh().get())
                    setResult(Activity.RESULT_OK);
            }
        });
    }

    private void childMode(View view) {
        mViewModel.getSelectPos().set(0);
        getSupportFragmentManager().beginTransaction().replace(mBinding.viewContent.getId(),mChildModeFragment).commit();
    }

    private void poster(View view){
        mViewModel.getSelectPos().set(1);
        getSupportFragmentManager().beginTransaction().replace(mBinding.viewContent.getId(),mPosterFragment).commit();
    }

    private void preference(View view){
        mViewModel.getSelectPos().set(2);
        getSupportFragmentManager().beginTransaction().replace(mBinding.viewContent.getId(),mPreferenceFragment).commit();
    }

    private void about(View view){
        mViewModel.getSelectPos().set(3);
    }

    private void requestFocusOnHover(View... views) {
        for (View v : views)
            v.setOnHoverListener(this);
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
            if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                v.requestFocus();
            }
        }
        return false;
    }


}
