package com.firefly.filepicker.utils;

import android.content.Context;
import android.net.Uri;

import com.firefly.filepicker.R;
import com.station.kit.util.StorageHelper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static String getDeviceId(String url) {
        Uri uri = Uri.parse(url);
        return getDeviceId(uri);
    }

    public static String getDeviceId(Uri uri) {
        List<String> segments = uri.getPathSegments();
        return segments.get(segments.size() - 1);
    }

    public static String translatePath(Context context, String text) {
        if (text.startsWith("/storage/emulated/0")) {
            text = text.replace("/storage/emulated/0", context.getString(R.string.external_storage));
        } else if (text.startsWith("/storage")) {
            String pattern = "^(/storage/([^/]*)).*";
            String regex = "^/storage/([^/]*)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(text);
            if (m.matches()) {
                String parentPath = m.group(1);
                String driverLetter = m.group(2);
                if (StorageHelper.isMountSdCard(context, parentPath)) {
                    text = text.replaceFirst(regex, String.format(context.getResources().getString(R.string.sdcard_name), driverLetter));
                } else if (StorageHelper.isMountUsb(context, parentPath)) {
                    text = text.replaceFirst(regex, String.format(context.getResources().getString(R.string.udisk_name), driverLetter));
                } else if (StorageHelper.isMountHardDisk(context, parentPath)) {
                    text = text.replaceFirst(regex, String.format(context.getResources().getString(R.string.sata_name), driverLetter));
                } else if (StorageHelper.isMountPcie(context, parentPath)) {
                    text = text.replaceFirst(regex, String.format(context.getResources().getString(R.string.pcie_name), driverLetter));
                }
            }
        }
        return text;
    }
}
