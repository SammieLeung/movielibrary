package com.firefly.filepicker.utils;

import android.net.Uri;

import java.util.List;

public class Utils {
    public static String getDeviceId(String url) {
        Uri uri = Uri.parse(url);
        return getDeviceId(uri);
    }

    public static String getDeviceId(Uri uri) {
        List<String> segments = uri.getPathSegments();
        return segments.get(segments.size() - 1);
    }
}
