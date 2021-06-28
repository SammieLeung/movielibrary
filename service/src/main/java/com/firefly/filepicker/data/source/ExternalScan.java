package com.firefly.filepicker.data.source;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.firefly.filepicker.data.bean.FileItem;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by rany on 18-1-9.
 */

public class ExternalScan extends AbstractScanFiles {
    private static final String TAG = ExternalScan.class.getSimpleName();

    private Context mContext;
    private FileFilter mFileFilter;

    public ExternalScan(Context context) {
        mContext = context;
    }

    @Override
    protected void scan() throws ScanException {
        /*
        String key = Utils.getDeviceId(mNode.getId());
        if (!Constants.newDevices.containsKey(key)
                || Constants.newDevices.get(key) == Constants.DEVICE_SCAN_FINISHED) {
            switch (mFilterType) {
                case FileItem.AUDIO:
                    scanAudio();
                    break;
                case FileItem.IMAGE:
                    scanImage();
                    break;
                case FileItem.VIDEO:
                    scanVideo();
                    break;
                default:
                    // TODO support other file type
                    finish();
                    break;
            }
        } else { */
        if (mFilterType != -1 || mFilterArg != null) {
            mFileFilter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.isDirectory()) {
                        return true;
                    }

                    if (mFilterArg != null && !file.getPath().contains(mFilterArg)) {
                        return false;
                    }

                    if (mFilterType != -1) {

                        String mimeType = getMimeType(file);
                        if (mimeType == null) {
                            return false;
                        }

                        switch (mFilterType) {
                            case FileItem.AUDIO:
                                return mimeType.startsWith("audio");
                            case FileItem.IMAGE:
                                return mimeType.startsWith("image");
                            case FileItem.VIDEO:
                                return mimeType.startsWith("video");
                            case FileItem.TEXT:
                                return mimeType.startsWith("text");
                            default:
                                return false;
                        }
                    }

                    return true;
                }
            };
        }

        File file = new File(mNode.getId());

        if (file.isDirectory()) {
            Log.e("lxp", "is file.canRead() " + file.canRead());
            scanRecursive(file);
            finish();
        }
//        }
    }

    private void scanAudio() {
        String selection = MediaStore.Audio.Media.DATA + " like?";
        String[] selectionArgs = new String[]{"%" + mNode.getId() + "%"};
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                selection,
                selectionArgs,
                MediaStore.Audio.Media.DISPLAY_NAME + " DESC");

        while (cursor != null && cursor.moveToNext() && !isCancel) {
            int path_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            int name_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int mime_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE);
            int size_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int date_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED);

            FileItem item = new FileItem(
                    FileItem.IMAGE,
                    cursor.getString(name_index),
                    cursor.getString(path_index),
                    null,
                    cursor.getString(mime_index),
                    cursor.getString(date_index),
                    cursor.getLong(size_index),
                    FileItem.EXTERNAL);

            addResultItem(item);
        }

        Log.d(TAG, mNode.getId());

        if (cursor != null) {
            cursor.close();
        }

        finish();
    }

    private void scanImage() {
        String selection = MediaStore.Images.Media.DATA + " like?";
        String[] selectionArgs = new String[]{"%" + mNode.getId() + "%"};
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                selection,
                selectionArgs,
                MediaStore.Images.Media.DATE_TAKEN + " DESC");

        while (cursor != null && cursor.moveToNext() && !isCancel) {
            int path_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int name_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int mime_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
            int thumb_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MINI_THUMB_MAGIC);
            int size_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            int date_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);

            FileItem item = new FileItem(
                    FileItem.IMAGE,
                    cursor.getString(name_index),
                    cursor.getString(path_index),
                    cursor.getString(thumb_index),
                    cursor.getString(mime_index),
                    cursor.getString(date_index),
                    cursor.getLong(size_index),
                    FileItem.EXTERNAL);

            addResultItem(item);
        }

        Log.d(TAG, mNode.getId());

        if (cursor != null) {
            cursor.close();
        }

        finish();
    }

    private void scanVideo() {
        String selection = MediaStore.Video.Media.DATA + " like?";
        String[] selectionArgs = new String[]{"%" + mNode.getId() + "%"};
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null,
                selection,
                selectionArgs,
                MediaStore.Video.Media.DATE_TAKEN + " DESC");

        while (cursor != null && cursor.moveToNext() && !isCancel) {
            int path_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int name_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int mime_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
            int thumb_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MINI_THUMB_MAGIC);
            int size_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            int date_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);

            FileItem item = new FileItem(
                    FileItem.VIDEO,
                    cursor.getString(name_index),
                    cursor.getString(path_index),
                    cursor.getString(thumb_index),
                    cursor.getString(mime_index),
                    cursor.getString(date_index),
                    cursor.getLong(size_index),
                    FileItem.EXTERNAL);

            addResultItem(item);
        }

        if (cursor != null) {
            cursor.close();
        }

        finish();
    }

    private void scanRecursive(File file) {
        File[] children = file.listFiles(mFileFilter);
        if (children != null)
            for (File f : children) {
                if (f.isFile()) {
                    processFile(f);
                } else {
                    scanRecursive(f);
                }
            }
    }

    private void processFile(File file) {
        String mimeType = getMimeType(file);
        int type = FileItem.OTHER;

        if (mimeType != null) {
            if (mimeType.startsWith("audio")) {
                type = FileItem.AUDIO;
            } else if (mimeType.startsWith("video")) {
                type = FileItem.VIDEO;
            } else if (mimeType.startsWith("image")) {
                type = FileItem.IMAGE;
            } else if (mimeType.startsWith("text")) {
                type = FileItem.TEXT;
            }
        }

        FileItem item = new FileItem(
                type,
                file.getName(),
                file.getPath(),
                null,
                mimeType,
                "-",
                file.length(),
                FileItem.EXTERNAL);

        addResultItem(item);
    }

    private static String getMimeType(File file) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());

        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }
        return type;
    }
}
