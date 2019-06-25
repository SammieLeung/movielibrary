//package com.firefly.filepicker.provider;
//
//import android.content.Context;
//import android.util.Log;
//
//import com.firefly.filepicker.data.Constants;
//import com.firefly.filepicker.provider.listener.VideoContentBrowse;
//import com.firefly.filepicker.provider.listener.VideoContentSearch;
//
//import org.fourthline.cling.model.meta.Device;
//import org.fourthline.cling.model.meta.Service;
//import org.fourthline.cling.model.types.UDAServiceType;
//
///**
// * Created by rany on 18-1-10.
// */
//
//public class BrowseVideos extends Thread {
//    private static final String TAG = BrowseVideos.class.getSimpleName();
//
//    private Device mDevice;
//    private Context mContext;
//
//    public BrowseVideos(Device device, Context context) {
//        mDevice = device;
//        mContext = context;
//    }
//
//    @Override
//    public void run() {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        Service service = mDevice.findService(new UDAServiceType("ContentDirectory"));
//        if (service != null) {
//            if (service.getAction("Search") != null) {
//                searchVideos(service);
//            } else {
//                scanVideos(service);
//            }
//        } else {
//            Log.w(TAG, "Device (" + mDevice.toString() + ") don't have service named ContentDirectory.");
//        }
//    }
//
//    private void searchVideos(Service service) {
//        Constants.upnpService.getControlPoint().execute(
//                new VideoContentSearch(mContext, service, "0"));
//    }
//
//    private void scanVideos(Service service) {
//        Constants.upnpService.getControlPoint().execute(new VideoContentBrowse(mContext, service,
//                "0"));
//    }
//}
