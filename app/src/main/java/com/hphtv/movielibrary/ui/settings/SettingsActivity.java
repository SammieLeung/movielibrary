package com.hphtv.movielibrary.ui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;
import androidx.databinding.Observable;
import androidx.fragment.app.FragmentManager;

import com.hphtv.movielibrary.databinding.ActivitySettingsBinding;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.settings.fragment.AboutFragment;
import com.hphtv.movielibrary.ui.settings.fragment.childmode.ChildModeFragment;
import com.hphtv.movielibrary.ui.settings.fragment.PosterFragment;
import com.hphtv.movielibrary.ui.settings.fragment.PreferenceFragment;

/**
 * author: Sam Leung
 * date:  2022/3/14
 */
public class SettingsActivity extends AppBaseActivity<SettingsViewModel, ActivitySettingsBinding> implements View.OnHoverListener {
    private ChildModeFragment mChildModeFragment;
    private PosterFragment mPosterFragment;
    private PreferenceFragment mPreferenceFragment;
    private AboutFragment mAboutFragment;
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
        mAboutFragment=AboutFragment.newInstance();
    }

    private void initView() {

        mBinding.tabChildmode.view.setOnClickListener(this::childMode);
        mBinding.tabPoster.view.setOnClickListener(this::poster);
        mBinding.tabPreference.view.setOnClickListener(this::preference);
        mBinding.tabAbout.view.setOnClickListener(this::about);
        mBinding.btnExit.setOnClickListener(v->finish());
        mBinding.setSelectPos(mViewModel.getSelectPos());
        mBinding.tabChildmode.view.setOnFocusChangeListener((v, hasFocus) -> childMode(v));
        mBinding.tabPoster.view.setOnFocusChangeListener((v, hasFocus) -> poster(v));
        mBinding.tabPreference.view.setOnFocusChangeListener((v, hasFocus) -> preference(v));
        mBinding.tabAbout.view.setOnFocusChangeListener((v, hasFocus) -> about(v));

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

    /**
     * 儿童模式
     * @param view
     */
    private void childMode(View view) {
        mViewModel.getSelectPos().set(0);
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction().replace(mBinding.viewContent.getId(),mChildModeFragment).commit();
    }

    /**
     * 海报设置
     * @param view
     */
    private void poster(View view){
        mViewModel.getSelectPos().set(1);
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction().replace(mBinding.viewContent.getId(),mPosterFragment).commit();
    }

    /**
     * 偏好设置
     * @param view
     */
    private void preference(View view){
        mViewModel.getSelectPos().set(2);
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction().replace(mBinding.viewContent.getId(),mPreferenceFragment).commit();
    }

    /**
     * 关于
     * @param view
     */
    private void about(View view){
        mViewModel.getSelectPos().set(3);
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager().beginTransaction().replace(mBinding.viewContent.getId(),mAboutFragment).commit();
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
