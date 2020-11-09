package com.firefly.filepicker.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ConditionVariable;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firefly.filepicker.data.Constants;
import com.firefly.filepicker.data.FilesRepository;
import com.firefly.filepicker.data.bean.FileItem;
import com.firefly.filepicker.data.bean.Node;
import com.firefly.filepicker.data.bean.xml.SaveItem;
import com.firefly.filepicker.data.source.IScanFiles;
import com.firefly.filepicker.utils.Base64Helper;
import com.firefly.filepicker.utils.MediaDirHelper;
import com.firefly.filepicker.utils.StorageHelper;

import org.fourthline.cling.model.meta.Device;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jcifs.Config;
import jcifs.context.SingletonContext;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import static jcifs.smb.NtStatus.NT_STATUS_BAD_NETWORK_NAME;

public class PickerContentProvider extends ContentProvider {
    private static final String TAG = PickerContentProvider.class.getSimpleName();
    private static String AUTHORITY = "com.firefly.filepicker";
    private static UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final String COLUMN_PATH = "path";
    private static final String COLUMN_FILESOURCE="file_source";

    private static final String DEVICE_TYPE_DLNA = "dlna";
    private static final String DEVICE_TYPE_LOCAL = "local";
    private static final String DEVICE_TYPE_SAMBA = "samba";

    private static final String[] DEFAULT_FILE_PROJECTION = new String[]{
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_SIZE,
            COLUMN_PATH,
            COLUMN_FILESOURCE
    };

    private static final String COLUMN_DEVICE_ID = "device_id";
    private static final String COLUMN_DEVICE_NAME = "device_name";
    private static final String COLUMN_DEVICE_TYPE = "device_type";

    private static final String[] DEFAULT_DEVICE_PROJECTION = new String[] {
            COLUMN_DEVICE_ID,
            COLUMN_DEVICE_NAME,
            COLUMN_DEVICE_TYPE
    };

    private static final int MATCH_DEVICES = 1;
    private static final int MATCH_DEVICE = 2;
    private static final int MATCH_FILES = 3;
    private static final int MATCH_FILE = 4;

    static {
        mUriMatcher.addURI(AUTHORITY, "devices", MATCH_DEVICES);
        mUriMatcher.addURI(AUTHORITY, "devices/*", MATCH_DEVICES);
        mUriMatcher.addURI(AUTHORITY, "dlna/*/*/*", MATCH_FILES);
        mUriMatcher.addURI(AUTHORITY, "local/*/*/*", MATCH_FILES);
        mUriMatcher.addURI(AUTHORITY, "samba/*/*/*", MATCH_FILES);
        mUriMatcher.addURI(AUTHORITY, "files/#", MATCH_FILE);
    }

    private static final String SELECTION_ARG_DEEP_SEARCH = "deep_search";

    private FilesRepository mFilesRepository;

