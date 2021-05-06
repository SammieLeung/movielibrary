package com.hphtv.movielibrary.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hphtv.movielibrary.R;
//import com.hphtv.movielibrary.activity.MovieDetailActivity;
import com.hphtv.movielibrary.activity.HomePageActivity;
import com.hphtv.movielibrary.activity.MovieDetailActivity;
import com.hphtv.movielibrary.adapter.MovieLibraryAdapter;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.sqlite.bean.Directory;
import com.hphtv.movielibrary.sqlite.bean.MovieWrapper;
import com.hphtv.movielibrary.sqlite.bean.ScraperInfo;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Movie;
import com.hphtv.movielibrary.sqlite.dao.DirectoryDao;
import com.hphtv.movielibrary.sqlite.dao.MovieDao;
import com.hphtv.movielibrary.sqlite.dao.MovieWrapperDao;
import com.hphtv.movielibrary.view.RecyclerViewWithMouseScroll;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author lxp
 * @date 19-5-15
 */
public class HomePageFragment extends Fragment {
    private static final int COLUMS = 6;

    public static final String TAG = HomePageFragment.class.getSimpleName();
    View mContentView;
    private RecyclerViewWithMouseScroll mRecyclerView;
    private TextView mTextViewTips;

    private Context mContext;
    private MovieLibraryAdapter mLibraryAdapter;// 电影列表适配器
    private List<MovieWrapper> mWrapperList = new ArrayList<>();// 电影数据

