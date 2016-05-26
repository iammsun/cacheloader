package com.simon.lib.cacheloader;

/**
 * Created by sunmeng on 16/5/26.
 */
public interface ICache<T> {

    boolean cache(String key, T data);

    boolean remove(String key);

    T get(String key);

    void clear();

    long size();
}
