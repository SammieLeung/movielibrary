package com.firefly.filepicker.provider;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.provider.DocumentsProvider;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firefly.filepicker.R;
import com.firefly.filepicker.data.Constants;
import com.firefly.filepicker.data.bean.DocumentMetadata;
import com.firefly.filepicker.provider.listener.DocumentContentsBrowse;
import com.firefly.filepicker.utils.Base64Helper;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.contentdirectory.callback.Browse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.provider.DocumentsContract.Document.MIME_TYPE_DIR;

/**
 * Created by rany on 18-1-2.
 */

public class DLNADocumentsProvider extends DocumentsProvider {
    private static final String TAG = DLNADocumentsProvider.class.getSimpleName();
    public static final String AUTHORITY = "com.firefly.filepicker.documents";
    // Use these as the default columns to return information about a root if no specific
    // columns are requested in a query.
    private static final String[] DEFAULT_ROOT_PROJECTION = new String[]{
            Root.COLUMN_ROOT_ID,
//            Root.COLUMN_MIME_TYPES,
            Root.COLUMN_FLAGS,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_DOCUMENT_ID,
//            Root.COLUMN_AVAILABLE_BYTES
    };

    // Use these as the default columns to return information about a document if no specific
    // columns are requested in a query.
    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE
    };

    private static final String ROOT = "root";
    private Map<String, List<DocumentMetadata>> mCache = new HashMap<>();
    private Map<String, DocumentMetadata> mDocCache = new HashMap<>();
    private final Executor mExecutor = Executors.newCachedThreadPool();

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");

        return true;
    }

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        // Create a cursor with either the requested fields, or the default projection.  This
        // cursor is returned to the Android system picker UI and used to display all roots from
        // this provider.
        final MatrixCursor result = new MatrixCursor(resolveRootProjection(projection));

        if (Constants.devices.isEmpty()) {
            return result;
        }

        for (Device device : Constants.devices) {
            if (device.findService(new UDAServiceType("ContentDirectory")) == null) {
                continue;
            }

            // It's possible to have multiple roots (e.g. for multiple accounts in the same app) -
            // just add multiple cursor rows.
            final MatrixCursor.RowBuilder row = result.newRow();
            String deviceId = device.getIdentity().getUdn().getIdentifierString();

            row.add(Root.COLUMN_ROOT_ID, ROOT + ":" + deviceId);
            row.add(Root.COLUMN_SUMMARY, device.getDisplayString());

            // FLAG_SUPPORTS_CREATE means at least one directory under the root supports creating
            // documents.  FLAG_SUPPORTS_RECENTS means your application's most recently used
            // documents will show up in the "Recents" category.  FLAG_SUPPORTS_SEARCH allows users
            // to search all documents the application shares.
            row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_RECENTS |
                    Root.FLAG_SUPPORTS_SEARCH |
                    Root.FLAG_SUPPORTS_IS_CHILD);

            // COLUMN_TITLE is the root title (e.g. what will be displayed to identify your provider).
            row.add(Root.COLUMN_TITLE, device.getDetails().getFriendlyName());

            // This document id must be unique within this provider and consistent across time.  The
            // system picker UI may save it and refer to it later.
            row.add(Root.COLUMN_DOCUMENT_ID, deviceId);

            // The child MIME types are used to filter the roots and only present to the user roots
            // that contain the desired type somewhere in their file hierarchy.
//            row.add(Root.COLUMN_MIME_TYPES, getChildMimeTypes(mBaseDir));
//            row.add(Root.COLUMN_AVAILABLE_BYTES, device.getDetails().getModelDetails());
            row.add(Root.COLUMN_ICON, R.drawable.ic_documentsui_icon);

            Log.d(TAG, "Add root: " + device.getDisplayString() + "  : " + device.getDetails().getFriendlyName());
        }

        return result;
    }

    @Override
    public boolean isChildDocument(String parentDocumentId, String documentId) {
        return documentId.startsWith(parentDocumentId);
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));
        String deviceId = getDeviceId(documentId);
        Device device = Constants.deviceHashMap.get(deviceId);
        String type = MIME_TYPE_DIR;
        DocumentMetadata doc = mDocCache.get(documentId);
        if (doc != null) {
            type = doc.getMimeType();
        }

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, documentId);
        row.add(Document.COLUMN_DISPLAY_NAME, device.getDetails().getFriendlyName());
        row.add(Document.COLUMN_SIZE, "0");
        row.add(Document.COLUMN_MIME_TYPE, type);
