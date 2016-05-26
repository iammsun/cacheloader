package com.simon.lib.cacheloader;

/**
 * @author mengsun
 * @date 2016-5-26 21:48:57
 */
public interface Callback<T> {

    int CANCEL_CODE_TIMEOUT = 1;

    int CANCEL_CODE_MANUAL = 2;

    void onResult(T data);

    void onError(Throwable e);

    void onCancel(int code);
}
