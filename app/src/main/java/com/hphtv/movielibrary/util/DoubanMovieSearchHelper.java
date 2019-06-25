package com.hphtv.movielibrary.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.SimpleMovie;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.listener.WebviewListener;
import com.hphtv.movielibrary.scraper.douban.DoubanApi;
import com.hphtv.movielibrary.scraper.douban.DoubanURL;

public class DoubanMovieSearchHelper {
    public static final String TAG = DoubanMovieSearchHelper.class.getSimpleName();
    public static final String SEARCH_MOVIE_BEGIN = "com.hphtv.movielibrary.SEARCH_MOIVE_BEGIN";
    public static final String SEARCH_MOVIE_COMPLETE = "com.hphtv.movielibrary.SEARCH_MOIVE_COMPLETE";
    public static final String SEARCH_MOVIE_NO_RESULT = "com.hphtv.movielibrary.SEARCH_MOVIE_NO_RESULT";

    private WebviewListener mWebviewListener;
    private WebView mWebview;
    private int movieOffset = DEFAULT_OFFSET;
    private int movieLimit = DEFAULT_LIMIT;
    // 默认搜索索引\数量
    private static final int DEFAULT_OFFSET = 0;
    public static final int DEFAULT_LIMIT = 10;
    // 搜索结果最后一页标记
    private boolean mIsLoadEnd = false;
    /**
     * 搜索模式
     */
    private int mSearchMode;
    /**
     * 当前listview最后一个可见元素的状态.
     */
    private int lastPosition = NOT_LAST_POSTION;
    // listview最后一个可视view不是最后一个元素
    public static final int NOT_LAST_POSTION = 0;
    // listview最后一个可视view最后一个元素
    public static final int LAST_POSTION = 1;
    // listview正在加载
    public static final int LOADING = 2;

    private MovieApplication mContext;
    private static DoubanMovieSearchHelper helper;
    private List<SimpleMovie> search_data_list;

    /**
     * 提供给js调用的类
     *
     * @author tchip
     */
    private class JSHandler {
        // API17后一定要加@JavascriptInterface,否则会报错

        /**
         * 获取到webview的数据,在这里进行分发,决定由哪一个API处理
         *
         * @param data
         */
        @JavascriptInterface
        public void dispatchData(String data) {
            int len;
            if (search_data_list == null)
                search_data_list = new ArrayList<>();
            switch (mSearchMode) {
                case ConstData.SearchMode.MODE_LIST:
                    len = DoubanApi.parserSearchMoiveLists(
                            search_data_list, data, movieLimit);
                    if (len > 0) {
                        // 搜索成功 发送搜索成功的广播
                        mWebviewListener.onGetData(search_data_list, ConstData.SearchMode.MODE_LIST);
                        // 搜到电影了,修改标记
                        lastPosition = NOT_LAST_POSTION;
                        movieOffset += len;
                    } else {
                        // 搜索不到代表是最后一页了
                        mIsLoadEnd = true;
                        // 搜不到电影了要提示
                        mWebviewListener.onGetData(null, ConstData.SearchMode.MODE_LIST);
                        lastPosition = LAST_POSTION;
                    }
                    break;
                case ConstData.SearchMode.MODE_INFO:
                    len = DoubanApi.parserSearchMoiveLists(
                            search_data_list, data, 1);
                    if (len >= 1) {
                        mWebviewListener.onGetData(search_data_list, ConstData.SearchMode.MODE_INFO);
                    } else {
                        mWebviewListener.onGetData(null, ConstData.SearchMode.MODE_INFO);
                    }
                    break;
            }
        }
    }

    public static DoubanMovieSearchHelper getInstance() {
        if (helper == null) {
            helper = new DoubanMovieSearchHelper();
        }
        return helper;
    }

    public void setContext(MovieApplication context) {
        this.mContext = context;
    }

    public void registerWebviewListener(WebviewListener listener) {
        mWebviewListener=listener;
    }


    public void loadUrl(String url) {
        synchronized (mWebview) {
            mWebview.loadUrl(url);
        }
    }

    /**
     * 初始化Webview
     */
    public void initWebView() {
        this.initWebView(false, null);
    }

