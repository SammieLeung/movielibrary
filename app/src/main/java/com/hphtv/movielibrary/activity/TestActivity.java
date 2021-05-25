package com.hphtv.movielibrary.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.ActivityTestBinding;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.hphtv.movielibrary.service.MovieScanService2;
import com.hphtv.movielibrary.util.LogUtil;
import com.hphtv.movielibrary.util.retrofit.MtimeAPIRequest;
import com.hphtv.movielibrary.util.retrofit.MtimeSearchRespone;
import com.hphtv.movielibrary.util.retrofit.RetrofiTools;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * author: Sam Leung
 * date:  2021/5/25
 */
public class TestActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityTestBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_test);
        mBinding.btnTestservice.setOnClickListener(this);
        mBinding.btnUnbind.setOnClickListener(this);
        mBinding.btnTestservice2.setOnClickListener(this);
        mBinding.btnUnbind2.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_testservice:
                Intent intent=new Intent();
                intent.setClass(this,DeviceMonitorService.class);
                bindService(intent,mServiceConnection, Context.BIND_AUTO_CREATE);
                break;
            case R.id.btn_unbind:
                unbindService(mServiceConnection);
                break;
            case R.id.btn_testservice2:
                Intent intent2=new Intent();
                intent2.setClass(this, MovieScanService2.class);
                bindService(intent2,mServiceConnection2, Context.BIND_AUTO_CREATE);
                break;
            case R.id.btn_unbind2:
                unbindService(mServiceConnection2);
                break;
        }
    }

    private void testRetrofi(){
        new Thread(new Runnable() {
                        @Override
            public void run() {
            MtimeAPIRequest test= RetrofiTools.createMtimeRequest();
            Call<MtimeSearchRespone> call=test.searchMovieByMtime("钢铁侠",1);
                call.enqueue(new Callback<MtimeSearchRespone>() {
                @Override
                public void onResponse(Call<MtimeSearchRespone> call, Response<MtimeSearchRespone> response) {
                    MtimeSearchRespone mtimeSearchRespone=response.body();
                    for(Movie movie:mtimeSearchRespone.toEntity()) {
                        LogUtil.v("lxp", movie.toString());
                    }
                }

                @Override
                public void onFailure(Call<MtimeSearchRespone> call, Throwable t) {

                }
            });
            }
        }).start();
    }

    ServiceConnection mServiceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    ServiceConnection mServiceConnection2=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MovieScanService2 movieScanService2=((MovieScanService2.ScanBinder)service).getService();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    VideoFileDao videoFileDao= MovieLibraryRoomDatabase.getDatabase(movieScanService2).getVideoFileDao();
                   List<VideoFile> videoFiles= videoFileDao.queryAll();
                   movieScanService2.start(videoFiles);

                }
            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
