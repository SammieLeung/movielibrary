package com.hphtv.movielibrary.ui.moviesearch;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.NewMovieLargeItemListAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.ActivityLocalSearchBinding;
import com.hphtv.movielibrary.effect.GridSpacingItemDecorationVertical;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.detail.MovieDetailActivity;
import com.station.kit.util.DensityUtil;

import java.util.ArrayList;

/**
 * author: Sam Leung
 * date:  2021/8/19
 */
public class PinyinSearchActivity extends AppBaseActivity<MovieSearchViewModel, ActivityLocalSearchBinding> {
    private NewMovieLargeItemListAdapter mMovieAdapter;

    private View.OnClickListener mOnClickListener = v -> {
        if (mBinding.floatkeyboard.isShowed()) {
            mBinding.floatkeyboard.hide();
        }
        switch (v.getId()) {
            case R.id.k0:
                mBinding.etSearch.append("0");
                break;
            case R.id.k1:
                mBinding.etSearch.append("1");
                break;
            case R.id.k2:
                showFloatKeyboard(v, new String[]{"2", "B", "A", "C"});
                break;
            case R.id.k3:
                showFloatKeyboard(v, new String[]{"3", "E", "D", "F"});
                break;
            case R.id.k4:
                showFloatKeyboard(v, new String[]{"4", "H", "G", "I"});
                break;
            case R.id.k5:
                showFloatKeyboard(v, new String[]{"5", "K", "J", "L"});
                break;
            case R.id.k6:
                showFloatKeyboard(v, new String[]{"6", "N", "M", "O"});
                break;
            case R.id.k7:
                showFloatKeyboard(v, new String[]{"7", "Q", "P", "R", "S"});
                break;
            case R.id.k8:
                showFloatKeyboard(v, new String[]{"8", "U", "T", "V"});
                break;
            case R.id.k9:
                showFloatKeyboard(v, new String[]{"9", "X", "W", "Y", "Z"});
                break;
            case R.id.kdel:
                Editable editable = mBinding.etSearch.getText();
                int len = editable.length();
                if (len > 0)
                    editable.delete(len - 1, len);
                break;
            case R.id.kclear:
                mBinding.etSearch.getText().clear();
                break;
        }
    };

