package com.firefly.dlna.httpserver;

import android.support.annotation.IntDef;

public class CacheValue {
    @IntDef({
            TYPE_OTHER,
            TYPE_HTTP,
            TYPE_HTTPS,
            TYPE_FILE,
            TYPE_SMB,
            TYPE_RTSP
    })
    @interface UriType {}
    public final static int TYPE_OTHER = -1;
    public final static int TYPE_HTTP = 0;
    public final static int TYPE_HTTPS = 1;
    public final static int TYPE_FILE = 2;
    public final static int TYPE_SMB = 3;
    public final static int TYPE_RTSP = 4;

    public String mimeType;
    public String uri;
    @UriType
    public int uriType;

    public CacheValue(String uri, String mimeType) {
        this.uri = uri;
        this.mimeType = mimeType;
        this.uriType = getUriType(uri);
    }

    @UriType
    public static int getUriType(String uri) {
        if (uri.startsWith("https://")) {
            return TYPE_HTTPS;
        } else if (uri.startsWith("http://")) {
            return TYPE_HTTP;
        } else if (uri.startsWith("smb://")) {
            return TYPE_SMB;
        } else if (uri.startsWith("rtsp://")) {
            return TYPE_RTSP;
        } else if (uri.startsWith("/")) {
            return TYPE_FILE;
        }

        return TYPE_OTHER;
    }
}
