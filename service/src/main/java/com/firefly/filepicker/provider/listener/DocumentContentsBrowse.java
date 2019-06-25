package com.firefly.filepicker.provider.listener;

import android.util.Log;

import com.firefly.filepicker.data.bean.DocumentMetadata;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.util.ArrayList;
import java.util.List;

import static com.firefly.filepicker.data.bean.DocumentMetadata.DIR;
import static com.firefly.filepicker.data.bean.DocumentMetadata.FILE;

/**
 * Created by rany on 18-1-4.
 */

public class DocumentContentsBrowse extends Browse {

    private LoadedListener mListener;
    private Service mService;

    public DocumentContentsBrowse(Service service, String containerId, LoadedListener listener) {
        super(service, containerId, BrowseFlag.DIRECT_CHILDREN);

        mListener = listener;
        mService = service;
    }

    @Override
    public void received(ActionInvocation actionInvocation, DIDLContent didl) {
        List<DocumentMetadata> files = new ArrayList<>();
        String deviceId = mService.getDevice().getIdentity().getUdn().getIdentifierString();
        DocumentMetadata doc = null;
        int type = FILE;

        for (Container container : didl.getContainers()) {
            doc = new DocumentMetadata<Container>();

            doc.setItem(container);
            doc.setType(DIR);
            doc.setDeviceId(deviceId);

            files.add(doc);
        }

//        IMAGE = 4;
//        public final static int PLAYLIST = 5;
//        public final static int TEXT = 6;
//        public final static int VIDEO
        for (Item item : didl.getItems()) {
            type = DocumentMetadata.checkType(item);
            doc = new DocumentMetadata();

            doc.setItem(item);
            doc.setType(type);
            doc.setDeviceId(deviceId);

            files.add(doc);
        }

        if (mListener != null) {
            mListener.loaded(actionInvocation, files);
        }
    }

    @Override
    public void updateStatus(Status status) {

    }

    @Override
    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
        Log.d("DocumentContentsBrowse", operation.getResponseDetails());
        if (mListener != null) {
            mListener.error(actionInvocation, defaultMsg);
        }
    }

    public interface LoadedListener {
        void loaded(ActionInvocation actionInvocation, List<DocumentMetadata> documents);
        void error(ActionInvocation invocation, String msg);
    }
}
