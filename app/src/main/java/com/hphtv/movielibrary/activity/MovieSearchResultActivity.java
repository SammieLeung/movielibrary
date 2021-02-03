package com.hphtv.movielibrary.activity;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.firefly.videonameparser.MovieNameInfo;
import com.hphtv.movielibrary.listener.WebviewListener;
import com.hphtv.movielibrary.sqlite.bean.VideoFile;
import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.MovieSearchAdapter;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Movie;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.SimpleMovie;
import com.hphtv.movielibrary.scraper.Scraper;
import com.hphtv.movielibrary.scraper.imdb.ImdbApi;
import com.hphtv.movielibrary.scraper.mtime.MtimeApi;
import com.hphtv.movielibrary.util.DoubanMovieSearchHelper;
import com.hphtv.movielibrary.view.RefreshLayout;
import com.hphtv.movielibrary.view.RefreshLayout.OnLoadListener;

import org.jsoup.select.Elements;

public class MovieSearchResultActivity extends Activity {
    public static final String TAG = "MovieSearchResult";
    public static final int LIMIT = 10;
    private ImageButton mBtnBack;
    private ListView mSearchResultLv;// 搜索结果列表

    private ImageView ivSearchDialogBg;// 搜索结果页面背景
    private EditText mEditTextKeyword;// 搜索输入框
    private RefreshLayout mRefreshLayout;// 搜索结果d拉插件

    private MovieSearchAdapter myMovieSearchAdapter;
    private MovieSearchResultActivity mActivity;
    private MovieApplication mApplication;
    private DoubanMovieSearchHelper searchHelper;
    private boolean itemClickable = true;
    // 电影搜索结果
    private List<SimpleMovie> mMovieList = new ArrayList<>();
    // 传过来的文件名
    private String mFileName;

    private Movie currentMovie;
    private VideoFile currentVideoFile;

