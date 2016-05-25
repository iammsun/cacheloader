package com.simon.lib.cacheloader;

/**
 * Created by sunmeng on 16/5/24.
 */
public interface Callback {

    int CANCEL_CODE_TIMEOUT = 1;

    int CANCEL_CODE_MANUAL = 2;

    void onResult(byte[] data);

    void onError(Throwable e);

    void onCancel(int code);
}
