package com.firefly.filepicker.provider;

import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by rany on 18-1-5.
 */

public class ReadFileTask extends AsyncTask<Void, Void, Void> {
    private final static int CAPACITY = 1024 * 1024;

    private HttpURLConnection mHttpURLConnection;
    private ParcelFileDescriptor mPipe;
    private boolean mReady = false;

    public ReadFileTask(HttpURLConnection urlConnection, ParcelFileDescriptor pipe) {
        mHttpURLConnection = urlConnection;
        mPipe = pipe;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        InputStream in = null;
        FileOutputStream out = null;
        byte[] buffer = new byte[CAPACITY];
        int size = 0;
        mReady = true;
        Log.d("-------", "xxxxxxxxxxxxxxxx");

        try {
            if (mHttpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new FileNotFoundException("Can't open http connection.");
            }

            in = mHttpURLConnection.getInputStream();
            out = new ParcelFileDescriptor.AutoCloseOutputStream(mPipe);

            while ((size = in.read(buffer)) != -1) {
                out.write(buffer, 0, size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public boolean isReady() {
        return mReady;
    }
}