    // 搜索结果
    private static final int SEARCH_SUCCESS = 1;
    private static final int SEARCH_ERROR = -1;
    private static final int SEARCH_BEGIN = 0;
    private int currentMode;
    private int api_version;
    private Object cacheDatas = null;
    private int cacheOffset = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_reslut);
        mApplication = (MovieApplication) getApplicationContext();
        mActivity = MovieSearchResultActivity.this;
        mApplication.addActivity(MovieSearchResultActivity.this);
        mBtnBack = findViewById(R.id.ibtn_back);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        searchHelper = mApplication.getSearchHelper();

        initOthers();
        initRefreshLayout();
        initListViewAdapter(this);
        searchHelper.setSearchMode(ConstData.SearchMode.MODE_LIST);

        onNewIntent(getIntent());//搜索入口
        // mEditTextKeyword.setText("qybs");
        // 显示界面可以自动搜索内容
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mOnRefreshListener.onRefresh();
            }
        }, 500);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        // 如果有传新的电影名
        if (bundle != null) {
//            if (bundle.getInt("random", -1) == -1) {//从文件管理器进入。
//                Log.v(TAG, "主页查看详情：" + bundle.getInt("random"));
//                mFileName = bundle.getString("filepath");
//                Log.v(TAG, "mFileName：" + mFileName);
//                MovieNameInfo mni = (new VideoNameParser()).parseVideoName(
//                        mFileName);
//                String parseName = getSeasonAndEpisode(mni);
//                currentMovie = new Movie();
//
//                currentVideoFile = new VideoFile();
//                currentVideoFile.setUri(mFileName);
//                mEditTextKeyword.setText(parseName);
//                currentMode = ConstData.MovieDetailMode.MODE_OUTSIDE;
//                // TODO
//            } else {//编辑电影信息
            int mode = bundle.getInt("mode");
            switch (mode) {
                case ConstData.MovieDetailMode.MODE_EDIT:
                    currentMode = ConstData.MovieDetailMode.MODE_EDIT;
                    mFileName = bundle.getString("keyword");
                    api_version = bundle.getInt("api");
                    if (mFileName != null && mFileName != "")
                        mEditTextKeyword.setText(mFileName);
                    break;
            }
            Log.v(TAG, "编辑电影信息");
            // 没有新的电影名,则用上次的.

//            }
        }
    }

    /**
     * 初始化其他组件
     */
    public void initOthers() {
        // listview本体
        mSearchResultLv = (ListView) findViewById(R.id.lv_search_content);
        // 背景图片
        ivSearchDialogBg = (ImageView) findViewById(R.id.iv_search_dialog_bg);
        // 搜索输入
        mEditTextKeyword = (EditText) findViewById(R.id.et_box_name);
        mEditTextKeyword.setOnEditorActionListener(mOnEditorActionListener);
        mEditTextKeyword.setOnFocusChangeListener(mOnFocusChangeListener);
        // webview
        // initWebView();

    }

    /**
     * 初始化RefreshLayout
     */
    public void initRefreshLayout() {
        mRefreshLayout = (RefreshLayout) findViewById(R.id.id_srl);
        mRefreshLayout.setSize(RefreshLayout.LARGE);
        mRefreshLayout.setColorSchemeResources(R.color.green_gray);
        mRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mRefreshLayout.setOnLoadListener(mOnLoadListener);
        mRefreshLayout.setProgressViewEndTarget(true, 280);
    }

    /**
     * 初始化ListView
     */
    public void initListViewAdapter(Context context) {
        myMovieSearchAdapter = new MovieSearchAdapter(context, mMovieList);
        mSearchResultLv.setAdapter(myMovieSearchAdapter);
        mSearchResultLv.setOnItemClickListener(mOnItemClickListener);
        mSearchResultLv.setOnItemSelectedListener(mOnItemSelectedListener);
    }


    //webview 组件回调
    private WebviewListener mWebviewListener = new WebviewListener() {
        @Override
        public void onStart() {
            handler.sendEmptyMessage(SEARCH_BEGIN);
        }

        @Override
        public void onGetData(List<SimpleMovie> simpleMovieList, int mode) {
            mMovieList = simpleMovieList;
            if (mMovieList != null) {
                handler.sendEmptyMessage(SEARCH_SUCCESS);
            } else {
                handler.sendEmptyMessage(SEARCH_ERROR);
            }

        }
    };

    SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {

        @Override
        public void onRefresh() {
            // 等于按搜索按钮
            searchHelper.setSearchMode(ConstData.SearchMode.MODE_LIST);
            mFileName = mEditTextKeyword.getText().toString().trim();
            Log.v(TAG, "onRefresh====>" + mFileName);
            // 电影名不为空则搜索
            if (!TextUtils.isEmpty(mFileName)) {
                switch (api_version) {
                    case ConstData.Scraper.DOUBAN:
                        searchHelper.SearchMovieByName(mFileName);//通过webview进行搜索
                        break;
                    case ConstData.Scraper.IMDB:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handler.sendEmptyMessage(SEARCH_BEGIN);
                                mMovieList.clear();
                                cacheOffset = 0;
                                cacheDatas = ImdbApi.SearchMoviesByName(mMovieList, null, mFileName, cacheOffset, LIMIT);
                                handler.sendEmptyMessage(SEARCH_SUCCESS);
                            }
                        }).start();
                        break;
                    case ConstData.Scraper.MTIME:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handler.sendEmptyMessage(SEARCH_BEGIN);
                                mMovieList.clear();
                                cacheOffset = 1;
                                cacheDatas = MtimeApi.SearchMoviesByName(mMovieList, null, mFileName, cacheOffset, LIMIT);
                                handler.sendEmptyMessage(SEARCH_SUCCESS);
                            }
                        }).start();
                        break;
                }
            } else
                mRefreshLayout.setLoading(false);
        }
    };
    OnEditorActionListener mOnEditorActionListener = new OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
            if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
                mOnRefreshListener.onRefresh();
            }
            return true;
        }
    };

    View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (v.getId() == R.id.et_box_name && hasFocus) {
                mEditTextKeyword.setSelection(0, mEditTextKeyword.getText().length());
            }
        }
    };


    OnLoadListener mOnLoadListener = new OnLoadListener() {
        @Override
        public void onLoad() {

            Log.v(TAG, "onLoad()");
            searchHelper.setSearchMode(ConstData.SearchMode.MODE_LIST);
            // 电影名不为空 且 不是最后一页 则继续搜索
            if (mFileName != null && !mFileName.equals("")
                    && !searchHelper.ismIsLoadEnd()) {
                switch (api_version) {
                    case ConstData.Scraper.DOUBAN:
                        searchHelper.SearchMovieByName(mFileName, false);

                        break;
                    case ConstData.Scraper.IMDB:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handler.sendEmptyMessage(SEARCH_BEGIN);
                                Elements tmp = ImdbApi.SearchMoviesByName(mMovieList, (Elements) cacheDatas, mFileName, cacheOffset, LIMIT);
                                if (tmp == null) {
                                    handler.sendEmptyMessage(SEARCH_ERROR);
                                } else {
                                    handler.sendEmptyMessage(SEARCH_SUCCESS);
                                }
                            }
                        }).start();
                        break;
                    case ConstData.Scraper.MTIME:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handler.sendEmptyMessage(SEARCH_BEGIN);
                                JSONArray tmp = MtimeApi.SearchMoviesByName(mMovieList, null, mFileName, cacheOffset, LIMIT);
                                if (tmp == null) {
                                    handler.sendEmptyMessage(SEARCH_ERROR);
                                } else {
                                    handler.sendEmptyMessage(SEARCH_SUCCESS);
                                }
                            }
                        }).start();
                        break;
                }
            } else {
                switch (api_version) {
                    case ConstData.Scraper.DOUBAN:
                        if (searchHelper.ismIsLoadEnd()) {
                            handler.sendEmptyMessage(SEARCH_ERROR);
                            searchHelper
                                    .setLastPosition(DoubanMovieSearchHelper.LAST_POSTION);
                        }
                        break;
                    case ConstData.Scraper.IMDB:
                        handler.sendEmptyMessage(SEARCH_ERROR);
                        break;
                    case ConstData.Scraper.MTIME:
                        handler.sendEmptyMessage(SEARCH_ERROR);
                        break;
                }
                mRefreshLayout.setLoading(false);
            }

        }
    };

    /**
     * 电影点击监听
     */
    OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position,
                                long id) {

            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt("mode", currentMode);
            bundle.putSerializable("simplemovie",
                    mMovieList.get(position));
            if (currentVideoFile != null)
                bundle.putSerializable("videoFile", currentVideoFile);
            intent.putExtras(bundle);
            setResult(1, intent);
            finish();
        }
    };

    AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.v(TAG, view.toString());
            Log.v(TAG, "p=" + position);
            Log.v(TAG, "id=" + id);
            if (position == myMovieSearchAdapter.getCount() - 1) {
                searchHelper
                        .setLastPosition(DoubanMovieSearchHelper.LAST_POSTION);
            } else {
                searchHelper.setLastPosition(DoubanMovieSearchHelper.NOT_LAST_POSTION);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                int index = mSearchResultLv.getSelectedItemPosition();
                mSearchResultLv.smoothScrollToPosition(index);
                // 如果已经是最后一个元素,还要继续按下的话执行
                if (searchHelper.isLastPostion()) {
                    // 状态设为正在LOADING
                    Log.v(TAG, "isLastPosition");
                    searchHelper.setLastPosition(DoubanMovieSearchHelper.LOADING);
                    try {
                        // 最终调用OnLoadListener的OnLoad方法,这样调用会出现loading动画
                        Method loadMethod = mRefreshLayout.getClass()
                                .getDeclaredMethod("loadData", new Class[0]);
                        loadMethod.setAccessible(true);
                        loadMethod.invoke(mRefreshLayout, new Object[]{});
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                }

            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                int index = mSearchResultLv.getSelectedItemPosition();
                mSearchResultLv.smoothScrollToPosition(index);
            }
        }

        return super.dispatchKeyEvent(event);

    }

    private class MyHandler extends Handler {
        private WeakReference<MovieSearchResultActivity> reference;

        public MyHandler(MovieSearchResultActivity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEARCH_SUCCESS:
                    if (reference.get().myMovieSearchAdapter != null) {
                        reference.get().myMovieSearchAdapter.notifyDataSetChanged();
                        reference.get().mRefreshLayout.setLoading(false);
                        if (reference.get().api_version != ConstData.Scraper.DOUBAN)
                            reference.get().cacheOffset += 1;
                    }
                    break;
                case SEARCH_ERROR:
                    Toast.makeText(reference.get().mActivity, "搜索不到了~", Toast.LENGTH_SHORT).show();
                    reference.get().mRefreshLayout.setLoading(false);
                    break;

                case SEARCH_BEGIN:
                    reference.get().mRefreshLayout.setLoading(true);
                default:
                    break;
            }
        }
    }

    MyHandler handler = new MyHandler(this);

    protected void onResume() {
        // 一定要为helper关联datalist.
        searchHelper.setSearch_data_list(mMovieList);
        Log.v(TAG, "onResume()被调用");
        super.onResume();
        searchHelper.registerWebviewListener(mWebviewListener);
    }


    protected void onPause() {
        Log.v(TAG, "onPause()被调用");
        searchHelper.setSearch_data_list(null);
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()被调用");
        mApplication.removeActivity(MovieSearchResultActivity.this);
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }


}
