package com.hphtv.movielibrary.activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firefly.videonameparser.MovieNameInfo;
import com.hphtv.movielibrary.sqlite.bean.Device;
import com.hphtv.movielibrary.sqlite.bean.Favorite;
import com.hphtv.movielibrary.sqlite.bean.History;
import com.hphtv.movielibrary.sqlite.bean.MovieWrapper;
import com.hphtv.movielibrary.sqlite.bean.PosterProviderBean;
import com.hphtv.movielibrary.sqlite.bean.ScraperInfo;
import com.hphtv.movielibrary.sqlite.bean.VideoFile;
import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.hphtv.movielibrary.service.MovieScanService;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.MovieTrailerAdapter;
import com.hphtv.movielibrary.adapter.MovieTrailerAdapter.OnRecyclerViewItemClickListener;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Movie;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.MovieTrailer;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Photo;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Rating;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.SimpleMovie;
import com.hphtv.movielibrary.scraper.douban.DoubanApi;
import com.hphtv.movielibrary.scraper.mtime.MtimeApi;
import com.hphtv.movielibrary.sqlite.dao.FavoriteDao;
import com.hphtv.movielibrary.sqlite.dao.HistoryDao;
import com.hphtv.movielibrary.sqlite.dao.MovieDao;
import com.hphtv.movielibrary.sqlite.dao.MovieWrapperDao;
import com.hphtv.movielibrary.sqlite.dao.MovietrailerDao;
import com.hphtv.movielibrary.sqlite.dao.PosterProviderDao;
import com.hphtv.movielibrary.sqlite.dao.VideoFileDao;
import com.hphtv.movielibrary.util.FileScanUtil;
import com.hphtv.movielibrary.util.LogUtil;
import com.hphtv.movielibrary.util.DoubanMovieSearchHelper;
import com.hphtv.movielibrary.util.MyPinyinParseAndMatchUtil;
import com.hphtv.movielibrary.util.StrUtils;
import com.hphtv.movielibrary.util.UriParseUtil;
import com.hphtv.movielibrary.util.VideoPlayTools;
import com.hphtv.movielibrary.view.CircleRecyelerViewWithMouseScroll;
import com.hphtv.movielibrary.view.ConfirmDialogFragment;
import com.hphtv.movielibrary.view.CustomLoadingCircleViewFragment;
import com.hphtv.movielibrary.view.CustomRadioDialogFragment;
import com.hphtv.movielibrary.view.DrawTopButton;
import com.hphtv.movielibrary.view.MovieEditFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import me.khrystal.library.widget.CircleRecyclerView;
import me.khrystal.library.widget.ItemViewMode;
import me.khrystal.library.widget.ScaleXCenterViewMode;

public class MovieDetailActivity extends AppBaseActivity {
    public static final String TAG = "MovieDetailActivity";
    private MovieApplication mApplication;
    private Context mContext;
    private DoubanMovieSearchHelper searchHelper;
    private ImageView mPosterIv;// 背景海报
    private RatingBar mRateBar;// 评分
    private TextView mRateTv;//评分
    private TextView mRateCountTv;//评价人数

    private TextView mTitleTv;// 电影名
    private ImageView mCoverIv;// 电影封面
    private TextView mPubDatesTv;// 上映年份
    private TextView mDuration;// 影片片长
    private TextView mLanguagesTv;// 语言
    private TextView mPubArea;//出品地区
    private TextView mActorsTv;// 演员
    private LinearLayout mActorsGroup;
    private TextView mGenresTv;// 电影/类型
    //    TextView mPathTv;// 路径
    private TextView mDetailTv;// 影片简介

    private CircleRecyelerViewWithMouseScroll mTrailerCircleRecyelerView;
    private ItemViewMode mItemViewMode;
    private LinearLayoutManager mLayoutManager;
    private MovieTrailerAdapter movieTrailerAdapter;
    private RelativeLayout mMovieTrailerRL;

    private ScrollView mDetialSV;
    private DrawTopButton mBtnPlay;// 播放
    private DrawTopButton mBtnFavorite;//收藏
    private DrawTopButton mBtnRemove;// 删除
    private DrawTopButton mBtnEdit;// 搜索按钮
    private DrawTopButton mTrailerBtn;// 预告片
    public static final int SEARCH_SUCCESS = 1;
    public static final int SEARCH_BEGIN = 0;
    public static final int SEARCH_ERROR = -1;
    private static final int PARSE_BEGIN = 2;
    private static final int PARSE_SUCCESS = 3;
    private static final int PARSE_ERROR = -2;

    boolean isSearchDialogShow = true;
    boolean isParseOver = false;
    boolean isExist;
    boolean isExistButNeedUpdate;
    boolean isChangeMovie;
    private int random;
    private List<MovieTrailer> trailers_list = new ArrayList<MovieTrailer>();
    private CustomLoadingCircleViewFragment mLoadingCircleViewDialogFragment;
    private MovieScanService mScanService;

    private MovieWrapper mCurrentWrapper;
    private Movie mCurrentMovie;
    private int currentMode;
    List<VideoFile> videoFileList;


    MovieWrapperDao mMovieWrapperDao;
    MovieDao mMovieDao;
    VideoFileDao mVideoFileDao;
    MovietrailerDao mMovietrailerDao;
    FavoriteDao mFavoriteDao;
    HistoryDao mHistoryDao;
    PosterProviderDao mPosterDao;

