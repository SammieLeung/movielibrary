package com.hphtv.movielibrary.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.firefly.dlna.DlnaManager;
import com.firefly.dlna.device.dmr.DmrBuilder;
import com.firefly.dlna.device.dmr.DmrDevice;
import com.firefly.dlna.device.dmr.IPlayer;
import com.firefly.dlna.device.dmr.MetaData;
import com.firefly.dlna.device.dms.DmsBuilder;
import com.firefly.dlna.device.dms.DmsDevice;
import com.firefly.dlna.device.dms.FileItem;
import com.firefly.dlna.device.dms.IFileStore;
import com.firefly.dlna.httpserver.Registration;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.util.VideoPlayTools;

import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.Channel;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.SortCriterion;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.support.model.item.Item;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class DlnaControlService extends Service {
    private final static String TAG = DlnaControlService.class.getSimpleName();

    private DmrDevice mDmrDevice;
    private DmsDevice mDmsDevice;
    private AudioManager mAudioManager;

    private IBinder mBinder = new DBinder();

    public DlnaControlService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DlnaManager dlnaManager = DlnaManager.getInstance();
        dlnaManager.startService(this, new DlnaManager.ServiceConnectionListener() {
            @Override
            public void onConnected(DlnaManager dlnaManager) {
                mDmrDevice = new DmrBuilder()
                        .setName("Station OS Renderer testing")
                        .setFriendlyName("Station OS Renderer testing")
                        .addPlayer(new PlayCallback())
                        .addIcon(createIcon())
                        .create();
                dlnaManager.addDmrDevice(mDmrDevice);

                mDmsDevice = new DmsBuilder()
                        .setName("Station OS DMS testing")
                        .setFriendlyName("Station OS DMS testing")
                        .setFileStore(new FileStore())
                        .addIcon(createIcon())
                        .create();

                dlnaManager.addDmsDevice(mDmsDevice);
            }

            @Override
            public void onDisconnected(DlnaManager dlnaManager) {

            }
        });

        mAudioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        DlnaManager.getInstance().stopService(this);
    }

    Icon createIcon() {
        Icon icon = null;
        try {
            icon = new Icon("image/jpeg",
                    32,
                    32,
                    3,
                    "icon-32",
                    getResources().openRawResource(R.raw.dlna_icon));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return icon;
    }

    class DBinder extends Binder {
        public DlnaControlService get() {
            return DlnaControlService.this;
        }

    }

    class FileStore implements IFileStore {

        @Override
        public FileItem browse(String objectId,
                               BrowseFlag browseFlag,
                               String filter,
                               long firstResult,
                               long maxResults,
                               SortCriterion[] orderBy) {
            FileItem fileItem = new FileItem(objectId);
            if (objectId.equals("0")) {
//                fileItem.newContainer("1", "Images", "GNaP MediaServer", 0);
                fileItem.newContainer("1", "Videos", "GNaP MediaServer", 0);
//            } else if (objectId.equals("1")) {
//                Cursor cursor=getContentResolver().query(MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            } else if (objectId.equals("1")) {
                String selection = MediaStore.Video.Media.DATA;
                Cursor cursor = getContentResolver().query(
                        MediaStore.Video.Media.INTERNAL_CONTENT_URI,
                        null,
                        null,
                        null,
                        MediaStore.Video.Media.DATE_TAKEN + " DESC");
                while (cursor != null && cursor.moveToNext()) {
                    int path_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                    int name_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                    int mime_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
                    int thumb_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MINI_THUMB_MAGIC);
                    int size_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                    int date_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);

                    String name = cursor.getString(name_index);
                    String thumbnailPath = getFilesDir() + "/thumbnails/" + name + ".jpg";
                    File file = new File(thumbnailPath);
                    Log.v(TAG,"thumbnails "+thumbnailPath);
                    if (!file.exists()) {
                        try {
                            Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                                    getContentResolver(),
                                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID)),
                                    MediaStore.Video.Thumbnails.MICRO_KIND,
                                    null);
                            file.getParentFile().mkdirs();
                            OutputStream outputStream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Log.d(TAG, cursor.getString(path_index) + "\n" +
                            cursor.getString(thumb_index) + "\n" +
                            cursor.getString(mime_index) + "\n" +
                            cursor.getString(date_index) + "\n" +
                            cursor.getLong(size_index));

                    Registration[] registration = DlnaManager.getInstance().getServer().registerFile(thumbnailPath);
                    try {
                        Item item = fileItem.newVideoItem(cursor.getString(path_index),
                                name,
                                cursor.getString(path_index),
                                cursor.getString(mime_index),
                                cursor.getLong(size_index),
                                "CREATOR"
                        );
                        if (registration != null && registration.length > 0) {
                            // 添加封面或预览
                            item.addProperty(
                                    new DIDLObject.Property.UPNP.ALBUM_ART_URI(
                                            new URI(registration[0].getUri())
                                    )
                            );
                            // 添加fanart，可用于背景显示
                            item.addResource(new Res(
                                    new ProtocolInfo("xbmc.org:*:fanart:*"), // 或者 http-get:*:fanart:*
                                    null,
                                    registration[0].getUri()
                            ));
                        }

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }


            }

            return fileItem;
        }

        @Override
        public FileItem search(String containerId,
                               String searchCriteria,
                               String filter,
                               long firstResult,
                               long maxResults,
                               SortCriterion[] orderBy) {
            return null;
        }
    }

    class PlayCallback implements IPlayer {
        @Override
        public void setUri(URI uri, @Nullable MetaData metaData) {
            Log.d(TAG, "setUri: " + uri.toString() + ", metaData: " + metaData);
            VideoPlayTools.play(DlnaControlService.this, Uri.parse(uri.toString()));
        }

        @Override
        public void setNextUri(URI uri, @Nullable MetaData metaData) {
            Log.d(TAG, "setNextUri: " + uri.toString() + ", metaData: " + metaData);
        }

        @Override
        public void onPlay(String speed) {
            Log.d(TAG, "onPlay: " + speed);
//            mHandler.sendEmptyMessage(VideoPlayActivity.VIDEO_PLAY);
        }

        @Override
        public void onPause() {
//            mHandler.sendEmptyMessage(VideoPlayActivity.VIDEO_PAUSE);
        }

        @Override
        public void onStop() {
//            mHandler.sendEmptyMessage(VideoPlayActivity.VIDEO_STOP);
        }

        @Override
        public void onNext() {

        }

        @Override
        public void onPrev() {

        }

        @Override
        public void onSeek(int position) {
            Log.d(TAG, "onSeek: position: " + position);
//            Message msg = mHandler.obtainMessage(VideoPlayActivity.VIDEO_SEEK, position, 0);
//            mHandler.sendMessage(msg);
        }

        @Override
        public void setMute(Channel channel, boolean state) {
            if (state) {
                mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_MUTE,
                        AudioManager.STREAM_MUSIC, AudioManager.FLAG_SHOW_UI);
            } else {
                mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_UNMUTE,
                        AudioManager.STREAM_MUSIC, AudioManager.FLAG_SHOW_UI);
            }
        }

        @Override
        public boolean getMute() {
            double v = (double) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            return v == 0;
        }

        @Override
        public void setVolume(Channel channel, int value) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, AudioManager.FLAG_SHOW_UI);
        }

        @Override
        public int getVolume(Channel channel) {
            return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        @Override
        public int getMaxVolume() {
            return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }

        @Override
        public int getMinVolume() {
            return 0;
//            return mAudioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC);
        }

        @Override
        public int getCurrentPosition() {
          return 0;
        }

        @Override
        public int getDuration() {
        return 0;
        }

    }
}
