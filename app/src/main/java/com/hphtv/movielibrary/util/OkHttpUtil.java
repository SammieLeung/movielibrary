package com.hphtv.movielibrary.util;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;


public class OkHttpUtil {
    private static final String CHARSET_NAME = "UTF-8";
    public static final int TIMEOUT = 10;

    //private final static  OkHttpClient mOkHttpClient = new OkHttpClient();
    private static OkHttpClient mOkHttpClient;

    /**
     * 创建一个OkHttpClient的对象的单例
     *
     * @return
     */
    private synchronized static OkHttpClient getOkHttpClientInstance() {
        if (mOkHttpClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    //设置连接超时等属性,不设置可能会报异常
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS);

            mOkHttpClient = builder.build();
        }
        return mOkHttpClient;
    }

    /**
     * 该不会开启异步线程。
     *
     * @param request
     * @return
     * @throws IOException
     */
    public static Response execute(Request request) throws IOException {
        return getOkHttpClientInstance().newCall(request).execute();
    }

    /**
     * 开启异步线程访问网络
     *
     * @param request
     * @param responseCallback
     */
    public static void enqueue(Request request, Callback responseCallback) {
        getOkHttpClientInstance().newCall(request).enqueue(responseCallback);
    }

    public static String getStringFromServer(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = execute(request);
        if (response.isSuccessful()) {
            String responseUrl = response.body().string();
            return responseUrl;
        } else {
            return String.valueOf(response.code());
        }
    }


    public static Response getResponseFromServer(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        return execute(request);
    }

    public static Response getPostResponseFromServer (String url, RequestBody requestBody) throws IOException {
        Request request = new Request.Builder().post(requestBody).url(url).build();
        return execute(request);
    }

    public static Response getResponse(String url) throws IOException {
        Request request = new Request.Builder()
                .addHeader("accept", "*/*").addHeader("connection", "Keep-Alive").removeHeader("User-Agent").addHeader("user-agent",
                        "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
                .url(url).build();

        return execute(request);
    }

    /**
     * 这里使用了HttpClinet的API。只是为了方便
     *
     * @param params
     * @return
     */
    public static String formatParams(List<BasicNameValuePair> params) {
        return URLEncodedUtils.format(params, CHARSET_NAME);
    }

    /**
     * 为HttpGet 的 url 方便的添加多个name value 参数。
     *
     * @param url
     * @param params
     * @return
     */
    public static String attachHttpGetParams(String url, List<BasicNameValuePair> params) {
        return url + "?" + formatParams(params);
    }

    /**
     * 为HttpGet 的 url 方便的添加1个name value 参数。
     *
     * @param url
     * @param name
     * @param value
     * @return
     */
    public static String attachHttpGetParam(String url, String name, String value) {
        return url + "?" + name + "=" + value;
    }


    /**
     * 生成post请求所需要的RequestBody。
     * @param jsonObject 用法:jsonObject.put("key","value");
     * @return
     */
    public static RequestBody buildPostRequestBody(JSONObject jsonObject){
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(mediaType,jsonObject.toString());
        return requestBody;
    }
}
