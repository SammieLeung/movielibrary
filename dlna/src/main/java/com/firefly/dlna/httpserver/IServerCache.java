package com.firefly.dlna.httpserver;

public interface IServerCache<T> {
    void set(String key, T value);
    T get(String key);
    void clear();
}