    private void showFloatKeyboard(View v, String[] datas) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mBinding.viewInputgroup.getLayoutParams();
        int offset_left = layoutParams.leftMargin;
        int offset_top = layoutParams.topMargin + mBinding.etSearch.getHeight() + ((RelativeLayout.LayoutParams) mBinding.rvKeyboardT9.getLayoutParams()).topMargin;
        mBinding.floatkeyboard.setDatas(datas);
        mBinding.floatkeyboard.show(v, offset_left, offset_top);
    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepearT9();
        initView();
        mViewModel.init();
    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        super.onActivityResultCallback(result);
        if (result.getResultCode() == RESULT_OK) {
            mViewModel.init();
            mBinding.etSearch.getText().clear();
            setResult(RESULT_OK);
        }
    }

    private void prepearT9() {
        mBinding.k0.tvKey1.setOnClickListener(mOnClickListener);
        mBinding.k1.tvKey1.setOnClickListener(mOnClickListener);
        mBinding.k2.tvKey2.setOnClickListener(mOnClickListener);
        mBinding.k3.tvKey2.setOnClickListener(mOnClickListener);
        mBinding.k4.tvKey2.setOnClickListener(mOnClickListener);
        mBinding.k5.tvKey2.setOnClickListener(mOnClickListener);
        mBinding.k5.tvKey2.requestFocus();
        mBinding.k6.tvKey2.setOnClickListener(mOnClickListener);
        mBinding.k7.tvKey2.setOnClickListener(mOnClickListener);
        mBinding.k8.tvKey2.setOnClickListener(mOnClickListener);
        mBinding.k9.tvKey2.setOnClickListener(mOnClickListener);
        mBinding.kclear.setOnClickListener(mOnClickListener);
        mBinding.kdel.setOnClickListener(mOnClickListener);

        mBinding.floatkeyboard.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    mBinding.etSearch.append(mBinding.floatkeyboard.getCenterValue());
                    mBinding.floatkeyboard.hide();
                    return true;
                }

                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    mBinding.etSearch.append(mBinding.floatkeyboard.getBottomValue());
                    mBinding.floatkeyboard.hide();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    mBinding.etSearch.append(mBinding.floatkeyboard.getLeftValue());
                    mBinding.floatkeyboard.hide();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    mBinding.etSearch.append(mBinding.floatkeyboard.getTopValue());
                    mBinding.floatkeyboard.hide();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    mBinding.etSearch.append(mBinding.floatkeyboard.getRightValue());
                    mBinding.floatkeyboard.hide();
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mBinding.floatkeyboard.hide();
                    return true;
                }
            }

            return false;
        });

        mBinding.floatkeyboard.setOnButtonClickListener(data -> {
            mBinding.etSearch.append(data);
            mBinding.floatkeyboard.hide();
        });
    }

    private void initView() {
        mMovieAdapter = new NewMovieLargeItemListAdapter(this, new ArrayList());
        mMovieAdapter.setZoomRatio(1.08f);
        mMovieAdapter.setOnItemClickListener((view, postion, data) -> {
            Intent intent = new Intent(PinyinSearchActivity.this,
                    MovieDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putLong(Constants.Extras.MOVIE_ID, data.id);
            bundle.putInt(Constants.Extras.MODE, Constants.MovieDetailMode.MODE_WRAPPER);
            intent.putExtras(bundle);
            startActivityForResult(intent);

        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        mBinding.btnExit.setOnClickListener(v -> finish());
        mBinding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString();
                search(keyword);
            }
        });
        mBinding.rvSearchMovies.setAdapter(mMovieAdapter);
        mBinding.rvSearchMovies.setLayoutManager(gridLayoutManager);
        mBinding.rvSearchMovies.addItemDecoration(new GridSpacingItemDecorationVertical(
                getResources().getDimensionPixelOffset(R.dimen.poster_item_large_w),
                DensityUtil.dip2px(this, 21),
                DensityUtil.dip2px(this, 35),
                DensityUtil.dip2px(this, 30),
                DensityUtil.dip2px(this, 30),
                3));
        TabLayout.Tab tab1 = mBinding.tablayout.newTab();
        TabLayout.Tab tab2 = mBinding.tablayout.newTab();
        TabLayout.Tab tab3 = mBinding.tablayout.newTab();
        mBinding.tablayout.addTab(tab1);
        mBinding.tablayout.addTab(tab2);
        mBinding.tablayout.addTab(tab3);

        mBinding.tablayout.selectTab(mBinding.tablayout.getTabAt(0));
        mBinding.tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        mMovieAdapter.getFilter().filter(null);
                        break;
                    case 1:
                        mMovieAdapter.getFilter().filter(Constants.SearchType.movie.name());
                        break;
                    case 2:
                        mMovieAdapter.getFilter().filter(Constants.SearchType.tv.name());
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void setTabs(int a, int b, int c) {
        mBinding.tablayout.getTabAt(0).setText(getString(R.string.tab_local_search_all, a));
        mBinding.tablayout.getTabAt(1).setText(getString(R.string.tab_local_search_movie, b));
        mBinding.tablayout.getTabAt(2).setText(getString(R.string.tab_local_search_tv, c));

        mBinding.tablayout.selectTab(mBinding.tablayout.getTabAt(0));
    }

    private void search(String pinyin) {
        mViewModel.search(pinyin, data -> {
            if (data.size() > 0) {
                hideEmptyTips();
                mBinding.setShowTab(true);
                mMovieAdapter.addAll(data);
                int count = 0;
                for (int i = 0; i < mMovieAdapter.getRealCount(); i++) {
                    if (data.get(i).type == Constants.SearchType.movie)
                        count++;
                }
                setTabs(mMovieAdapter.getRealCount(), count, mMovieAdapter.getRealCount() - count);
            } else {
                mMovieAdapter.clearAll();
                mBinding.setShowTab(false);
                showEmptyTips();
            }
        });
    }

    private void showEmptyTips() {
        mBinding.setIsEmpty(true);
    }

    private void hideEmptyTips() {
        mBinding.setIsEmpty(false);
    }

}