    private MovieWrapperDao mWrapperDao;
    private MovieDao mMovieDao;
    private DirectoryDao mDirDao;
    private static AtomicBoolean atomicBoolean = new AtomicBoolean();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.f_layout_favorite, container, false);
        return mContentView;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        initMovie();
    }

    public void initMovie() {
        Log.v(TAG, "initMovie");
        getMovieData();
    }

    /**
     * 初始化
     */
    private void initView(View view) {
        mContext = getActivity();
        mWrapperDao = new MovieWrapperDao(mContext);
        mMovieDao = new MovieDao(mContext);
        mDirDao = new DirectoryDao(mContext);

        mTextViewTips = (TextView) view.findViewById(R.id.tips_empty);
        mRecyclerView = (RecyclerViewWithMouseScroll) view.findViewById(R.id.rv_movies);
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(mContext, COLUMS, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mLibraryAdapter = new MovieLibraryAdapter(mContext, mWrapperList);
        mRecyclerView.setAdapter(mLibraryAdapter);
        mLibraryAdapter
                .setOnItemClickListener(new MovieLibraryAdapter.OnRecyclerViewItemClickListener() {
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

    }

    private void getMovieData() {
        if (!atomicBoolean.get()) {
            Log.v(TAG, "getMovieData HomePage");
            atomicBoolean.set(true);
            final HomePageActivity ac = (HomePageActivity) mContext;
            ac.startLoading();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mWrapperList.clear();
                    boolean isShowUnMatched = false;
                    long dev_id = ac.getFilterDeviceId();
                    long dir_id = ac.getFilterDirId();
                    String genres = ac.getFilterGenres();
                    String year = ac.getFilterYear();
                    String order_sql = ac.getOrderBySqlStr();
                    boolean isShowEncrypted = ac.isShowEncrypted();
                    boolean isSkip = false;//为true代表目录全部隐藏.
                    StringBuffer buffer = new StringBuffer();
                    List<String> argList = new ArrayList<>();

                    if (genres.isEmpty()) {
                        genres = ac.mTagAll;
                    }
                    if (year.isEmpty()) {
                        year = ac.mTagAll;
                    }
                    if (genres.equals(ac.mTagAll) && year.equals(ac.mTagAll)) {
                        isShowUnMatched = true;
                    } else {
                        isShowUnMatched = false;
                        if (!genres.equals(ac.mTagAll)) {
                            String id = mMovieDao.getGenresId(genres);
                            buffer.append("genres like '%\"" + id + "\"%' and ");
                        }
                        if (!year.equals(ac.mTagAll)) {
                            String[] sortYears=year.split(" - ");
                            if(sortYears.length>1){
                                buffer.append("year<=? and year >=? and");
                                argList.add(sortYears[1]);
                                argList.add(sortYears[0]);
                            }else{
                                buffer.append("year=? and");
                                argList.add(year);
                            }

                        }
                    }


                    if (buffer.length() > 4)
                        buffer.replace(buffer.lastIndexOf(" and"), buffer.length(), "");

                    //1===>获取已匹配的所有符合筛选条件的电影。
                    Cursor movieCursor = mMovieDao.select(null, buffer.toString(), argList.toArray(new String[0]), null, null, order_sql, null);
                    if (movieCursor.getCount() > 0) {
                        List<Movie> movies = mMovieDao.parseList(movieCursor);
                        buffer = new StringBuffer();
                        //特定目录查询条件
                        if (dir_id != -1) {
                            buffer.append("dir_ids like '%" + dir_id + "%' and ");
                        } else {
                            //特点设备查询条件
                            if (dev_id != -1) {
                                //不显示私密
                                if (!isShowEncrypted) {
                                    //获取属于特定设备并且是公开的目录
                                    Cursor dirCursor = mDirDao.select("is_encrypted=? and parent_id=?", new String[]{"0", String.valueOf(dev_id)}, null);
                                    if (dirCursor.getCount() > 0) {
                                        List<Directory> directories = mDirDao.parseList(dirCursor);
                                        buffer.append("(");
                                        for (Directory t_dir : directories) {
                                            buffer.append("(dir_ids like '%" + t_dir.getId() + "%' or dir_ids like '%" + t_dir.getId() + "]%') or ");
                                        }
                                        buffer.replace(buffer.lastIndexOf(" or"), buffer.length(), ") and ");
                                    } else {
                                        //一个非隐私目录都没有则不允许查询
                                        isSkip = true;
                                    }
                                } else {
                                    Cursor dirCursor = mDirDao.select("parent_id=?", new String[]{String.valueOf(dev_id)}, null);
                                    if (dirCursor.getCount() > 0) {
                                        List<Directory> directories = mDirDao.parseList(dirCursor);
                                        buffer.append("(");
                                        for (Directory t_dir : directories) {
                                            buffer.append("(dir_ids like '%" + t_dir.getId() + "%' or dir_ids like '%" + t_dir.getId() + "]%') or ");
                                        }
                                        buffer.replace(buffer.lastIndexOf(" or"), buffer.length(), ") and ");
                                    }
                                }

                            } else {
                                //代表设备筛选条件为全部
                                if (!isShowEncrypted) {
                                    //没有显示隐藏目录时
                                    Cursor dirCursor = mDirDao.select("is_encrypted=?", new String[]{"0"}, null);
                                    if (dirCursor.getCount() > 0) {
                                        List<Directory> directories = mDirDao.parseList(dirCursor);
                                        buffer.append("(");
                                        for (Directory t_dir : directories) {
                                            buffer.append("(dir_ids like '%" + t_dir.getId() + "%' or dir_ids like '%" + t_dir.getId() + "]%') or ");
                                        }
                                        buffer.replace(buffer.lastIndexOf(" or"), buffer.length(), ") and ");
                                    } else {
                                        //一个非隐私目录都没有则不允许查询
                                        isSkip = true;
                                    }
                                }
                            }
                        }
                        if (!isSkip) {
                            buffer.append("id=?");
                            for (Movie movie : movies) {
                                Cursor wrapperCursor = mWrapperDao.select(buffer.toString(), new String[]{String.valueOf(movie.getWrapperId())}, null);
                                if (wrapperCursor.getCount() > 0) {
                                    MovieWrapper wrapper = mWrapperDao.parseList(wrapperCursor).get(0);
                                    ScraperInfo[] scraperInfos = wrapper.getScraperInfos();
                                    if (scraperInfos != null)
                                        if (scraperInfos[0].getId() == movie.getId())
                                            if (!mWrapperList.contains(wrapper))
                                                mWrapperList.add(wrapper);
                                }

                            }
                        }
                    }
                    //2=====>没有筛选条件才显示未匹配电影
                    if (isShowUnMatched) {
                        buffer = new StringBuffer();
                        //特定目录查询条件
                        if (dir_id != -1) {
                            buffer.append("dir_ids like '%" + dir_id + "%' and ");
                        } else {
                            //特点设备查询条件
                            if (dev_id != -1) {
                                //不显示私密
                                if (!isShowEncrypted) {
                                    //获取属于特定设备并且是公开的目录
                                    Cursor dirCursor = mDirDao.select("is_encrypted=? and parent_id=?", new String[]{"0", String.valueOf(dev_id)}, null);
                                    if (dirCursor.getCount() > 0) {
                                        List<Directory> directories = mDirDao.parseList(dirCursor);
                                        buffer.append("(");
                                        for (Directory t_dir : directories) {
                                            buffer.append("(dir_ids like '%" + t_dir.getId() + "%' or dir_ids like '%" + t_dir.getId() + "]%') or ");
                                        }
                                        buffer.replace(buffer.lastIndexOf(" or"), buffer.length(), ") and ");
                                    } else {
                                        //一个非隐私目录都没有则不允许查询
                                        isSkip = true;
                                    }
                                } else {
                                    Cursor dirCursor = mDirDao.select("parent_id=?", new String[]{String.valueOf(dev_id)}, null);
                                    if (dirCursor.getCount() > 0) {
                                        List<Directory> directories = mDirDao.parseList(dirCursor);
                                        buffer.append("(");
                                        for (Directory t_dir : directories) {
                                            buffer.append("(dir_ids like '%" + t_dir.getId() + "%' or dir_ids like '%" + t_dir.getId() + "]%') or ");
                                        }
                                        buffer.replace(buffer.lastIndexOf(" or"), buffer.length(), ") and ");
                                    }
                                }

                            } else {
                                //代表设备筛选条件为全部
                                if (!isShowEncrypted) {
                                    //没有显示隐藏目录时
                                    Cursor dirCursor = mDirDao.select("is_encrypted=?", new String[]{"0"}, null);
                                    if (dirCursor.getCount() > 0) {
                                        List<Directory> directories = mDirDao.parseList(dirCursor);
                                        buffer.append("(");
                                        for (Directory t_dir : directories) {
                                            buffer.append("(dir_ids like '%" + t_dir.getId() + "%' or dir_ids like '%" + t_dir.getId() + "]%') or ");
                                        }
                                        buffer.replace(buffer.lastIndexOf(" or"), buffer.length(), ") and ");
                                    } else {
                                        //一个非隐私目录都没有则不允许查询
                                        isSkip = true;
                                    }
                                }
                            }
                        }
                        if (!isSkip) {
                            //修改buffer即可

                            buffer.append("scraper_infos=?");
                            String asc = ac.isSortByAsc() ? " asc" : " desc";
                            Cursor wrapperCursor = mWrapperDao.select(null, buffer.toString(), new String[]{"null"}, null, null, "title_pinyin " + asc + ",title " + asc, null);
                            if (wrapperCursor.getCount() > 0) {
                                mWrapperList.addAll(mWrapperDao.parseList(wrapperCursor));
                            }
                        }
                    }
                    mLibraryAdapter.setData(mWrapperList);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshMovie();
                            ac.stopLoading();
                        }
                    });
                    atomicBoolean.set(false);
                }
            }).start();
        }
    }

    private void refreshMovie() {
        if (mWrapperList.size() > 0) {
            mTextViewTips.setVisibility(View.GONE);
        } else {
            mTextViewTips.setVisibility(View.VISIBLE);
        }
        mLibraryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult resultCode=" + resultCode);
    }
}
