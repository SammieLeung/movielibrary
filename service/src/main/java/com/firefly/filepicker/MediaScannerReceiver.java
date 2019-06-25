package com.firefly.filepicker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.util.Log;

import com.firefly.filepicker.data.Constants;

import static com.firefly.filepicker.utils.Utils.getDeviceId;

public class MediaScannerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MediaScannerReceiver", intent.getAction() + "  " + intent.getData());
        String action = intent.getAction();
        Uri uri = intent.getData();

        if (uri == null) {
            return;
        }

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)
                || Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            Constants.newDevices.put(getDeviceId(uri), Constants.DEVICE_NOT_SCAN);
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)
                || Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
            Constants.newDevices.remove(getDeviceId(uri));
        } else if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
            String key = getDeviceId(uri);
            if (Constants.newDevices.containsKey(key)) {
                Constants.newDevices.put(key, Constants.DEVICE_SCAN_STARTED);
            }
        } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
            String key = getDeviceId(uri);
            if (Constants.newDevices.containsKey(key)) {
                Constants.newDevices.put(key, Constants.DEVICE_SCAN_FINISHED);
            }
        }
    }

}
