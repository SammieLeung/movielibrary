package com.hphtv.movielibrary.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.BaseAdapter;
import com.hphtv.movielibrary.adapter.T9KeyBoradAdapter;
import com.hphtv.movielibrary.adapter.MovieAdapter;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.databinding.LayoutMovieSearchBinding;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.viewmodel.MovieSearchViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/8/19
 */
public class PinyinSearchActivity extends AppBaseActivity<MovieSearchViewModel, LayoutMovieSearchBinding> {
    private MovieAdapter mMovieAdapter;

    @Override
    protected int getContentViewId() {
        return R.layout.layout_movie_search;
    }

    @Override
    protected void processLogic() {
        prepearT9();
        initView();
        mViewModel.init();
    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        super.onActivityResultCallback(result);
        if (result.getResultCode() == 1) {
            mViewModel.init();
            mBinding.etSearch.getText().clear();
        }
    }

    private void prepearT9() {
        List<Object[]> datas = new ArrayList<Object[]>();
        String[] s1 = getResources().getStringArray(R.array.k1);
        String[] s2 = getResources().getStringArray(R.array.k2);
        String[] s3 = getResources().getStringArray(R.array.k3);
        String[] s4 = getResources().getStringArray(R.array.k4);
        String[] s5 = getResources().getStringArray(R.array.k5);
        String[] s6 = getResources().getStringArray(R.array.k6);
        String[] s7 = getResources().getStringArray(R.array.k7);
        String[] s8 = getResources().getStringArray(R.array.k8);
        String[] s9 = getResources().getStringArray(R.array.k9);
        String[] sc = getResources().getStringArray(R.array.kc);
        String[] s0 = getResources().getStringArray(R.array.k0);
        Integer[] sback = new Integer[]{R.mipmap.keyboard_del};

        datas.add(s1);
        datas.add(s2);
        datas.add(s3);
        datas.add(s4);
        datas.add(s5);
        datas.add(s6);
        datas.add(s7);
        datas.add(s8);
        datas.add(s9);
        datas.add(sc);
        datas.add(s0);
        datas.add(sback);
        GridLayoutManager t9LayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        T9KeyBoradAdapter t9KeyBoradAdapter = new T9KeyBoradAdapter(this, datas);
        t9KeyBoradAdapter.setOnKeyBoardClickListener((view, pos, keyValues) -> {
            if (pos > 0 && pos < 9) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mBinding.viewInputgroup.getLayoutParams();
                int offset_left = layoutParams.leftMargin;
                int offset_top = layoutParams.topMargin + mBinding.etSearch.getHeight() + ((RelativeLayout.LayoutParams) mBinding.rvKeyboardT9.getLayoutParams()).topMargin;
                mBinding.floatkeyboard.setDatas((String[]) keyValues);
                mBinding.floatkeyboard.show(view, offset_left, offset_top);
            } else {
                if (mBinding.floatkeyboard.isShowed()) {
                    mBinding.floatkeyboard.hide();
                }
                switch (pos) {
                    case 0:
                    case 10:
                        mBinding.etSearch.append(((TextView) view.findViewById(R.id.kbi_single_num)).getText());
                        break;
                    case 9:
                        mBinding.etSearch.getText().clear();
                        break;
                    case 11:
                        Editable editable = mBinding.etSearch.getText();
                        int len = editable.length();
                        if (len > 0)
                            editable.delete(len - 1, len);
                }
            }
        });
        mBinding.rvKeyboardT9.setLayoutManager(t9LayoutManager);
        mBinding.rvKeyboardT9.setAdapter(t9KeyBoradAdapter);

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
        mMovieAdapter = new MovieAdapter(this, new ArrayList());
        mMovieAdapter.setOnItemClickListener((BaseAdapter.OnRecyclerViewItemClickListener<MovieDataView>) (view, data) -> {
            Intent intent = new Intent(PinyinSearchActivity.this,
                    MovieDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putLong(ConstData.IntentKey.KEY_MOVIE_ID, data.id);
            bundle.putInt(ConstData.IntentKey.KEY_MODE, ConstData.MovieDetailMode.MODE_WRAPPER);
            intent.putExtras(bundle);

        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);
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

    }

    private void search(String pinyin) {
        mViewModel.search(pinyin, data -> {
            if (data.size() > 0) {
                hideEmptyTips();
                mMovieAdapter.addAll(data);
            } else {
                mMovieAdapter.removeAll();
                showEmptyTips();
            }
        });
    }

    private void showEmptyTips() {
        mBinding.tvEmptyTips.setVisibility(View.VISIBLE);
    }

    private void hideEmptyTips() {
        mBinding.tvEmptyTips.setVisibility(View.GONE);
    }

}
