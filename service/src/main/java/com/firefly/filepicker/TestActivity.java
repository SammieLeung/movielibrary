package com.firefly.filepicker;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firefly.filepicker.data.Constants;
import com.firefly.filepicker.data.bean.FileItem;
import com.firefly.filepicker.data.bean.Node;
import com.firefly.filepicker.data.source.DLNAScan;
import com.firefly.filepicker.data.source.ExternalScan;
import com.firefly.filepicker.data.source.IScanFiles;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = TestActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG,"testActivity");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button startButton = (Button) findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this, DLNAService.class);
                startService(intent);
            }
        });

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Node node = new Node("0", "title", Node.DLNA, null);
                if (Constants.deviceHashMap == null)
                    Constants.init();
//                for (Device device : Constants.devices) {
//
//                    Service service = device.findService(new UDAServiceType("ContentDirectory"));
//                    if (service != null) {
//                        node.setItem(device);
//                        break;
//                    }
//                }
                node.setItem(Constants.deviceHashMap.get("6396869c-47eb-18a6-d079-08df0b6220c9"));
                IScanFiles svf = new DLNAScan();
                svf.setFilterType(FileItem.VIDEO);
                svf.setNode(node);
                svf.setListener(new IScanFiles.ScanListener() {
                    @Override
                    public void error(int status, String msg) {

                    }

                    @Override
                    public void foundItem(FileItem item) {
                        Log.d(TAG, item.getName());
                    }

                    @Override
                    public void result(ArrayList<FileItem> files) {
                        Log.d(TAG, files.size() + "----------");
                    }

                    @Override
                    public void finish() {

                    }
                });
                svf.begin();
            }
        });

        findViewById(R.id.test_ex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Test External Storage */
                String path = Environment.getExternalStorageDirectory().getPath();
                Node node = new Node(path, "", Node.EXTERNAL, null);
                IScanFiles svf = new ExternalScan(TestActivity.this);
                svf.setFilterType(FileItem.VIDEO);
                svf.setNode(node);
                svf.begin();

                File file = new File(path);

                for (String f : file.list()) {
                    Log.d("TestActivity", f);
                }
            }
        });

//        findViewById(R.id.text_ev).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                /* Test ExtraVolume */
//                Node node = new Node("/storage/2A0B-3FAC", "L", Node.EXTERNAL, null);
//                IScanFiles svf = new ExtraVolumeScan(TestActivity.this);
//                svf.setFilterType(FileItem.TEXT);
//                svf.setNode(node);
//                svf.setListener(new IScanFiles.ScanListener() {
//                    @Override
//                    public void error(int status, String msg) {
//
//                    }
//
//                    @Override
//                    public void foundItem(FileItem item) {
//                        Log.d(TAG, item.getName());
//                    }
//
//                    @Override
//                    public void result(ArrayList<FileItem> files) {
//
//                    }
//
//                    @Override
//                    public void finish() {
//
//                    }
//                });
//                svf.begin();
//            }
//        });

        findViewById(R.id.content_pro).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.firefly.FILE_PICKER");
                startActivityForResult(intent, 1000);
            }
        });

        findViewById(R.id.condition_var_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    ConditionVariable conditionVariable = new ConditionVariable();

                    @Override
                    public void run() {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < 100; ++i) {
                                    Log.d(TAG, "Thread2: " + i);
                                }

                                conditionVariable.open();
                            }
                        }).start();

                        conditionVariable.block();

                        Log.d(TAG, "Thread1: " + Thread.currentThread().getName());
                    }
                }).start();
            }
        });

        findViewById(R.id.cp_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final ContentResolver contentResolver = getContentResolver();
                        Cursor cursor = contentResolver.query(
                                Uri.parse("content://com.firefly.filepicker/devices/samba"),
                                null,
                                null,
                                null,
                                null);

                        if (cursor == null) {
                            Log.d(TAG, "Cursor1 is null");
                            return;
                        }
                        while (cursor.moveToNext()) {
                            int id_index = cursor.getColumnIndexOrThrow("device_id");
                            int name_index = cursor.getColumnIndexOrThrow("device_name");
                            int type_index = cursor.getColumnIndexOrThrow("device_type");

                            Log.d(TAG, "ID: " + cursor.getString(id_index));
                            Log.d(TAG, "Name: " + cursor.getString(name_index));
                            Log.d(TAG, "Type: " + cursor.getString(type_index));

                            Log.d(TAG, "----------------------------------- \n");
                        }
                    }
                }).start();
            }
        });

        findViewById(R.id.select_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.firefly.FILE_PICKER");
                intent.putExtra("selectType", 1);
                intent.putExtra("supportNet", true);
                intent.putExtra("title", "Select file");
                startActivityForResult(intent, 1001);
            }
        });

        Intent intent = new Intent(TestActivity.this, DLNAService.class);
        startService(intent);

        /* Test StorageHelper */
//        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
//        String[] paths = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            paths = StorageHelper.getVolumePaths(storageManager);
//        } else {
//            paths = StorageHelper.getVolumePathsPreN(storageManager);
//        }
//
//        for (String path : paths) {
//            Log.d("TestActivity", path);
//        }
//
//        File file = new File("/storage/2A0B-3FAC");
//        for (File f : file.listFiles()) {
//            Log.d("TestActivity", f.getPath());
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK) {
            final Uri uri = data.getData();
            final ContentResolver contentResolver = getContentResolver();
            Log.d(TAG, uri.toString());
            new Thread(new Runnable() {
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
                        int name_index = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
                        int path_index = cursor.getColumnIndexOrThrow("path");
                        int date_index = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED);
                        int size_index = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE);
                        int type_index = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE);

                        Log.d(TAG, cursor.getString(name_index));
                        Log.d(TAG, cursor.getString(path_index));
                        Log.d(TAG, "Last modified: " + cursor.getString(date_index));
                        Log.d(TAG, "Size: " + cursor.getString(size_index));
                        Log.d(TAG, "MimeType: " + cursor.getString(type_index));

                        Log.d(TAG, "----------------------------------- \n");
                    }
                    Log.d(TAG, "finish");
                }
            }).start();
        } else if (requestCode == 1001 && resultCode == RESULT_OK) {
            final Uri uri = data.getData();
            Log.d(TAG, "Url：" + uri);
            Intent openVideo = new Intent(Intent.ACTION_VIEW);
            openVideo.setDataAndTypeAndNormalize(uri, "video/*");
            startActivity(openVideo);
        }
    }
}
