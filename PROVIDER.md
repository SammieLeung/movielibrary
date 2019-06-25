ContentProvider
=======
# 1.FilePicker
## 数据格式
### device
可获取device_id(设备id)、device_name(设备名)和device_type(设备类型)

1. device_id：DLNA设备为DLNA设备ID，本地存储设备为设备路径如：/storage/emulated/0
2. device_name：本地设备为根路径文件夹名，如/storage/emulated/0的设备名为0
3. device_type：可为local、dlna和samba

### 文件
可以获取：

- mime_type：文件MIME类型
- _display_name：文件名
- last_modified：最后更改时间
- _size：文件大小
- path：文件路径或url

除了path之外，其它的可通过`DocumentsContract.Document`获取值，如

```JAVA

DocumentsContract.Document.COLUMN_MIME_TYPE // mime_type

```

## URI格式
### devices
URI: content://com.firefly.filepicker/devices[/&lt;deviceType&gt;]  
deviceType为可选值：local、dlna和samba

### 文件
URI： content://com.firefly.filepicker/&lt;deviceType&gt;/&lt;identity&gt;/&lt;filetype&gt;/&lt;isPrivate&gt;  

#### 1. deviceType
可选：local、dlna和samba

#### 2. identity
1. deviceType为local: identity为路径的Base64值
2. deviceType为dlna: identity为&lt;deviceId&gt;:&lt;directoryId&gt;的Base64值

#### 3. filetype
```JAVA
public static final int OTHER = 0; // 全部文件
public static final int AUDIO = 1; // 音频文件
public static final int IMAGE = 2; // 图片文件
public static final int TEXT = 3; // 文本文件
public static final int VIDEO = 4; // 视频文件
```

#### 4. isPrivate
1为私密，0为非私密

## 实例代码
```JAVA
... {
    Intent intent = new Intent("com.firefly.FILE_PICKER");
    startActivityForResult(intent, 1000);
    ...
}

protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        }
}
```

# 2.MovieLibrary

#### 封面海报接口

**URI**: content://com.hphtv.movielibrary/poster

返回多个图片的地址字符串：
    
    https://img3.doubanio.com/view/photo/l/public/p2504277551.jpg
    

####调用例子
    
     Uri uri = Uri.parse("content://com.hphtv.movielibrary/poster");
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int random = (int) (Math.random() * cursor.getCount());
                cursor.moveToPosition(random);
                final String images = cursor.getString(cursor.getColumnIndex("poster"));
                Log.v(TAG, "poster= " + images);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(images);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setConnectTimeout(10000);
                            connection.setDoInput(true);
                            connection.setUseCaches(false);
                            InputStream in = connection.getInputStream();
                            final Bitmap bitmap = BitmapFactory.decodeStream(in);
                            in.close();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    imageView.setImageBitmap(bitmap);
                                }
                            });
    
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
    
    
            }
     

####关于编译签名
使用第三方签名即可，不需要系统权限。
