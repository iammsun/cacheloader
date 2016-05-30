package com.simon.lib.cacheloader;

import android.text.TextUtils;

import com.simon.lib.cacheloader.util.IOUtils;

/**
 * @author mengsun
 * @date 2016-5-26 21:48:57
 */
class DownLoader implements Runnable {

    protected final DownLoadManager mDownLoadManager = DownLoadManager.getInstance();
    protected final String mUrl;
    protected int mFlags;

    DownLoader(String url, int flags) {
        mUrl = url;
        mFlags = flags;
        if (TextUtils.isEmpty(mUrl)) {
            throw new IllegalArgumentException("url is null");
        }
    }

    @Override
    public void run() {
        Throwable ex = null;
        byte[] data = null;
        if (mDownLoadManager.isLoadCache(mFlags)) {
            data = mDownLoadManager.getCache().get(mUrl);
        }
        if (data == null) {
            try {
                data = IOUtils.readUrl(mUrl);
            } catch (Exception e) {
                ex = e;
            }
            if (data != null && mDownLoadManager.isCache(mFlags)) {
                mDownLoadManager.getCache().cache(mUrl, data);
            }
        }
        onLoadComplete(data, ex);
    }

    void onLoadComplete(byte[] data, Throwable ex) {
    }
}
