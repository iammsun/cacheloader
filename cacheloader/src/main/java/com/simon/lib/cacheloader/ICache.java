package com.simon.lib.cacheloader;

/**
 * @author mengsun
 * @date 2016-5-26 21:48:57
 */
public interface ICache<T> {

    boolean cache(String key, T data);

    boolean remove(String key);

    T get(String key);

    void clear();

    long size();
}