    public PickerContentProvider() {
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        mFilesRepository = new FilesRepository(getContext());

        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;

        switch ( mUriMatcher.match(uri)){
            case MATCH_DEVICE:
            case MATCH_DEVICES:
                cursor = getDevices(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case MATCH_FILE:
            case MATCH_FILES:
                cursor = getFiles(uri, projection, selection, selectionArgs, sortOrder);
                break;
        }

        return cursor;
    }

    private Cursor getFiles(final Uri uri, String[] projection, String selection,
                            String[] selectionArgs, String sortOrder) {
        final String[] relProjection = resolveFileProjection(projection);
        final MatrixCursor result = new MatrixCursor(relProjection);
        Node node = null;
        final ConditionVariable mWaitCondition = new ConditionVariable();
        String deviceType = uri.getPathSegments().get(0);
        // 从Uri.getPathSegments()获取的参数已经进行Url解码
        String identity = uri.getPathSegments().get(1);
        String fileType = uri.getPathSegments().get(2);

        if (deviceType == null || identity == null || fileType == null) {
            return result;
        }

        if (DEVICE_TYPE_DLNA.equals(deviceType)) {
            node = createDNLANode(identity);
        } else if (DEVICE_TYPE_LOCAL.equals(deviceType)) {
            node = createLocalNode(identity);
        } else if (DEVICE_TYPE_SAMBA.equals(deviceType)) {
            node = createSambaNode(identity);
        }

        if (node == null) {
            return result;
        }

        boolean deepSearch = "true".equals(getSelectionEqualsArg(selection, selectionArgs, SELECTION_ARG_DEEP_SEARCH));
        mFilesRepository.setDeepSearch(deepSearch);

        mFilesRepository.scanFiles(node, Integer.valueOf(fileType),
                new IScanFiles.ScanListener() {
            @Override
            public void error(int status, String msg) {
                Log.d(TAG, status + ": " + msg);
            }

            @Override
            public void foundItem(FileItem item) {

            }

            @Override
            public void result(ArrayList<FileItem> files) {
                for (FileItem fileItem : files) {
                    result.addRow(getDocumentValues(relProjection, fileItem));
                }
            }

            @Override
            public void finish() {
                mWaitCondition.open();
            }
        });


        mWaitCondition.block(30000);
        return result;
    }

    private Node createDNLANode(String identity) {
        String idRaw = Base64Helper.decode(identity);
        String[] ids = idRaw.split(":", 3);

        if (ids.length != 3) {
            return null;
        }
        Log.d(TAG, Arrays.toString(ids));
        Device device = Constants.deviceHashMap.get(ids[0]);

        return new Node(ids[1], "", Node.DLNA, device);
    }

    private Node createLocalNode(String identity) {
        return new Node(Base64Helper.decode(identity), "", Node.EXTERNAL, null);
    }

    private Node createSambaNode(String identity) {
        return new Node(Base64Helper.decode(identity), "", Node.SAMBA, null);
    }

    private Object[] getDocumentValues(String[] projection, FileItem fileItem) {
        Object[] row = new Object[projection.length];
        for (int i = 0; i < projection.length; ++i) {
            switch (projection[i]) {
                case DocumentsContract.Document.COLUMN_DOCUMENT_ID:
                    row[i] = fileItem.getPath(); // TODO
                    break;
                case DocumentsContract.Document.COLUMN_DISPLAY_NAME:
                    row[i] = fileItem.getName();
                    break;
                case COLUMN_PATH:
                    row[i] = fileItem.getPath();
                    break;
                case DocumentsContract.Document.COLUMN_MIME_TYPE:
                    row[i] = fileItem.getMimeType();
                    break;
                case DocumentsContract.Document.COLUMN_SIZE:
                    row[i] = fileItem.getSize();
                    break;
                case DocumentsContract.Document.COLUMN_LAST_MODIFIED:
                    row[i] = fileItem.getDate();
                    break;
                case DocumentsContract.Document.COLUMN_ICON:
                    row[i] = fileItem.getThumb();
                    break;
                case COLUMN_FILESOURCE:
                    row[i]=fileItem.getFileSource();
                    break;
            }
        }

        return row;
    }

    private Cursor getDevices(Uri uri, String[] projection, String selection,
                              String[] selectionArgs, String sortOrder) {
        projection = resolveDeviceProjection(projection);
        MatrixCursor result = new MatrixCursor(projection);
        String deviceType = null;

        try {
            deviceType = uri.getPathSegments().get(1);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        if (DEVICE_TYPE_DLNA.equals(deviceType) || deviceType == null) {
            getDLNADevices(uri, projection, result);
        }

        if (DEVICE_TYPE_LOCAL.equals(deviceType) || deviceType == null) {
            getLocalDevices(uri, projection, result);
        }

        if (DEVICE_TYPE_SAMBA.equals(deviceType) || deviceType == null) {
            getSambaDevices(uri, projection, result);
        }

        return result;
    }

    private void getSambaDevices(Uri uri, String[] projection, MatrixCursor result) {
        List<SaveItem> saveItems = MediaDirHelper.getSambas(getContext());
        Config.registerSmbURLHandler();
        Set<URL> urls = new HashSet<>();

        for (SaveItem saveItem : saveItems) {
            try {
                SmbFile smbFile = new SmbFile(saveItem.getDir(),SingletonContext.getInstance().withAnonymousCredentials());
                URL url = new URL("smb", smbFile.getServer(),  smbFile.getShare());

                urls.add(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (URL url : urls) {
            SmbFile smbFile = null;
            try {
                smbFile=new SmbFile(url, SingletonContext.getInstance().withAnonymousCredentials());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                smbFile.exists();
            } catch (SmbException e) {
                if (e.getNtStatus() == NT_STATUS_BAD_NETWORK_NAME) {
                    continue;
                }
            }

            result.addRow(getSmbDeviceValue(projection, smbFile));
        }
    }

    private String[] resolveDeviceProjection(String[] projection) {
        return projection == null? DEFAULT_DEVICE_PROJECTION : projection;
    }

    private String[] resolveFileProjection(String[] projection) {
        return projection == null? DEFAULT_FILE_PROJECTION : projection;
    }

    private void getDLNADevices(Uri uri, String[] projection, MatrixCursor result) {
        if (Constants.devices == null)
            Constants.init();
        for (Device device : Constants.devices) {
            result.addRow(getDLNADeviceValue(projection, device));
        }
    }

    private void getLocalDevices(Uri uri, String[] projection, MatrixCursor result) {
        String[] paths = StorageHelper.getVolumePaths(getContext());

        for (String path : paths) {
            result.addRow(getLocalDeviceValue(projection, path));
        }
    }

    private Object[] getSmbDeviceValue(String[] projection, SmbFile smbFile) {
        Object[] row = new Object[projection.length];
        for (int i = 0; i < projection.length; ++i) {
            switch (projection[i]) {
                case COLUMN_DEVICE_ID:
                    row[i] = smbFile.getCanonicalPath();
                    break;
                case COLUMN_DEVICE_NAME:
                    String name = smbFile.getName();
                    row[i] = name;
                    break;
                case COLUMN_DEVICE_TYPE:
                    row[i] = DEVICE_TYPE_SAMBA;
                    break;
            }
        }

        return row;
    }

    private Object[] getLocalDeviceValue(String[] projection, String path) {
        Object[] row = new Object[projection.length];
        String[] pathSegments = path.split("/");

        for (int i = 0; i < projection.length; ++i) {
            switch (projection[i]) {
                case COLUMN_DEVICE_ID:
                    row[i] = path;
                    break;
                case COLUMN_DEVICE_NAME:
                    row[i] = pathSegments[pathSegments.length-1];
                    break;
                case COLUMN_DEVICE_TYPE:
                    row[i] = DEVICE_TYPE_LOCAL;
                    break;
            }
        }

        return row;
    }

    private Object[] getDLNADeviceValue(String[] projection, Device device) {
        Object[] row = new Object[projection.length];
        for (int i = 0; i < projection.length; ++i) {
            switch (projection[i]) {
                case COLUMN_DEVICE_ID:
                    row[i] = device.getIdentity().getUdn().getIdentifierString();
                    break;
                case COLUMN_DEVICE_NAME:
                    row[i] = device.getDisplayString();
                    break;
                case COLUMN_DEVICE_TYPE:
                    row[i] = DEVICE_TYPE_DLNA;
                    break;
            }
        }

        return row;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not support");
    }

    private String getSelectionEqualsArg(String selection, String[] args, String key) {
        if (selection == null) {
            return null;
        }

        String[] conditions = selection.trim().toLowerCase().split("(\\s+and\\s+)|(\\s+or\\s+)");

        int i = 0;
        for (String con : conditions) {
            String[] values = con.trim().split("\\s*=\\s*");
            if (values.length == 2) {
                if ("?".equals(values[1])) {
                    if (args == null || args.length <= i) {
                        throw new IllegalArgumentException("selectionArgs is invalid.");
                    }

                    if (values[0].equals(key)) {
                        return args[i];
                    } else {
                        ++i;
                    }
                } else if (values[0].equals(key)) {
                    return values[1];
                }
            }
        }

        return null;
    }
}
