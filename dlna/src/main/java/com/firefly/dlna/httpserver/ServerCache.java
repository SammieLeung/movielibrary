package com.firefly.dlna.httpserver;

import java.util.HashMap;
import java.util.Map;

public class ServerCache implements IServerCache<CacheValue> {
    private Map<String, CacheValue> mCache = new HashMap<>();

    @Override
    public void set(String key, CacheValue value) {
        mCache.put(key, value);
    }

    @Override
    public CacheValue get(String key) {
        return mCache.get(key);
    }

    @Override
    public void clear() {
        mCache.clear();
    }
}
