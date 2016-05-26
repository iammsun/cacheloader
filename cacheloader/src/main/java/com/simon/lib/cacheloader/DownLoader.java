package com.simon.lib.cacheloader;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.simon.lib.cacheloader.util.IOUtils;

/**
 * Created by sunmeng on 16/5/24.
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

    String getUrl() {
        return mUrl;
    }

    void onLoadComplete(byte[] data, Throwable ex) {

    }
}
