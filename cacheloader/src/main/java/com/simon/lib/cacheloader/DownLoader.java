package com.simon.lib.cacheloader;

import android.text.TextUtils;

import com.simon.lib.cacheloader.util.IOUtils;

/**
 * Created by sunmeng on 16/5/24.
 */
public abstract class DownLoader implements Runnable {

    private final String mUrl;
    private final boolean mWithCache;
    private final boolean mCache;
    boolean loop;

    DownLoader(String url, boolean withCache, boolean cache) {
        mUrl = url;
        mWithCache = withCache;
        mCache = cache;
        if (TextUtils.isEmpty(mUrl)) {
            throw new IllegalArgumentException("url is null");
        }
    }

    @Override
    public void run() {
        Throwable ex = null;
        byte[] data = null;
        if (mWithCache) {
            data = Cache.loadCache(mUrl);
        }
        if (data == null) {
            try {
                data = IOUtils.readUrl(mUrl);
            } catch (Exception e) {
                ex = e;
            }
            if (data != null && mCache) {
                Cache.cache(mUrl, data);
            }
        }
        onLoadComplete(data, ex);
    }

    public String getUrl() {
        return mUrl;
    }

    abstract void onLoadComplete(byte[] data, Throwable ex);
}
