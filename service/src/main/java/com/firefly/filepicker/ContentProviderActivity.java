package com.firefly.filepicker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ContentProviderActivity extends Activity {
    private static final String TAG = ContentProviderActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button button = new Button(this);
        button.setText("Click");
        setContentView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.firefly.FILE_PICKER");
                startActivityForResult(intent, 1000);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            final Uri uri = data.getData();
            final ContentResolver contentResolver = getContentResolver();
            Log.d(TAG, uri.toString());
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Cursor cursor = contentResolver.query(
                            uri,
                            null,
                            null,
                            null,
                            null);

                    if (cursor == null) {
                        Log.d(TAG, "Cursor is null");
                        return;
                    }
                    while (cursor.moveToNext()) {
                        int path_index = cursor.getColumnIndexOrThrow("path");
                        Log.d(TAG, cursor.getString(path_index));
                    }
                    Log.d(TAG, "finish");
                }
            });
        }

    }
}
