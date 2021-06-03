package com.firefly.filepicker.commom.listener;

import android.content.Context;
import android.content.Intent;
import android.provider.DocumentsContract;
import android.util.Log;

import com.firefly.filepicker.data.Constants;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import static com.firefly.filepicker.provider.DLNADocumentsProvider.AUTHORITY;

/**
 * Created by rany on 18-1-2.
 */

public class DeviceRegistryListener extends DefaultRegistryListener {
    private static final String TAG = DeviceRegistryListener.class.getSimpleName();
    public static final String DEVICE_CHANGED_BROADCAST = "com.firefly.filepicker.DLNA_DEVICE_CHANGED";

    private Context mContext;

    public DeviceRegistryListener(Context context) {
        mContext = context;
    }

    public void deviceAdded(Registry registry, final Device device) {
        Log.d(TAG, "Device added: " + device.toString());
        if (Constants.devices == null || Constants.deviceHashMap == null)
            Constants.init();
        Constants.devices.add(device);
        Constants.deviceHashMap.put(device.getIdentity().getUdn().getIdentifierString(), device);

        mContext.getContentResolver().notifyChange(DocumentsContract.buildRootsUri
                (AUTHORITY), null, false);

//        new BrowseVideos(device, mContext).run();
        sendDeviceChangedBroadcast();
    }

    public void deviceRemoved(Registry registry, Device device) {
        if (Constants.devices != null && Constants.deviceHashMap != null) {
            Log.d(TAG, "Device removed: " + device.toString());
            Constants.devices.remove(device);
            Constants.deviceHashMap.remove(device.getIdentity().getUdn().getIdentifierString());

            mContext.getContentResolver().notifyChange(DocumentsContract.buildRootsUri
                    (AUTHORITY), null, false);
            sendDeviceChangedBroadcast();
        }
    }

    public void beforeShutdown(Registry registry) {
        Constants.upnpService = null;
        Constants.devices = null;
        Constants.deviceHashMap = null;
    }

    public void afterShutdown() {

    }

    private void sendDeviceChangedBroadcast() {
        Intent intent = new Intent(DEVICE_CHANGED_BROADCAST);
        mContext.sendBroadcast(intent);
    }
}
