package com.firefly.dlna.httpserver;

public interface IServer {

    /**
     * 开启文件服务器
     */
    void start();

    /**
     * 销毁文件服务器
     */
    void destroy();

    /**
     * 注册文件到服务器，服务器将转换uri为http协议的uri。
     * 如果uri有明确的后缀则可以调用此方法
     * @param uri 注册文件uri
     * @return {@link Registration}数组
     */
    Registration[] registerFile(String uri);

    /**
     * 注册文件到服务器，服务器将转换uri为http协议的uri。
     * 如果uri没有明确的后缀则应该调用此方法
     * @param uri 注册文件uri
     * @param mimeType uri的MIME Type，如video/mp4
     * @return {@link Registration}数组
     */
    Registration[] registerFile(String uri, String mimeType);

    /**
     * 获取监听的端口
     * @return 返回监听的端口
     */
    int getPort();
}
