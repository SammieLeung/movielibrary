package com.hphtv.movielibrary.ui.homepage

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.hphtv.movielibrary.util.MovieHelper
import com.hphtv.movielibrary.util.rxjava.SimpleObserver

class PlayVideoReceiver(
    val context: Context,
    val refreshAction: (() -> Unit)? = null,
    val unregisterAction: (() -> Unit)? = null
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (ACTION_PLAYER_CALLBACK == action) {
            var path: String? = null
            var position: Long = 0
            var duration: Long = 0
            if (intent.hasExtra(EXTRA_PATH)) {
                path = intent.getStringExtra(EXTRA_PATH)
            }
            if (intent.hasExtra(EXTRA_POSITION)) {
                position = intent.getLongExtra(EXTRA_POSITION, 0)
            }
            if (intent.hasExtra(EXTRA_DURATION)) {
                duration = intent.getLongExtra(EXTRA_DURATION, 0)
            }
            Log.w(
                BaseAutofitHeightFragment.TAG,
                "onReceive: $path $position/$duration"
            )
            MovieHelper.updateHistory(path, position, duration)
                .subscribe(object : SimpleObserver<String?>() {

                    override fun onAction(t: String?) {
                        refreshAction?.invoke()
                    }
                })
        }
        unregisterAction?.invoke()
    }

    companion object {
        const val ACTION_PLAYER_CALLBACK = "com.firefly.video.player"
        const val EXTRA_PATH = "video_address"
        const val EXTRA_POSITION = "video_position"
        const val EXTRA_DURATION = "video_duration"
    }
}