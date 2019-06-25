//package com.firefly.filepicker.provider.listener;
//
//import android.content.Context;
//
//import com.firefly.filepicker.data.db.VideoDbHelper;
//
//import org.fourthline.cling.model.action.ActionInvocation;
//import org.fourthline.cling.model.message.UpnpResponse;
//import org.fourthline.cling.model.meta.Service;
//import org.fourthline.cling.support.contentdirectory.callback.Search;
//import org.fourthline.cling.support.model.DIDLContent;
//import org.fourthline.cling.support.model.item.Item;
//
//import java.util.List;
//
///**
// * Created by rany on 18-1-3.
// */
//
//public class VideoContentSearch extends Search {
//    private static final String TAG = VideoContentBrowse.class.getSimpleName();
//
//    private Context mContext;
//    private Service mService;
//    private VideoDbHelper mDbHelper;
//
//    public VideoContentSearch(Context context, Service service, String containerId) {
//        super(service, containerId, "upnp:class derivedfrom \"object.item.videoItem\"");
//
//        mContext = context;
//        mService = service;
//        mDbHelper = VideoDbHelper.getInstance(context);
//        mDbHelper.clearVideosOfDevice(service.getDevice().getIdentity().getUdn().getIdentifierString());
//    }
//
//    @Override
//    public void received(ActionInvocation actionInvocation, DIDLContent didl) {
//        List<Item> items = didl.getItems();
//
//        if (items != null && items.size() > 0) {
//            String deviceId = mService.getDevice().getIdentity().getUdn().getIdentifierString();
//            mDbHelper.save(items, deviceId);
//        }
//    }
//
//    @Override
//    public void updateStatus(Status status) {
//
//    }
//
//    @Override
//    public void failure(ActionInvocation invocation,
//                        UpnpResponse operation, String defaultMsg) {
//
//    }
//}