    // Button mBtnPlay;//播放
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail);

        // 初始化
        initView();
        mContext = this;
        mApplication.addActivity(MovieDetailActivity.this);
        searchHelper = mApplication.getSearchHelper();
        mMovieWrapperDao = new MovieWrapperDao(mContext);
        mMovieDao = new MovieDao(mContext);
        mVideoFileDao = new VideoFileDao(mContext);
        mMovietrailerDao = new MovietrailerDao(mContext);
        mHistoryDao = new HistoryDao(mContext);
        mFavoriteDao = new FavoriteDao(mContext);
        mPosterDao = new PosterProviderDao(mContext);
        Log.v(TAG, "onCreate===>start");
        // 先从数据库查找,有则不进入搜索界面.

    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume()被调用");
        Intent intent = new Intent(this, MovieScanService.class);
        bindService(intent, connection, Service.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    public void OnDeviceChange(Context context, List<Device> deviceList) {
    }

    @Override
    public void OnDeviceMonitorServiceConnect(DeviceMonitorService service) {

    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause()被调用");
//        unbindService(connection);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()被调用");
        unbindService(connection);
        mApplication.removeActivity(MovieDetailActivity.this);
        super.onDestroy();
    }

    public void initView() {
        mApplication = (MovieApplication) getApplicationContext();
        mPosterIv = (ImageView) findViewById(R.id.iv_poster);
        mRateBar = (RatingBar) findViewById(R.id.rb_rate);
        mRateTv = (TextView) findViewById(R.id.tv_rate);
        mRateCountTv = (TextView) findViewById(R.id.tv_rate_count);

        mTitleTv = (TextView) findViewById(R.id.tv_title);
        mCoverIv = (ImageView) findViewById(R.id.iv_cover);
        mPubDatesTv = (TextView) findViewById(R.id.tv_pub_dates);
        mDuration = (TextView) findViewById(R.id.tv_duration);
        mLanguagesTv = (TextView) findViewById(R.id.tv_spoken_languages);
        mActorsTv = (TextView) findViewById(R.id.tv_actors);
        mGenresTv = (TextView) findViewById(R.id.tv_genres);
//        mPathTv = (TextView) findViewById(R.id.tv_path);
        mDetailTv = (TextView) findViewById(R.id.tv_detail);
        mDetialSV = (ScrollView) findViewById(R.id.sv_detail);
        mPubArea = (TextView) findViewById(R.id.pub_area);
        mBtnPlay = (DrawTopButton) findViewById(R.id.bt_play);
        mBtnRemove = (DrawTopButton) findViewById(R.id.bt_remove);
        mBtnEdit = (DrawTopButton) findViewById(R.id.bt_edit);
        mTrailerBtn = (DrawTopButton) findViewById(R.id.btn_trailer);
        mBtnFavorite = (DrawTopButton) findViewById(R.id.btn_favorite);
        mBtnEdit.setOnClickListener(mClickListener);
        mTrailerBtn.setOnClickListener(mClickListener);
        mBtnPlay.setOnClickListener(mClickListener);
        mBtnRemove.setOnClickListener(mClickListener);
        mBtnFavorite.setOnClickListener(mClickListener);
        mMovieTrailerRL = (RelativeLayout) findViewById(R.id.rl_trailer);

        mTrailerCircleRecyelerView = (CircleRecyelerViewWithMouseScroll) findViewById(R.id.rv_trailer);
        mTrailerCircleRecyelerView.setOnCenterItemFocusListener(new CircleRecyclerView.OnCenterItemFocusListener() {
            @Override
            public void onCenterItemFocus(View v, boolean isViewOnCenter) {
                if (isViewOnCenter) {
                    v.findViewById(R.id.text_group_trailer).setVisibility(View.VISIBLE);
                } else {
                    v.findViewById(R.id.text_group_trailer).setVisibility(View.GONE);
                }
            }
        });

        mActorsGroup = (LinearLayout) findViewById(R.id.actors_group);
        initMovieTrailerList();
        mBtnPlay.requestFocus();
        mDetialSV.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mBtnPlay.requestFocus();
                }
            }
        });
    }

    /**
     * 预告片组件初始化。
     */
    private void initMovieTrailerList() {

        // 创建默认的线性LayoutManager
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(OrientationHelper.HORIZONTAL);

        mTrailerCircleRecyelerView.setLayoutManager(mLayoutManager);

        mItemViewMode = new ScaleXCenterViewMode();
        mTrailerCircleRecyelerView.setViewMode(mItemViewMode);
        mTrailerCircleRecyelerView.setNeedCenterForce(true);
        mTrailerCircleRecyelerView.setNeedLoop(true);
        // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mTrailerCircleRecyelerView.setHasFixedSize(true);
        // 创建并设置Adapter
        movieTrailerAdapter = new MovieTrailerAdapter(MovieDetailActivity.this,
                trailers_list);
        mTrailerCircleRecyelerView.setAdapter(movieTrailerAdapter);
        movieTrailerAdapter
                .setOnItemClickListener(mCenterItemClickListener);
//        new CenterLinearSnapHelper().attachToRecyclerView(mTrailerCircleRecyelerView);
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

    private void refreshMovieInfo(Intent intent) {
        Log.v(TAG, "refreshMovieInfo()");
        isExist = false;
        isExistButNeedUpdate = false;
        isChangeMovie = false;
//        if (videoFileList == null) {
//            videoFileList = new ArrayList<>();
//        }

        try {
            int mode = intent.getIntExtra("mode", ConstData.MovieDetailMode.MODE_WRAPPER);

            currentMode = mode;

            switch (currentMode) {
                case ConstData.MovieDetailMode.MODE_EDIT:
                    ButtonEnable(mBtnEdit, true);
                    getMovie((SimpleMovie) intent.getSerializableExtra("simplemovie"));
                    break;
                case ConstData.MovieDetailMode.MODE_OUTSIDE:
                    ButtonEnable(mBtnEdit, false);
                    getMovieNotSave((SimpleMovie) intent.getSerializableExtra("simplemovie"), (Movie) intent.getSerializableExtra("mCurrentMovie"), (VideoFile) intent.getSerializableExtra("videoFile"));
                    break;
                case ConstData.MovieDetailMode.MODE_WRAPPER:
                    mCurrentWrapper = (MovieWrapper) intent.getSerializableExtra("wrapper");
                    if (mCurrentWrapper != null) {
                        getMovieByWrapperId(mCurrentWrapper);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ButtonEnable(View v, boolean b) {
        v.setEnabled(b);
        v.setFocusable(b);
    }

    private void buildNullMovie(MovieWrapper wrapper) {
        mCurrentMovie = new Movie();

        Cursor cursor = mVideoFileDao.select("wrapper_id=?", new String[]{String.valueOf(wrapper.getId())}, null);
        if (cursor.getCount() > 0) {
            videoFileList = mVideoFileDao.parseList(cursor);
        }
        VideoFile videoFile = videoFileList.get(0);
        LogUtil.v(TAG, "file name=" + videoFile.getFilename());
        int end = videoFile.getFilename().lastIndexOf(".");
        String filename = videoFile.getFilename();
        if (end != -1) {
            filename = videoFile.getFilename().substring(0, end);
        }
        com.hphtv.movielibrary.sqlite.bean.scraperBean.Images images = new com.hphtv.movielibrary.sqlite.bean.scraperBean.Images();
        images.large = videoFile.getThumbnail();
        mCurrentMovie.setTitle(wrapper.getTitle());
        mCurrentMovie.setSummary(getResources().getString(R.string.tips_none));
        mCurrentMovie.setImages(images);
        mCurrentMovie.setApi(ConstData.Scraper.MTIME);
    }


    private void getMovieByWrapperId(final MovieWrapper wrapper) {
        startLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ScraperInfo[] Scrapers = wrapper.getScraperInfos();
                long id = wrapper.getId();
                Cursor videoFileCursor = mVideoFileDao.select("wrapper_id=?", new String[]{String.valueOf(id)}, null);
                if (videoFileCursor.getCount() > 0) {
                    videoFileList = mVideoFileDao.parseList(videoFileCursor);
                }

                if (Scrapers != null && Scrapers.length > 0) {
                    ScraperInfo scraper_info = Scrapers[0];
                    long movie_id = scraper_info.getId();
                    mCurrentMovie = mScanService.getMovie(String.valueOf(movie_id), ConstData.Scraper.UNKNOW);
                    mCurrentMovie.setTitle(mCurrentWrapper.getTitle());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refresh(mCurrentMovie);
                        }
                    });
//                    }

                } else {
                    buildNullMovie(wrapper);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refresh(mCurrentMovie);
                        }
                    });
                }
                isParseOver = true;
            }
        }).start();
    }

    /**
     * 根据VideoFileId id查询电影
     *
     * @param id
     */
    private void getMovieByVid(final String id) {
//        Log.v(TAG, "getMovieByVid() id=" + id);
//        startLoading();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                //1.清空播放文件列表
//                videoFileList.clear();
//                //2.数据库查找当前文件信息,获取电影id
//                VideoFileDao vDao = new VideoFileDao(MovieDetailActivity.this);
//                Cursor vcursor = vDao.select("id=?", new String[]{id},
//                        "0,1");
//                if (vcursor.getCount() > 0) {//根据id查找到videofile
//                    VideoFile videoFile = vDao.parseList(vcursor).get(0);
//                    String movieId = null; //获取videofile对应的电影。
//                    String baseclause = null;
//
////                    switch (api_version) {
////                        case ConstData.Scraper.DOUBAN:
////                            movieId = videoFile.getMovieId();
////                            baseclause = "movie_id=?";
////                            break;
////                        case ConstData.Scraper.IMDB:
////                            movieId = videoFile.getImdb_movie_id();
////                            baseclause = "imdb_movie_id=?";
////                            break;
////                        case ConstData.Scraper.MTIME:
////                            movieId = videoFile.getMtime_movie_id();
////                            baseclause = "mtime_movie_id=?";
////                            break;
////                    }
//                    //3.电影id为空或者为-1,则表示没有匹配的电影
//                    if (!TextUtils.isEmpty(movieId) && !movieId.equals("-1")) {
//                        //4.有匹配电影则查找出该电影所关联的所有文件
//                        Cursor videoFilesCursor = vDao.select(baseclause, new String[]{movieId}, null);
//                        videoFileList = vDao.parseList(videoFilesCursor);
//                    }
//                    //5.根据电影ID获取电影
//                    if (mScanService != null)
//                        mCurrentMovie = mScanService.getMovie(movieId);
//                    else
//                        Log.v(TAG, "mScanService fail!");
//                    //6.如果获取到的电影为null,则建立一个空白的电影界面
//                    if (mCurrentMovie == null) {
//                        buildNullMovie(videoFile);
//                    }
//                    //7.允许打开预告片,并刷新界面
//                    isParseOver = true;
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            refresh(mCurrentMovie, videoFileList);
//                        }
//                    });
//                } else {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            stopLoading();
//                        }
//                    });
//                }
//            }
//        }).start();

    }


    /**
     * 根据电影ID获取电影
     *
     * @param id 电影在数据库的索引id
     */
    private void getMovieByMovieId(final String id) {
//        Log.v(TAG, "getMovieByMovieId()");
//        startLoading();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                //1.清空播放文件列表
//                videoFileList.clear();
//
//                if (!TextUtils.isEmpty(id) && !id.equals("-1")) {
//                    //2.有匹配电影则查找出该电影所关联的所有文件
//                    VideoFileDao vDao = new VideoFileDao(MovieDetailActivity.this);
//                    String baseclause = "";
//                    switch (api_version) {
//                        case ConstData.Scraper.DOUBAN:
//                            baseclause = "movie_id=?";
//                            break;
//                        case ConstData.Scraper.IMDB:
//                            baseclause = "imdb_movie_id=?";
//                            break;
//                        case ConstData.Scraper.MTIME:
//                            baseclause = "mtime_movie_id=?";
//                            break;
//                    }
//                    Cursor videoFilesCursor = vDao.select(baseclause, new String[]{id}, null);
//                    videoFileList = vDao.parseVideoFiles(videoFilesCursor);
//                }
//                //3.根据电影ID获取电影
//                if (mScanService != null)
//                    mCurrentMovie = mScanService.getMovie(id);
//                else
//                    Log.v(TAG, "mScanService fail!");
//                //4.允许打开预告片,并刷新界面
//                isParseOver = true;
//                mHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        refresh(mCurrentMovie, videoFileList);
//                    }
//                });
//            }
//
//
////                videoFileList.clear();
////                MovieDao dao = new MovieDao(MovieDetailActivity.this);
////                VideoFileDao vDao = new VideoFileDao(MovieDetailActivity.this);
////
////
////                if (!TextUtils.isEmpty(movieId) && !movieId.equals("-1")) {
////                    Cursor cursor = dao.select("id=?", new String[]{movieId},
////                            "0,1");
////                    if (cursor.getCount() > 0) {//根据id查找到Movie
////                        mCurrentMovie = dao.parseMovies(cursor).get(0);
////                        Cursor videoFilesCursor = vDao.select("movie_id=?", new String[]{movieId}, null);
////                        videoFileList = vDao.parseVideoFiles(videoFilesCursor);
////                        String uptime = mCurrentMovie.getUptime();
////                        long currentTime = System.currentTimeMillis();
////
////                        if (TextUtils.isEmpty(uptime)) {
////                            uptime = String.valueOf(currentTime);
////                            mCurrentMovie.setUptime(uptime);
////                            ContentValues contentValues = dao
////                                    .parseContentValues(mCurrentMovie);
////                            dao.update(contentValues, "id=?",
////                                    new String[]{mCurrentMovie.getId()});
////                        }
////                        long updateTime = Long.valueOf(uptime);
////                        if (currentTime - updateTime > MovieDao.DEFAULT_TIME) {
////                            isExistButNeedUpdate = true;
////                            Log.v(TAG, "isExistButNeedUpdate " + isExistButNeedUpdate);
////                        }
////
////                        isParseOver = true;
////                        mHandler.post(new Runnable() {
////                            @Override
////                            public void run() {
////                                refresh(mCurrentMovie, videoFileList);
////                            }
////                        });//先给用户显示.
////                        if (isExistButNeedUpdate) {
////
////                            String url = mCurrentMovie.getAlt();//上面设置过，获取自己的路径
////                            if (url != null && !url.equals("") && !url.equals("null")) {
////                                Movie movie = DoubanApi.parserMovieInfo(url);
////                                if (movie == null) {
////                                    mHandler.post(new Runnable() {
////                                        @Override
////                                        public void run() {
////                                            stopLoading();
////                                            Toast.makeText(MovieDetailActivity.this, "获取电影信息失败！" + MovieLibraryError.SERVICE_UNAVAILABLE, Toast.LENGTH_LONG).show();
////                                        }
////                                    });
////                                    return;
////                                }
////
////                                String titlepinyin = MyPinyinParseAndMatchUtil.parsePinyin(movie);
////
////                /*--------获取不到的属性重新赋值-------*/
////                                movie.setUptime(String.valueOf(System
////                                        .currentTimeMillis()));
////                                movie.setTitlePinyin(titlepinyin);
////                                ContentValues contentValues = dao
////                                        .parseContentValues(movie);// 将movie对象转换为ContentValues对象
////                                long rowId = -1;
////                                rowId = dao.update(contentValues, "id=?",
////                                        new String[]{mCurrentMovie.getId()});
////                                movie.setId(mCurrentMovie.getId());
////                                mCurrentMovie = movie;
////                                isParseOver = true;
////                                mHandler.post(new Runnable() {
////                                    @Override
////                                    public void run() {
////                                        refresh(mCurrentMovie, videoFileList);
////                                    }
////                                });
////                            }
////                        }
////                    }
////                }
//
//        }).start();

    }

    /**
     * for MODE_EDIT
     *
     * @param simpleMovie
     */

    private void getMovie(final SimpleMovie simpleMovie) {
        Log.v(TAG, "getMovie for MODE_EDIT");
        startLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String newMovieId = simpleMovie.getId();
                String newAlt = simpleMovie.getAlt();

                String oldMovieId = mCurrentMovie.getMovieId();
                if (!newMovieId.equals(oldMovieId)) {
                    //3.根据电影ID获取电影
                    Movie movie = null;
                    if (mScanService != null)
                        movie = mScanService.getMovie(newMovieId, newAlt, false, MovieScanService.MODE_SEARCH_SERVICE, ConstData.Scraper.MTIME);
                    if (movie == null) {
                        showTipsDialog(getResources().getString(R.string.network_error));
                        return;
                    }

                    //保存新电影
                    Cursor cursor = mMovieDao.select("movie_id=?", new String[]{newMovieId}, null);
                    if (cursor.getCount() > 0) {
                        movie = mMovieDao.parseList(cursor).get(0);
                        movie.setWrapper_id(mCurrentWrapper.getId());
                        mMovieDao.update(mMovieDao.parseContentValues(movie), "id=?", new String[]{String.valueOf(movie.getId())});
                    } else {
                        movie.setWrapper_id(mCurrentWrapper.getId());
                        long rowId = mMovieDao.insert(mMovieDao.parseContentValues(movie));
                        if (rowId > 0) {
                            movie.setId(rowId);
                        }
                    }


                    if (oldMovieId == null) {//对应无电影的文件外壳
                        ScraperInfo[] scraperInfos = new ScraperInfo[1];
                        ScraperInfo scraperInfo = new ScraperInfo();
                        scraperInfo.setApi(movie.getApi());
                        scraperInfo.setId(movie.getId());
                        scraperInfos[0] = scraperInfo;
                        mCurrentWrapper.setScraperInfos(scraperInfos);
                    } else {
                        //删除替换掉的旧电影.
                        mMovieDao.delete("id=?", new String[]{String.valueOf(mCurrentMovie.getId())});
                        ScraperInfo[] scraperInfos = mCurrentWrapper.getScraperInfos();
                        for (int i = 0; i < scraperInfos.length; i++) {
                            //用新电影替换旧电影
                            if (scraperInfos[i].getId() == mCurrentMovie.getId()) {
                                scraperInfos[i].setId(movie.getId());
                                scraperInfos[i].setApi(movie.getApi());
                            }
                        }
                    }

                    mCurrentWrapper.setTitle(movie.getTitle());
                    mCurrentWrapper.setTitlePinyin(MyPinyinParseAndMatchUtil.parsePinyin(movie.getTitle()));
                    if (movie.getImages() != null)
                        mCurrentWrapper.setPoster(movie.getImages().getLarge());
                    if (movie.getRating() != null)
                        mCurrentWrapper.setAverage(String.valueOf(movie.getRating().average));
                    else
                        mCurrentWrapper.setAverage(getString(R.string.rate_not));

                    mMovieWrapperDao.update(mMovieWrapperDao.parseContentValues(mCurrentWrapper), "id=?", new String[]{String.valueOf(mCurrentWrapper.getId())});
                    mCurrentMovie = movie;
                    isParseOver = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setResult(1);
                            refresh(mCurrentMovie);
                        }
                    });

                } else {
                    isParseOver = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setResult(1);
                            refresh(mCurrentMovie);
                        }
                    });
                }
            }
        }).start();

    }


    private void getMovieNotSave(final SimpleMovie simpleMovie, final Movie movie, final VideoFile videoFile) {
        Log.v(TAG, "getMovieNotSave");
        startLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                videoFileList.clear();
                Movie basemovie = DoubanApi
                        .parserBaseMovieInfo(simpleMovie);// 解析基本电影信息
                Movie newMovie = DoubanApi
                        .parserMovieInfo(basemovie);// 解析电影信息(耗时几秒)
                if (newMovie == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopLoading();
                            Toast.makeText(MovieDetailActivity.this, "获取电影信息失败！", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                videoFileList.add(videoFile);
                mCurrentMovie = newMovie;
                isParseOver = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refresh(mCurrentMovie);
                    }
                });
            }
        }).start();


    }


    private void refresh(Movie movie) {
        Rating rating = movie.getRating();
        if (rating != null) {
            mRateBar.setVisibility(View.VISIBLE);
            mRateBar.setMax(rating.max);
            if (rating.average == -1) {
                mRateBar.setRating(0);
                mRateTv.setText(null);
                mRateCountTv.setText(R.string.rate_not);
            } else {
                mRateBar.setRating(rating.average * 5 / rating.max);
                mRateTv.setText(String.valueOf(rating.average));
                mRateCountTv.setText(getString(R.string.rate_full,
                        String.valueOf(movie.getRatingsCount())));
            }
        } else {
            mRateBar.setVisibility(View.GONE);
        }
        Log.v(TAG, "refresh getMax:" + mRateBar.getMax());
        Log.v(TAG, "refresh getRating:" + mRateBar.getRating());
        Log.v(TAG, "refresh getNumStars:" + mRateBar.getNumStars());
        Log.v(TAG, "refresh getStepSize:" + mRateBar.getStepSize());
        Log.v(TAG, "MovieTile[" + movie.getTitle() + "]");


        if (movie.getSubtype() != null && movie.getSubtype().equals(ConstData.MovieSubType.TV)) {
            String episodes = movie.getEpisodes();
            if (TextUtils.isEmpty(episodes)) {
                mDuration.setVisibility(View.GONE);
            } else {
                mDuration.setText(episodes);
                mDuration.setVisibility(View.VISIBLE);
            }
        } else {
            String durations = StrUtils.arrayToString(movie.getDurations());
            if (TextUtils.isEmpty(durations)) {
                mDuration.setVisibility(View.GONE);
            } else {
                mDuration.setText(durations);
                mDuration.setVisibility(View.VISIBLE);
            }

        }


        String countries = StrUtils.arrayToString(movie.getCountries());
        if (TextUtils.isEmpty(countries)) {
            mPubArea.setVisibility(View.GONE);
        } else {
            mPubArea.setText(countries);
            mPubArea.setVisibility(View.VISIBLE);
        }


        mTitleTv.setText(movie.getTitle());


        String languages = StrUtils.arrayToString(movie.getLanguages());
        if (TextUtils.isEmpty(languages)) {
            mLanguagesTv.setVisibility(View.GONE);
        } else {
            mLanguagesTv.setText(languages);
            mLanguagesTv.setVisibility(View.VISIBLE);
        }


        String casts = StrUtils.arrayToString(movie.getCasts());
        if (TextUtils.isEmpty(casts)) {
            mActorsGroup.setVisibility(View.GONE);
        } else {
            mActorsTv.setText(casts);
            mActorsGroup.setVisibility(View.VISIBLE);
        }

        String genres = StrUtils.arrayToString(movie.getGenres());
        if (TextUtils.isEmpty(genres)) {
            mGenresTv.setVisibility(View.GONE);
        } else {
            mGenresTv.setText(genres);
            mGenresTv.setVisibility(View.VISIBLE);
        }

        String years = movie.getYear();
        if (TextUtils.isEmpty(years)) {
            mPubDatesTv.setVisibility(View.GONE);
        } else {
            mPubDatesTv.setText(years);
            mPubDatesTv.setVisibility(View.VISIBLE);
        }

        mDetailTv.setText(movie.getSummary() != null ? movie.getSummary().replaceAll(" " + (char) 0x3000 + (char) 0x3000, "\r\n" + (char) 0x3000 + (char) 0x3000) : getResources().getString(R.string.tips_none));//经过查阅unicode为12288字符为全角空格

        try {
            if (movie.getImages() != null) {
                Glide.with(this).load(movie.getImages().large)
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default)).into(mCoverIv);
            } else {
                Glide.with(this).load(R.mipmap.ic_poster_default)
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default)).into(mCoverIv);
            }

            if (movie.getPhotos() != null && movie.getPhotos().length > 0) {
                Photo[] photos = movie.getPhotos();
                Log.v(TAG, "photos length=" + photos.length);
                // [0,leng-1]
                random = new Random().nextInt(photos.length);
                String url = photos[random].getImageUrl();
                Glide.with(this).load(url)
                        .apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default)).into(mPosterIv);
            } else {
                Glide.with(this).load(R.mipmap.ic_poster_default).apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default)).into(mPosterIv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        refreshFavroite();
        stopLoading();
    }


    public OnClickListener mClickListener = new OnClickListener() {

        @Override
        public void onClick(final View view) {
            if (view == mBtnEdit) {
                final MovieEditFragment movieEditFragment = MovieEditFragment.newInstance();
                final Movie movie = mCurrentMovie;
                final HashMap<String, Object> map = new HashMap<>();

                String title = movie.getTitle();
                Rating rating = movie.getRating();
                String score = "";
                if (rating == null || rating.average == -1) {
                    score = getResources().getString(R.string.rate_not);
                } else {
                    score = String.valueOf(rating.average);
                }
                String img = null;
                if (movie.getImages() != null) {
                    img = movie.getImages().large;
                }
                List<String> pathlist = new ArrayList<>();
                for (VideoFile videoFile : videoFileList) {
                    pathlist.add(videoFile.getUri());
                }
                String[] paths = pathlist.toArray(new String[0]);
                String[] genres = movie.getGenres();
                map.put(MovieEditFragment.TITLE, title);
                map.put(MovieEditFragment.SCORE, score);
                map.put(MovieEditFragment.IMG, img);
                map.put(MovieEditFragment.PATH, paths);
                map.put(MovieEditFragment.GENRES, genres);

                movieEditFragment.setPositiveListener(new MovieEditFragment.PositiveListener() {
                    @Override
                    public void OnPositivePress(View v) {
                        movie.setTitle(movieEditFragment.getFilename());

                        mCurrentMovie = movie;
                        if (mScanService != null && mScanService.isRunning()) {
                            showIsScanningDialog();
                        } else {
                            Intent intent = new Intent(MovieDetailActivity.this,
                                    MovieSearchResultActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt("mode", ConstData.MovieDetailMode.MODE_EDIT);
                            bundle.putString("keyword", movieEditFragment.getFilename());
                            bundle.putInt("api", mCurrentMovie.getApi());
                            intent.putExtras(bundle);
                            startActivityForResult(intent, 0);
                        }

                    }
                });
                movieEditFragment.setInfo(map).show(getFragmentManager(), TAG);

            } else if (view == mTrailerBtn && isParseOver) {// 需要等待电影解析完毕才能解析预告片
                startLoading();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Movie movie = mCurrentMovie;
                        long id = movie.getId();
                        if (id > 0) {
                            Cursor cursor = mMovietrailerDao.select(null, "movie_id=?",
                                    new String[]{String.valueOf(id)});
                            if (cursor.getCount() > 0) {
                                movieTrailerAdapter.removeAll();
                                trailers_list = mMovietrailerDao.parseList(cursor);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showMovieTrailer();
                                        movieTrailerAdapter.addItems(trailers_list);
                                    }
                                });
                                return;//返回
                            }
                            int api = mCurrentMovie.getApi();
                            switch (api) {
                                case ConstData.Scraper.DOUBAN:
                                    DoubanApi.parseMovieTrailerInfo(
                                            mCurrentMovie, trailers_list);
                                    break;
                                case ConstData.Scraper.MTIME:
                                    MtimeApi.parseMovieTrailerInfo(mCurrentMovie.getId(), mCurrentMovie.getMovieId(), trailers_list);
                                    break;
                            }

                            Log.v(TAG,
                                    "trailers_list.size()="
                                            + trailers_list.size());
                            if (trailers_list.size() > 0) {
                                try {
                                    Log.v(TAG, "time ===>save data begin ");
                                    for (MovieTrailer movieTrailer : trailers_list) {
                                        ContentValues contentValues = mMovietrailerDao
                                                .parseContentValues(movieTrailer);
                                        mMovietrailerDao.insert(contentValues);
                                    }
                                    Log.v(TAG, "time ===>save data end ");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        movieTrailerAdapter.notifyDataSetChanged();
                                        showMovieTrailer();
                                    }
                                });

                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MovieDetailActivity.this, "找不到预告片", Toast.LENGTH_SHORT).show();
                                        stopLoading();
                                    }
                                });
                            }
                        }

                    }

                }).start();


            } else if (view == mBtnPlay) {

                if (mCurrentWrapper != null) {
                    if (videoFileList.size() == 1) {
                        startLoading();
                        playingVideo(videoFileList.get(0));
                    } else if (videoFileList.size() > 1) {
                        showRadioDialog(new CustomRadioDialogFragment.DialogSetting() {
                            @Override
                            public void doPositiveClick(Object obj) {
                                startLoading();
                                VideoFile file = (VideoFile) obj;
                                playingVideo(file);
                            }

                            @Override
                            public void doItemSelect(Object obj) {
                                this.doPositiveClick(obj);
                            }

                            @Override
                            public VideoFile[] getVideoFiles() {
                                List<VideoFile> tmplist = new ArrayList<>();
                                for (int i = 0; i < videoFileList.size(); i++) {
                                    tmplist.add(videoFileList.get(i));
                                }
                                return tmplist.toArray(new VideoFile[0]);
                            }
                        });
                    }
                }


            } else if (view == mBtnRemove) {
                ConfirmDialogFragment confirmDialogFragment = new ConfirmDialogFragment(MovieDetailActivity.this);
                confirmDialogFragment.setPositiveButton(new ConfirmDialogFragment.OnPositiveListener() {
                    @Override
                    public void OnPositivePress(Button button) {
                        removeMovie();
                    }
                }, true).setMessage(getResources().getString(R.string.remove_confirm)).show(getFragmentManager(), TAG);

            } else if (view == mBtnFavorite) {
                toggleFavroite();
            }
        }

    };


    OnRecyclerViewItemClickListener mCenterItemClickListener = new OnRecyclerViewItemClickListener() {
        @Override
        public void onItemClick(View view, MovieTrailer data) {
            final String pageUrl = data.getAlt();
            startLoading();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String videoUrl = "";
                    switch (mCurrentMovie.getApi()) {
                        case ConstData.Scraper.DOUBAN:
                            videoUrl = DoubanApi.parseTrailerUrl(pageUrl);
                            break;
                        case ConstData.Scraper.MTIME:
                            videoUrl = pageUrl;
                            break;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopLoading();
                        }
                    });
                    if (videoUrl != null && !videoUrl.equals("")) {
//                        Intent intent = new Intent(MovieDetailActivity.this,
//                                MovieTrailerPlayerActivity.class);
//                        intent.putExtra("url", videoUrl);
//                        startActivity(intent);

                        Intent intent = new Intent();
                        intent.setAction("firefly.intent.action.PLAY_VIDEO");
                        intent.setDataAndType(Uri.parse(videoUrl), "video/*");
                        try {
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                Log.v(TAG, "----");
                                startActivity(intent);
                                stopLoading();
                            } else {
                                startActivity(intent);
                                Log.v(TAG, "2----");
                                stopLoading();
                            }
                        } catch (Exception e) {
                            stopLoading();
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        }
    };

    /**
     * 播放视频
     *
     * @param file 路径
     */
    public void playingVideo(final VideoFile file) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String poster = mCurrentWrapper.getPoster();
                    if (poster != null) {
                        mPosterDao.deleteAll();
                        PosterProviderBean posterProviderBean = new PosterProviderBean();
                        posterProviderBean.setPoster(poster);
                        ContentValues values = mPosterDao.parseContentValues(posterProviderBean);
                        mPosterDao.insert(values);
                    }
                    Cursor historyCursor = mHistoryDao.select("wrapper_id=?", new String[]{String.valueOf(mCurrentWrapper.getId())}, null);
                    if (historyCursor.getCount() > 0) {
                        long currentTime = System.currentTimeMillis();
                        History history = mHistoryDao.parseList(historyCursor).get(0);
                        history.setLast_play_time(String.valueOf(currentTime));
                        ContentValues contentValues = mHistoryDao.parseContentValues(history);
                        mHistoryDao.update(contentValues, "id=?", new String[]{String.valueOf(history.getId())});
                    } else {
                        long currentTime = System.currentTimeMillis();
                        History history = new History();
                        history.setWrapper_id(mCurrentWrapper.getId());
                        history.setTime("0");
                        history.setLast_play_time(String.valueOf(currentTime));
                        ContentValues contentValues = mHistoryDao.parseContentValues(history);
                        mHistoryDao.insert(contentValues);
                    }

                }
            }).start();
            VideoPlayTools.play(MovieDetailActivity.this, file);

        } catch (Exception e) {

        } finally {
            stopLoading();
        }
    }

    /**
     * 删除电影和电影文件信息
     */
    private void removeMovie() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ScraperInfo[] scraperInfos = mCurrentWrapper.getScraperInfos();
                long wrapper_id = mCurrentWrapper.getId();
                if (scraperInfos != null && scraperInfos.length > 0) {
                    int rowId = mMovieDao.delete("wrapper_id=?", new String[]{String.valueOf(wrapper_id)});
                    if (rowId > 0) {
                        mCurrentWrapper.setScraperInfos(null);
                        Cursor cursor = mVideoFileDao.select("wrapper_id=?", new String[]{String.valueOf(wrapper_id)}, null);
                        if (cursor.getCount() > 0) {
                            int size = cursor.getCount();
                            Long[] f_ids = new Long[size];
                            List<VideoFile> videoFiles = mVideoFileDao.parseList(cursor);
                            if (videoFiles.size() > 0) {
                                for (int i = 0; i < size; i++) {
                                    f_ids[i] = videoFiles.get(i).getId();
                                }
                                VideoFile file = videoFiles.get(0);
                                MovieNameInfo mni = FileScanUtil.simpleParse(file.getFilename());
                                mCurrentWrapper.setTitle(mni.getName());
                                mCurrentWrapper.setPoster(null);
                                mCurrentWrapper.setAverage("-1");
                                mMovieWrapperDao.update(mMovieWrapperDao.parseContentValues(mCurrentWrapper), "id=?", new String[]{String.valueOf(wrapper_id)});
                            }
                        }

                    }
                    setResult(1);
                    buildNullMovie(mCurrentWrapper);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refresh(mCurrentMovie);
                            Toast.makeText(MovieDetailActivity.this, getResources().getString(R.string.toast_del_success), Toast.LENGTH_SHORT).show();

                        }
                    });
                } else {
                    long rowId = mVideoFileDao.delete("wrapper_id=?", new String[]{String.valueOf(wrapper_id)});
                    if (rowId > 0) {
                        mFavoriteDao.delete("wrapper_id=?", new String[]{String.valueOf(wrapper_id)});
                        mHistoryDao.delete("wrapper_id=?", new String[]{String.valueOf(wrapper_id)});
                        rowId = mMovieWrapperDao.delete("id=?", new String[]{String.valueOf(wrapper_id)});
                        if (rowId > 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MovieDetailActivity.this, getResources().getString(R.string.toast_del_success), Toast.LENGTH_SHORT).show();
                                }
                            });
                            setResult(1);
                            finish();
                        }
                    }

                }

            }
        }).start();


    }


    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mScanService = ((MovieScanService.ScanBinder) service).getService();
            refreshMovieInfo(getIntent());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mScanService = null;
        }
    };


    private int backclik = 0;

    /**
     * 显示电影预告片
     */
    private void showMovieTrailer() {
        stopLoading();
        mMovieTrailerRL.setVisibility(RelativeLayout.VISIBLE);
    }

    /**
     * 隐藏电影预告片
     */
    private void hideMovieTrailer() {
        movieTrailerAdapter.removeAll();
        mMovieTrailerRL.setVisibility(RelativeLayout.GONE);
    }

    /**
     * 判断预告片弹出框是否显示
     *
     * @return
     */
    private boolean isMovieTrailerShowing() {
        return mMovieTrailerRL.getVisibility() == RelativeLayout.VISIBLE;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (isMovieTrailerShowing())
                    if (mTrailerCircleRecyelerView.findFocus() == null) {
                        mTrailerCircleRecyelerView.getChildAt(0).requestFocus();
                        return true;
                    } else {
                        if (mTrailerCircleRecyelerView.getChildAt(0).isFocused()) {
                            return false;
                        }
                    }

            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (isMovieTrailerShowing()) {
                    int postion = mTrailerCircleRecyelerView.getChildCount() - 1;
                    if (mTrailerCircleRecyelerView.findFocus() == null) {
                        mTrailerCircleRecyelerView.getChildAt(postion).requestFocus();
                        return true;
                    } else {

                        if (mTrailerCircleRecyelerView.getChildAt(postion).isFocused()) {
                            return false;
                        }
                    }
                }
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                if (isMovieTrailerShowing()) {
                    return true;
                }

            }
        }


        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (isMovieTrailerShowing())
            hideMovieTrailer();
        else {
            finish();
        }

    }

    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;

        if (isExit == false) {

            isExit = true; // 准备退出
            Toast.makeText(MovieDetailActivity.this, "再按一次返回键退出程序",
                    Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            mApplication.exit();
            // System.exit(0);直接 System.exit 导致destory函数未被调用
        }
    }

    /**
     * 通过url解析影片封面
     */
    private Bitmap createVideoThumbnail(String url, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int kind = MediaStore.Video.Thumbnails.MINI_KIND;
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(url, new HashMap<String, String>());
            } else {
                retriever.setDataSource(url);
            }
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (kind == Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    public void showRadioDialog(CustomRadioDialogFragment.DialogSetting dialogSetting) {
        CustomRadioDialogFragment dialogFragment = CustomRadioDialogFragment.newInstance(dialogSetting);
        dialogFragment.show(getFragmentManager(), "detail");
    }

    private CustomRadioDialogFragment checkGroupDialogFragment;

    public void showCheckGroupDialog(CustomRadioDialogFragment.DialogSetting dialogSetting) {
        checkGroupDialogFragment = CustomRadioDialogFragment.newInstance(dialogSetting, CustomRadioDialogFragment.TYPE_CHECKBOX);
        checkGroupDialogFragment.setTitle(getResources().getString(R.string.tx_choose_video));
        checkGroupDialogFragment.setConfirmText(getResources().getString(R.string.tx_choose_confirm));
        checkGroupDialogFragment.show(getFragmentManager(), "edit_list");
    }

    public void showIsScanningDialog() {
        ConfirmDialogFragment dialogFragment = new ConfirmDialogFragment(MovieDetailActivity.this);
        dialogFragment.setMessage(getResources().getString(R.string.dialog_is_scanning));
        dialogFragment.show(getFragmentManager(), "MovieDetail");
    }

    public void showTipsDialog(String tips) {
        ConfirmDialogFragment dialogFragment = new ConfirmDialogFragment(MovieDetailActivity.this);
        dialogFragment.setMessage(tips);
        dialogFragment.show(getFragmentManager(), "MovieDetail");
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            int mode = data.getIntExtra("mode", ConstData.MovieDetailMode.NORMAL_FOR_V_ID);
            currentMode = mode;

            if (currentMode == ConstData.MovieDetailMode.MODE_EDIT) {
                ButtonEnable(mBtnEdit, true);
                getMovie((SimpleMovie) data.getSerializableExtra("simplemovie"));
            }


        } else {

        }
    }

    /**
     * 刷新详情页面收藏状态。
     */
    private void refreshFavroite() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long id = mCurrentWrapper.getId();
                Cursor cursor = null;
                if (id > 0) {
                    cursor = mFavoriteDao.select("wrapper_id=?", new String[]{String.valueOf(id)}, null);
                    if (cursor.getCount() > 0) {
                        List<Favorite> favoriteList = mFavoriteDao.parseList(cursor);
                        if (favoriteList.size() > 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mBtnFavorite.setFavoriteState(true);
                                }
                            });
                        }
                    }
                } else {

                }
            }
        }).start();

    }

    private void toggleFavroite() {
        startLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                long id = mCurrentWrapper.getId();

                Cursor cursor = null;
                if (id > 0) {
                    cursor = mFavoriteDao.select("wrapper_id=?", new String[]{String.valueOf(id)}, null);
                    if (cursor != null) {
                        List<Favorite> favoriteList = mFavoriteDao.parseList(cursor);
                        long count;
                        if (favoriteList.size() > 0) {
                            count = mFavoriteDao.delete("wrapper_id=?", new String[]{String.valueOf(id)});
                            if (count > 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mBtnFavorite.setFavoriteState(false);
                                    }
                                });
                            }
                        } else {
                            Favorite favorite = new Favorite();
                            favorite.setWrapper_id(id);
                            ContentValues values = mFavoriteDao.parseContentValues(favorite);
                            count = mFavoriteDao.insert(values);
                            if (count > 0) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mBtnFavorite.setFavoriteState(true);
                                    }
                                });
                            }
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopLoading();
                    }
                });
            }
        }).start();

    }


}
