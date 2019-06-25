//package com.firefly.filepicker.provider.listener;
//
//import android.content.Context;
//import android.util.Log;
//
//import com.firefly.filepicker.data.Constants;
//import com.firefly.filepicker.data.db.VideoDbHelper;
//
//import org.fourthline.cling.model.action.ActionInvocation;
//import org.fourthline.cling.model.message.UpnpResponse;
//import org.fourthline.cling.model.meta.Service;
//import org.fourthline.cling.support.contentdirectory.callback.Browse;
//import org.fourthline.cling.support.model.BrowseFlag;
//import org.fourthline.cling.support.model.BrowseResult;
//import org.fourthline.cling.support.model.DIDLContent;
//import org.fourthline.cling.support.model.container.Container;
//import org.fourthline.cling.support.model.item.Item;
//
//import java.util.List;
//
///**
// * Created by rany on 18-1-3.
// */
//
//public class VideoContentBrowse extends Browse {
//    private static final String TAG = VideoContentBrowse.class.getSimpleName();
//
//    private Service mService;
//    private Context mContext;
//    private VideoDbHelper mDbHelper;
//
//    public VideoContentBrowse(Context context, Service service, String containerId) {
//        super(service, containerId, BrowseFlag.DIRECT_CHILDREN);
//
//        mContext = context;
//        mService = service;
//        mDbHelper = VideoDbHelper.getInstance(context);
//    }
//
//    @Override
//    public void received(ActionInvocation actionInvocation, DIDLContent didl) {
//        List<Item> items = didl.getItems();
//        List<Container> containers = didl.getContainers();
//
//        if (items != null && items.size() > 0) {
//            String deviceId = mService.getDevice().getIdentity().getUdn().getIdentifierString();
//            mDbHelper.save(items, deviceId);
//        }
//
//        for (Container container : containers) {
//            Constants.upnpService.getControlPoint().execute(
//                    new VideoContentBrowse(mContext, mService, container.getId()));
//        }
//    }
//
//    @Override
//    public void updateStatus(Status status) {
//
//    }
//
//    @Override
//    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
//
//    }
//
//    @Override
//    public boolean receivedRaw(ActionInvocation actionInvocation, BrowseResult browseResult) {
//        Log.d(TAG, browseResult.getResult());
//        return super.receivedRaw(actionInvocation, browseResult);
//    }
//}
