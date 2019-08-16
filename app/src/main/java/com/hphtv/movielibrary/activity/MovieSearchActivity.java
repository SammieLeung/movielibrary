package com.hphtv.movielibrary.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.adapter.MovieLibraryAdapter;
import com.hphtv.movielibrary.sqlite.bean.Directory;
import com.hphtv.movielibrary.sqlite.bean.MovieWrapper;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.LocalSearchAdapter;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.decoration.GridSpacingItemDecorationVertical;
import com.hphtv.movielibrary.sqlite.dao.DirectoryDao;
import com.hphtv.movielibrary.sqlite.dao.MovieWrapperDao;
import com.hphtv.movielibrary.util.LogUtil;
import com.hphtv.movielibrary.util.MyPinyinParseAndMatchUtil;
import com.hphtv.movielibrary.view.CustomLoadingCircleViewFragment;
import com.hphtv.movielibrary.view.FloatKeyboard;
import com.hphtv.movielibrary.view.RecyclerViewWithMouseScroll;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tchip on 17-12-14.
 */

public class MovieSearchActivity extends Activity {
    public static final String TAG = "MovieSearchActivity";

    private Context mContext;
    EditText mEditTextSearch;
    RecyclerView mRVKeyBoard;
    TextView mTextViewTips;
    RecyclerViewWithMouseScroll mRVMovies;
    List<MovieWrapper> mWrapperList = new ArrayList<>();
    FloatKeyboard mRVFloatFastBoard;
    MovieLibraryAdapter movieLibraryAdapter;
    CustomLoadingCircleViewFragment mLoadingCircleViewDialogFragment;
    private MovieApplication mApp;