    /**
     * @param isCached     是否开启缓存
     * @param cacheDirPath 数据库缓存目录
     */
    @SuppressLint("JavascriptInterface")
    public void initWebView(boolean isCached, String cacheDirPath) {
        WebSettings settings = mWebview.getSettings();
        // 设置可以运行js脚本
        settings.setJavaScriptEnabled(true);
        // webview注入java对象,提供给js调用.
        mWebview.addJavascriptInterface(new JSHandler(), "handler");
        // 设置缓存模式
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // 开启DOM storage API 功能
        settings.setDomStorageEnabled(true);
        // 开启database storage API功能
        settings.setDatabaseEnabled(true);
        // 设置数据库缓存路径
        settings.setAppCachePath(cacheDirPath);
        settings.setAppCacheEnabled(true);
        mWebview.setWebChromeClient(new WebChromeClient() {
            // 这里设置获取到的网站title
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });
        final JSHandler handler = new JSHandler();
        mWebview.setWebViewClient(new WebViewClient() {
            // 在webview里打开新链接
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.v(TAG, "shouldOverrideUrlLoading");
                return super.shouldOverrideUrlLoading(view, url);
            }

            // 通知主程序准备加载的网页地址
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO mRefreshLayout.setRefreshing(true);
//                mContext.sendBroadcast(new Intent(SEARCH_MOVIE_BEGIN));
                mWebviewListener.onStart();
            }

            ;

            // 网站加载完成后调用
            @Override
            public void onPageFinished(WebView view, String url) {

                Log.v(TAG, "onPageFinished");
                view.evaluateJavascript("javascript:function a(){return document.body.innerHTML}a();", new ValueCallback<String>() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onReceiveValue(String s) {
                        s = StringEscapeUtils.unescapeJava(s);
                        handler.dispatchData(s);
                    }
                });
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);

                Log.v(TAG, "onReceivedError errorCode" + errorCode
                        + " \n====>description:" + description);
            }
        });
    }

    /**
     * 重新搜索电影列表(入口)
     *
     * @param name
     */
    public void SearchMovieByName(String name) {
        this.SearchMovieByName(name, true);
    }

    /**
     * 按名字搜索电影列表
     *
     * @param name     搜索名字
     * @param isReload 是否重新搜索
     */
    public void SearchMovieByName(String name, boolean isReload) {
        if (isReload) {
            search_data_list.clear();
            // 需要初始化movieOffset
            movieOffset = DEFAULT_OFFSET;
            // 初始化最后一页标记
            mIsLoadEnd = false;
            lastPosition = NOT_LAST_POSTION;
        } else {
        }
        this.SearchMovieByName(name, movieOffset, movieLimit);
    }

    /**
     * 按名字搜索电影
     *
     * @param name   关键字
     * @param offset 索引
     * @param limit  数量
     */
    public void SearchMovieByName(String name, int offset, int limit) {
        List<BasicNameValuePair> paramsList = new ArrayList<BasicNameValuePair>();
        paramsList.add(new BasicNameValuePair("search_text", String
                .valueOf(name)));
        paramsList.add(new BasicNameValuePair("cat", String.valueOf(1002)));
        paramsList.add(new BasicNameValuePair("start", String.valueOf(offset)));
        this.movieLimit = limit;
        String search_url = null;
        search_url = OkHttpUtil.attachHttpGetParams(
                DoubanURL.SEARCH_URL_NO_API, paramsList);
//        Log.v(TAG, "loadUrl begin...");
        loadUrl(search_url);
    }

    public List<SimpleMovie> getSearch_data_list() {
        return search_data_list;
    }

    public void setSearch_data_list(List<SimpleMovie> search_data_list) {
        this.search_data_list = search_data_list;
    }

    public WebView getmWebview() {
        return mWebview;
    }

    public void setmWebview(WebView mWebview) {
        this.mWebview = mWebview;
    }

    public void setmIsLoadEnd(boolean isLoadEnd) {
        this.mIsLoadEnd = isLoadEnd;
    }

    public boolean ismIsLoadEnd() {
        return mIsLoadEnd;
    }

    public void setMovieLimit(int movieLimit) {
        this.movieLimit = movieLimit;
    }

    public void setLastPosition(int lastPosition) {
        this.lastPosition = lastPosition;
    }

    public int getLastPosition() {
        return lastPosition;
    }

    public boolean isLastPostion() {
        return lastPosition == LAST_POSTION;
    }

    public boolean isNotLastPostion() {
        return lastPosition == NOT_LAST_POSTION;
    }

    public boolean isLoadingData() {
        return lastPosition == LOADING;
    }


    public void setSearchMode(@WebviewListener.SearchMode int mSearchMode) {
        if (mSearchMode == ConstData.SearchMode.MODE_INFO) {
            movieLimit = 1;
        } else {
            movieLimit = DEFAULT_LIMIT;
        }
        this.mSearchMode = mSearchMode;
    }

    public int getmSearchMode() {
        return mSearchMode;
    }
}
