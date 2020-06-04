package com.firefly.filepicker.data.source;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.firefly.filepicker.commom.BrowseRunnable;
import com.firefly.filepicker.commom.ScanThreadPoolManager;
import com.firefly.filepicker.data.Constants;
import com.firefly.filepicker.data.bean.FileItem;
import com.firefly.filepicker.data.bean.Node;

import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.support.contentdirectory.callback.Browse;
import org.fourthline.cling.support.contentdirectory.callback.Search;
import org.fourthline.cling.support.model.BrowseFlag;
import org.fourthline.cling.support.model.DIDLContent;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.container.Container;
import org.fourthline.cling.support.model.item.Item;

import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

/**
 * Created by rany on 18-1-10.
 *
 * For this class id of Node is the container id,
 * item of Node is device object.
 */

public class DLNAScan extends AbstractScanFiles {
    private static final String TAG = DLNAScan.class.getSimpleName();
    private int mRecursiveCount = 1;
    private final Object mCountLock = new Object();

    private Set<String> mContainersScanned = new HashSet<>();
    private Queue<String> mContainerQueue = new ConcurrentLinkedQueue<>();

    private Context mContext;
    private Future mExecuteFuture;
    private ScanThreadPoolManager mThreadPoolManager;

    public DLNAScan(Context context) {
        this();
        mContext = context;
    }

    public DLNAScan() {
    }

    @Override
    public void cancel() {
        super.cancel();

        if (mExecuteFuture != null) {
            mExecuteFuture.cancel(true);
        }

        if (mThreadPoolManager != null) {
            mThreadPoolManager.cancel();
        }
    }

    @Override
    protected void scan() throws ScanException {
        Device device = (Device) mNode.getItem();

        Service service = device.findService(new UDAServiceType("ContentDirectory"));
        if (service != null) {
            if (!mDeepSearch && service.getAction("Search") != null) {
                searchFiles(device, service);
            } else {
                browseFiles(device, service);
            }
        } else {
            throw new ScanException(NOT_SUPPORT, "Device not support ContentDirectory service.");
        }
    }

    private void searchFiles(Device device, Service service) {
        String criteria = "";

        switch (mFilterType) {
            case FileItem.AUDIO:
                criteria = "upnp:class derivedfrom \"object.item.audioItem\"";
                break;
            case FileItem.IMAGE:
                criteria = "upnp:class derivedfrom \"object.item.imageItem\"";
                break;
            case FileItem.TEXT:
                criteria = "upnp:class derivedfrom \"object.item.textItem\"";
                break;
            case FileItem.VIDEO:
            default:
                criteria = "upnp:class derivedfrom \"object.item.videoItem\"";

        }

        mExecuteFuture = Constants.upnpService.getControlPoint().execute(
                new SearchFiles(service, getContainerId(), criteria));
    }

    private void browseFiles(Device device, final Service service) {
        mContainerQueue.offer(getContainerId());
        mContainersScanned.add(getContainerId());
        mRecursiveCount = 1;
        mThreadPoolManager = ScanThreadPoolManager.getsInstance();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isEnd()) {
                    try {
                        String id = mContainerQueue.poll();

                        if (isCancel) {
                            break;
                        }

                        if (id == null || id.isEmpty()) {
                            continue;
                        }

//                        Constants.upnpService.getControlPoint().execute(
//                                new DLNAScan.BrowseFiles(service, id));
                        mThreadPoolManager.execute(
                                new BrowseRunnable(new DLNAScan.BrowseFiles(service, id)));

                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "finish");
                finish();
            }
        }).start();
    }

    private boolean isEnd() {
        synchronized(mCountLock) {
            return mRecursiveCount <= 0;
        }
    }

    private void processItems(List<Item> items) {
        for (Item item : items) {
            Res res = item.getFirstResource();

            String name = item.getTitle();
            String path = res.getValue();
            String mimeType = res.getProtocolInfo().getContentFormat();
            int type = FileItem.mimeTypeToType(mimeType);
            long size = res.getSize() == null ? 0 : res.getSize();
            String thumb = null;
            String date = "-";
            int fileSource=FileItem.DLNA;

            List<DIDLObject.Property> properties = item.getProperties();
            for (DIDLObject.Property property : properties) {
                if (property instanceof DIDLObject.Property.DC.DATE) {
                    date = ((DIDLObject.Property.DC.DATE)property).getValue();
                } else if (property instanceof DIDLObject.Property.UPNP.ALBUM) {
                    thumb = ((DIDLObject.Property.UPNP.ALBUM)property).getValue();
                }
            }

            addResultItem(new FileItem(
                    type,
                    name,
                    path,
                    thumb,
                    mimeType,
                    date,
                    size,
                    fileSource));
        }
    }

    private String getContainerId() {
        String containerId;

        if (mNode.getType() == Node.DLNA_DEVICE) {
            containerId = "0";
        } else {
            containerId = mNode.getId();
        }

        return containerId;
    }

    class BrowseFiles extends Browse {
        private Service mService;

        BrowseFiles(Service service, String containerId) throws InterruptedException {
            super(service, containerId, BrowseFlag.DIRECT_CHILDREN);

            mService = service;
        }

        @Override
        public void received(ActionInvocation actionInvocation, DIDLContent didl) {
            List<Item> items = didl.getItems();
            List<Container> containers = didl.getContainers();

            if (items != null && !items.isEmpty()) {
                processItems(items);
            }

            for (Container container : containers) {
                if (mContainersScanned.contains(container.getId())) {
                    continue;
                }

                mContainerQueue.offer(container.getId());

                synchronized(mCountLock) {
                    ++mRecursiveCount;

                    mContainersScanned.add(container.getId());
                }
            }

            synchronized(mCountLock) {
                --mRecursiveCount;
            }

        }

        @Override
        public void updateStatus(Status status) {

        }

        @Override
        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
            synchronized(mCountLock) {
                --mRecursiveCount;
            }

            error(SERVER_ERROR, defaultMsg);
            Log.d(TAG, "error: " + defaultMsg);
        }
    }

    class SearchFiles extends Search {
        private Service mService;

        SearchFiles(Service service, String containerId, String searchCriteria) {
            super(service, containerId, searchCriteria);

            mService = service;
        }

        @Override
        public void received(ActionInvocation actionInvocation, DIDLContent didl) {
            List<Item> items = didl.getItems();

            if (items != null && !items.isEmpty()) {
                processItems(items);
            }

            finish();
        }

        @Override
        public void updateStatus(Status status) {

        }

        @Override
        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
//            error(SERVER_ERROR, defaultMsg);
            Log.d(TAG, "error: " + defaultMsg);

            browseFiles(mService.getDevice(), mService);
        }
    }
}