    private MovieWrapperDao mMovieWrapperDao;
    private DirectoryDao mDirDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_movie_search);
        mContext = this;
        mApp = (MovieApplication) getApplication();
        mMovieWrapperDao = new MovieWrapperDao(mContext);
        mDirDao = new DirectoryDao(mContext);
        initView();
        initMovie();
    }

    private void initView() {
        mTextViewTips = (TextView) findViewById(R.id.tv_empty_tips);
        mEditTextSearch = (EditText) findViewById(R.id.et_search);
        mEditTextSearch.setFocusable(false);
        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.v(TAG, "onTextChanged");
                String keyword = mEditTextSearch.getText().toString();
                Log.v(TAG, "matchMovieBegin-------");

                List<MovieWrapper> wrapperList = matchMovie(keyword);
                Log.v(TAG, "matchMovie finish-------");

                refreshMovie(wrapperList);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mRVKeyBoard = (RecyclerView) findViewById(R.id.rv_kb_t9);
        mRVMovies = (RecyclerViewWithMouseScroll) findViewById(R.id.rv_search_movies);
        mRVMovies.setFocusable(false);
        GridLayoutManager layoutManager = new GridLayoutManager(MovieSearchActivity.this, 3, GridLayoutManager.VERTICAL, false);
        GridLayoutManager linearLayoutManager = new GridLayoutManager(MovieSearchActivity.this, 6, GridLayoutManager.VERTICAL, false);
        mRVKeyBoard.setLayoutManager(layoutManager);
        mRVMovies.setLayoutManager(linearLayoutManager);
        mRVFloatFastBoard = (FloatKeyboard) findViewById(R.id.float_keboard);
        mRVFloatFastBoard.hide();
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

        LocalSearchAdapter localSearchAdapter = new LocalSearchAdapter(MovieSearchActivity.this, datas);
        movieLibraryAdapter = new MovieLibraryAdapter(MovieSearchActivity.this, mWrapperList);
        movieLibraryAdapter.setOnItemClickListener(new MovieLibraryAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, MovieWrapper wrapper) {
                Intent intent = new Intent(mContext,
                        MovieDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("wrapper", wrapper);
                bundle.putInt("mode", ConstData.MovieDetailMode.MODE_WRAPPER);
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
            }

        });
        mRVKeyBoard.setAdapter(localSearchAdapter);
        mRVKeyBoard.post(new Runnable() {
            @Override
            public void run() {
                mRVKeyBoard.getChildAt(0).requestFocus();
            }
        });
        mRVMovies.setAdapter(movieLibraryAdapter);
        GridSpacingItemDecorationVertical itemDecoration = new GridSpacingItemDecorationVertical(MovieSearchActivity.this, R.dimen.itemOffset, 3);
        mRVKeyBoard.addItemDecoration(itemDecoration);
        localSearchAdapter.setOnKeyBoardClickListener(new LocalSearchAdapter.OnKeyBoardClickListener() {
            @Override
            public void OnKeyBoardClick(View view, int pos, final Object[] keyValues) {

                if (pos > 0 && pos < 9) {
                    int offset_left = ((FrameLayout.LayoutParams) ((View) mRVKeyBoard.getParent()).getLayoutParams()).leftMargin;
                    int offset_top = ((FrameLayout.LayoutParams) ((View) mRVKeyBoard.getParent()).getLayoutParams()).topMargin + mEditTextSearch.getHeight() + ((RelativeLayout.LayoutParams) mRVKeyBoard.getLayoutParams()).topMargin;
                    mRVFloatFastBoard.setDatas((String[]) keyValues);
                    mRVFloatFastBoard.show(view, offset_left, offset_top);
                } else {
                    if (mRVFloatFastBoard.isShowed()) {
                        mRVFloatFastBoard.hide();
                    }
                    switch (pos) {
                        case 0:
                        case 10:
                            mEditTextSearch.append(((TextView) view.findViewById(R.id.kbi_single_num)).getText());
                            break;
                        case 9:
                            mEditTextSearch.setText(null);
                            break;
                        case 11:
                            StringBuffer tmpString = new StringBuffer();
                            tmpString.append(mEditTextSearch.getText());
                            int len = tmpString.length();
                            if (len > 0)
                                tmpString.deleteCharAt(len - 1);
                            mEditTextSearch.setText(tmpString.toString());
                    }
                }

            }
        });


        mRVFloatFastBoard.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        mEditTextSearch.append(mRVFloatFastBoard.getCenterValue());
                        mRVFloatFastBoard.hide();
                        return true;
                    }

                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        mEditTextSearch.append(mRVFloatFastBoard.getBottomValue());
                        mRVFloatFastBoard.hide();
                        return true;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        mEditTextSearch.append(mRVFloatFastBoard.getLeftValue());
                        mRVFloatFastBoard.hide();
                        return true;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        mEditTextSearch.append(mRVFloatFastBoard.getTopValue());
                        mRVFloatFastBoard.hide();
                        return true;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        mEditTextSearch.append(mRVFloatFastBoard.getRightValue());
                        mRVFloatFastBoard.hide();
                        return true;
                    }
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        mRVFloatFastBoard.hide();
                        return true;
                    }
                }

                return false;
            }
        });
        mRVFloatFastBoard.setOnButtonClickListener(new FloatKeyboard.OnButtonClickListener() {
            @Override
            public void OnButtonClick(String data) {
                mEditTextSearch.append(data);
                mRVFloatFastBoard.hide();
            }
        });
        showEmptyTips();
    }


    private void initMovie() {
        startLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isShowEncrypted = mApp.isShowEncrypted();
                mWrapperList = new ArrayList<>();
                Cursor cursor = null;
                if (isShowEncrypted) {
                    cursor = mMovieWrapperDao.selectAll();
                } else {
                    Cursor dirCursor = mDirDao.select("is_encrypted=?", new String[]{"0"}, null);
                    if (dirCursor.getCount() > 0) {
                        List<Directory> directories = mDirDao.parseList(dirCursor);
                        StringBuffer buffer=new StringBuffer();
                        buffer.append("(");
                        for(Directory t_dir:directories){
                            buffer.append("(dir_ids like '%"+t_dir.getId()+"%' or dir_ids like '%"+t_dir.getId()+"]%') or ");
                        }
                        buffer.replace(buffer.lastIndexOf("or"),buffer.length(),")");
                        cursor=mMovieWrapperDao.select(buffer.toString(),null,null);
                    }
                }
                if (cursor != null && cursor.getCount() > 0) {
                    mWrapperList = mMovieWrapperDao.parseList(cursor);
                }
                stopLoading();
            }
        }).start();

    }

    private List<MovieWrapper> matchMovie(String keyword) {
        if (keyword == null || keyword.equals("")) {
            return null;
        }

        //*
        MyPinyinParseAndMatchUtil pinyinParseAndMatchUtil = MyPinyinParseAndMatchUtil.getInstance();
        pinyinParseAndMatchUtil.initBaseDatas(mWrapperList);
        return pinyinParseAndMatchUtil.match(keyword);
        /*/
        return MyPinyinParseAndMatchUtil.match(keyword, MovieSearchActivity.this);
        //*/

    }

    private void refreshMovie(List<MovieWrapper> wrappers) {
        movieLibraryAdapter.removeAll();
        if (wrappers == null || wrappers.size() == 0) {
            showEmptyTips();
            return;
        }
        hideEmptyTips();
        for (int i = 0; i < wrappers.size(); i++) {
            movieLibraryAdapter.addItem(wrappers.get(i), i);
        }

    }


    private void showEmptyTips() {
        mTextViewTips.setVisibility(View.VISIBLE);
    }

    private void hideEmptyTips() {
        mTextViewTips.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            initMovie();
            mEditTextSearch.setText(null);
        }
    }

    private void startLoading() {
        LogUtil.v(TAG, "startLoading");
        if (mLoadingCircleViewDialogFragment == null) {
            mLoadingCircleViewDialogFragment = new CustomLoadingCircleViewFragment();
            mLoadingCircleViewDialogFragment.show(getFragmentManager(), TAG);
        }

    }

    private void stopLoading() {
        LogUtil.v(TAG, "stopLoading");
        if (mLoadingCircleViewDialogFragment != null) {
            mLoadingCircleViewDialogFragment.dismiss();
            mLoadingCircleViewDialogFragment = null;
        }
    }
}
