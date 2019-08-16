package com.firefly.dlna.httpserver;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class FileServer extends NanoHTTPD implements IServer {
    private final static String TAG = FileServer.class.getSimpleName();

    private Context mContext;
    private IRegistrationFactory mRegistrationFactory;
    private ServerCache mServerCache;

    /**
     * @param context {@link Context}实例，推荐使用getApplicationContext()
     * @param port 服务器监听的端口，等于0则使用随机端口
     */
    public FileServer(Context context, int port) {
        super(port);

        mContext = context;

        mServerCache = new ServerCache();
        mRegistrationFactory = new RegistrationFactory(mContext, mServerCache, this);
    }

    @Override
    public void start() {
        try {
            super.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        mServerCache.clear();
        stop();
    }

    @Override
    public Response serve(IHTTPSession session) {
        String[] parts = session.getUri().split("/");
        CacheValue cacheValue;
        Response res;
        String key;
        Map<String, String> header = session.getHeaders();

        if (parts.length == 4) {
            key = parts[2];
            cacheValue = mServerCache.get(key);
        } else {
            return newFixedLengthResponse(Response.Status.NOT_FOUND,
                    NanoHTTPD.MIME_HTML, "File not found.");
        }


        try {
            InputStream inputStream = null;
            long size = -1;
            Log.d(TAG, session.getHeaders().toString());

            switch (cacheValue.uriType) {
                case CacheValue.TYPE_HTTP:
                case CacheValue.TYPE_HTTPS:
                    URL url = new URL(cacheValue.uri);
                    URLConnection urlConnection = url.openConnection();

                    inputStream = urlConnection.getInputStream();
                    size = urlConnection.getContentLengthLong();

                    break;
                case CacheValue.TYPE_FILE:
                    File file = new File(cacheValue.uri);
                    size = file.length();
                    inputStream = new FileInputStream(file);

                    break;
                case CacheValue.TYPE_SMB:
                    SmbFile smbFile = null;
                    if (cacheValue.uri.contains("@")) {
                        smbFile = new SmbFile(cacheValue.uri);

                    } else {
                        try {
                            smbFile = new SmbFile(cacheValue.uri,
                                    NtlmPasswordAuthentication.ANONYMOUS);
                            smbFile.connect();
                        } catch (SmbException e) {
                            smbFile = new SmbFile(cacheValue.uri,
                                    new NtlmPasswordAuthentication("?", "GUEST", ""));
                            smbFile.connect();
                        }

                    }

                    inputStream = smbFile.getInputStream();
                    size = smbFile.length();
                    break;
                case CacheValue.TYPE_RTSP:
//                    break;
                case CacheValue.TYPE_OTHER:
                    return newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED,
                            NanoHTTPD.MIME_HTML, "Not implemented yet.");
            }

//            String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());
            res = createResponse(header, cacheValue.mimeType, inputStream, size, key);

        } catch (Exception e) {
            e.printStackTrace();
            res = newFixedLengthResponse(Response.Status.INTERNAL_ERROR,
                    NanoHTTPD.MIME_HTML, e.getLocalizedMessage());
        }

        return res;
    }

    @Override
    public Registration[] registerFile(String uri) {
        return mRegistrationFactory.generate(uri);
    }

    @Override
    public Registration[] registerFile(String uri, String mimeType) {
        return mRegistrationFactory.generate(uri, mimeType);
    }

    @Override
    public int getPort() {
        return super.getListeningPort();
    }

    /**
     * 代码来源： org.nanohttpd.webserver.SimpleWebServer
     *
     * @param header 用户请求的头
     * @param mime 返回的mimeType
     * @param fis 响应文件流，返回用户的数据
     * @param fileLen 文件的长度
     * @param etag 当前文件标识，标识文件是否发生变化
     * @return {@link fi.iki.elonen.NanoHTTPD.Response}
     */
    private Response createResponse(Map<String, String> header,
                                    String mime,
                                    InputStream fis,
                                    long fileLen,
                                    String etag) throws Exception {
        Response res = null;

        // Support (simple) skipping:
        long startFrom = 0;
        long endAt = -1;
        String range = header.get("range");
        if (range != null) {
            if (range.startsWith("bytes=")) {
                range = range.substring("bytes=".length());
                int minus = range.indexOf('-');
                try {
                    if (minus > 0) {
                        startFrom = Long.parseLong(range.substring(0, minus));
                        endAt = Long.parseLong(range.substring(minus + 1));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // get if-range header. If present, it must match etag or else we
        // should ignore the range request
        String ifRange = header.get("if-range");
        boolean headerIfRangeMissingOrMatching = (ifRange == null || etag.equals(ifRange));

        String ifNoneMatch = header.get("if-none-match");
        boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && ("*".equals(ifNoneMatch) || ifNoneMatch.equals(etag));

        // Change return code and add Content-Range header when skipping is
        // requested

        if (headerIfRangeMissingOrMatching && range != null && startFrom >= 0 && startFrom < fileLen) {
            // range request that matches current etag
            // and the startFrom of the range is satisfiable
            if (headerIfNoneMatchPresentAndMatching) {
                // range request that matches current etag
                // and the startFrom of the range is satisfiable
                // would return range from file
                // respond with not-modified
                res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                res.addHeader("ETag", etag);
            } else {
                if (endAt < 0) {
                    endAt = fileLen - 1;
                }
                long newLen = endAt - startFrom + 1;
                if (newLen < 0) {
                    newLen = 0;
                }

                fis.skip(startFrom);

                res = newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, mime, fis, newLen);
                res.addHeader("Accept-Ranges", "bytes");
                res.addHeader("Content-Length", "" + newLen);
                res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                res.addHeader("ETag", etag);
            }
        } else {

            if (headerIfRangeMissingOrMatching && range != null && startFrom >= fileLen) {
                // return the size of the file
                // 4xx responses are not trumped by if-none-match
                res = newFixedLengthResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                res.addHeader("Content-Range", "bytes */" + fileLen);
                res.addHeader("ETag", etag);
            } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                // full-file-fetch request
                // would return entire file
                // respond with not-modified
                res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                res.addHeader("ETag", etag);
            } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                // range request that doesn't match current etag
                // would return entire (different) file
                // respond with not-modified

                res = newFixedLengthResponse(Response.Status.NOT_MODIFIED, mime, "");
                res.addHeader("ETag", etag);
            } else {
                // supply the file
                res = newFixedLengthResponse(Response.Status.OK, mime, fis, fileLen);
                res.addHeader("Accept-Ranges", "bytes");
                res.addHeader("Content-Length", "" + fileLen);
                res.addHeader("ETag", etag);
            }
        }

        return res;
    }
}
