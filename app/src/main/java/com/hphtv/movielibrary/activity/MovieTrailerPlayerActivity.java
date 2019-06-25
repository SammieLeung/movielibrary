package com.hphtv.movielibrary.activity;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.view.CustomLoadingCircleView;
import com.hphtv.movielibrary.view.CustomLoadingCircleViewFragment;
import com.hphtv.movielibrary.view.CustomMediaController;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

public class MovieTrailerPlayerActivity extends Activity {
    public static final String TAG = "MovieTrailerPlayer";
    VideoView videoView;
    CustomLoadingCircleViewFragment mLoadingCircleViewDialogFragment;
    MediaController mc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoplayer);
        videoView = (VideoView) findViewById(R.id.vv_player);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        Uri uri = Uri.parse(url);
        mc = new CustomMediaController(this);


        videoView.setMediaController(mc);
        videoView.setVideoPath(url);
        videoView.setOnPreparedListener(onPreparedListener);
        videoView.setOnInfoListener(onInfoListener);
        videoView.setOnClickListener(onClickListener);
        videoView.setOnTouchListener(onTouchListener);
        videoView.setOnCompletionListener(onCompletionListener);
        videoView.requestFocus();
        videoView.start();

    }

    public void changeStateOfScreen(boolean isFullScreen) {
        RelativeLayout.LayoutParams rLP;
        if (isFullScreen) {
            rLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rLP.addRule(RelativeLayout.CENTER_IN_PARENT);
        } else {
            rLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            rLP.addRule(RelativeLayout.CENTER_IN_PARENT);
        }
        videoView.setLayoutParams(rLP);

    }

    OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_UP) {
                mc.show();
                if (videoView.isPlaying()) {
                    videoView.pause();
                } else {
                    videoView.start();
                }
            }
            return true;
        }
    };


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mc.show();
            if (videoView.isPlaying()) {
                videoView.pause();
            } else {
                videoView.start();
            }
        }
    };


    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);
        }
    };
    MediaPlayer.OnInfoListener onInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                Log.v(TAG, "Loading");
                startLoading();
            } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                //此接口每次回调完START就回调END,若不加上判断就会出现缓冲图标一闪一闪的卡顿现象
                if (mp.isPlaying()) {
                    stopLoading();
                    Log.v(TAG, "Stop Loading");
                }
            }
            return true;
        }
    };

    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.v(TAG, "Stop Loading");
            stopLoading();
        }
    };

    private void startLoading() {
        if (mLoadingCircleViewDialogFragment == null) {
            mLoadingCircleViewDialogFragment = new CustomLoadingCircleViewFragment();
            mLoadingCircleViewDialogFragment.show(getFragmentManager(), TAG);
        }
    }

    private void stopLoading() {
        if (mLoadingCircleViewDialogFragment != null) {
            mLoadingCircleViewDialogFragment.dismiss();
            mLoadingCircleViewDialogFragment = null;
        }
    }
}