//            row.add(Document.COLUMN_LAST_MODIFIED, device);
        row.add(Document.COLUMN_FLAGS, 0);

        return result;
    }

    @Override
    public Cursor queryChildDocuments(final String parentDocumentId,
                                      String[] projection,
                                      String sortOrder) throws FileNotFoundException {
        final String[] projections = resolveDocumentProjection(projection);
        final MatrixCursor result = new MatrixCursor(projections);
        Bundle bundle = new Bundle();
        List<DocumentMetadata> documents = mCache.get(parentDocumentId);
        boolean loading = false;
        Device device = Constants.deviceHashMap.get(getDeviceId(parentDocumentId));
        Service service = null;
        final Uri parentDocumentUri = DocumentsContract.buildDocumentUri(
                AUTHORITY, parentDocumentId);

        if (documents == null && device != null &&
                (service = device.findService(new UDAServiceType("ContentDirectory"))) != null) {
            Browse browse = new DocumentContentsBrowse(service, getContainerIdOrUrl(parentDocumentId),
                    new DocumentContentsBrowse.LoadedListener() {
                        @Override
                        public void loaded(ActionInvocation actionInvocation,
                                           List<DocumentMetadata> documents) {
                            mCache.put(parentDocumentId, documents);

                            for (DocumentMetadata doc : documents) {
                                mDocCache.put(toDocumentId(doc), doc);
                            }

                            getContext().getContentResolver().notifyChange(parentDocumentUri,
                                    null, false);
                        }

                        @Override
                        public void error(ActionInvocation invocation, String msg) {
                            Log.e(TAG, msg);
                        }

                    });

            Constants.upnpService.getControlPoint().execute(browse);
            loading = true;
        } else if (documents != null) {
            for (DocumentMetadata doc : documents) {
                result.addRow(getDocumentValues(projections, doc));
            }
        }

        result.setNotificationUri(getContext().getContentResolver(), parentDocumentUri);
        bundle.putBoolean(DocumentsContract.EXTRA_LOADING, loading);
        result.setExtras(bundle);

        return result;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal)
            throws FileNotFoundException {
        if (!"r".equals(mode)) {
            throw new UnsupportedOperationException("Only support mode \"r\".");
        }

        try {
            URL url = new URL(getContainerIdOrUrl(documentId));
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createReliableSocketPair();

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            ReadFileTask readFileTask = new ReadFileTask(
                    urlConnection,
                    pipe[1]);

            readFileTask.executeOnExecutor(mExecutor);

            while (!readFileTask.isReady());

            return pipe[0];
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileNotFoundException("Can't open http connection.");
        }

        return null;
    }

    private Object[] getDocumentValues(String[] projection, DocumentMetadata metadata) {
        Object[] row = new Object[projection.length];
        for (int i = 0; i < projection.length; ++i) {
            switch (projection[i]) {
                case Document.COLUMN_DOCUMENT_ID:
                    row[i] = toDocumentId(metadata);
                    break;
                case Document.COLUMN_DISPLAY_NAME:
                    row[i] = metadata.getName();
                    break;
                case Document.COLUMN_FLAGS:
                    int flag = 0;
                    flag |= Document.FLAG_VIRTUAL_DOCUMENT;
                    row[i] = flag;
                    break;
                case Document.COLUMN_MIME_TYPE:
                    row[i] = metadata.getMimeType();
                    break;
                case Document.COLUMN_SIZE:
                    row[i] = metadata.getSize();
                    break;
                case Document.COLUMN_LAST_MODIFIED:
                    row[i] = metadata.getLastModified();
                    break;
                case Document.COLUMN_ICON:
                    row[i] = null;
                    break;
            }
        }

        return row;
    }

    /**
     * @param projection the requested root column projection
     * @return either the requested root column projection, or the default projection if the
     * requested projection is null.
     */
    private static String[] resolveRootProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_ROOT_PROJECTION;
    }

    private static String[] resolveDocumentProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION;
    }

    /**
     * Make sure the return value contain the device id
     *
     * @param doc DocumentMetadata object
     * @return document id
     */
    private String toDocumentId(@NonNull DocumentMetadata doc) {
        String url = doc.getUrl();
        String suf = url != null ? url : doc.getId();

        StringBuilder builder = new StringBuilder(doc.getDeviceId());
        builder.append(':');
        builder.append(doc.getParent());
        builder.append(':');
        builder.append(Base64Helper.encode(suf));

        return builder.toString();
    }

    private String getDeviceId(String documentId) {
        String[] ids = documentId.split(":");
        return ids[0];
    }

    private String getContainerIdOrUrl(String documentId) {
        String[] ids = documentId.split(":");
        if (ids.length == 3) {
            String res = Base64Helper.decode(ids[2]);
            Log.d(TAG, "Http address: " + res);
            return res;
        }

        return "0";
    }
}
