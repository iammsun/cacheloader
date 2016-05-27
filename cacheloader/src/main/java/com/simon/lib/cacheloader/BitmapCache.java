package com.simon.lib.cacheloader;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.LruCache;

import com.simon.lib.cacheloader.util.IOUtils;

/**
 * @author mengsun
 * @date 2016-5-27 23:19:26
 */
class BitmapCache implements ICache<Bitmap> {

    private final LruCache<String, Bitmap> mMemoryCache;

    BitmapCache(long memorySize) {
        mMemoryCache = new LruCache<String, Bitmap>((int) (memorySize / IOUtils.UNIT)) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                if (value == null || value.isRecycled()) {
                    return 0;
                }
                return value.getByteCount() / IOUtils.UNIT;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap
                    newValue) {
                if (oldValue != null) {
                    oldValue.recycle();
                }
            }
        };
    }

    @Override
    public synchronized boolean cache(String key, Bitmap data) {
        if (TextUtils.isEmpty(key) || data == null || data.isRecycled()) {
            return false;
        }
        mMemoryCache.put(key, data);
        return true;
    }

    @Override
    public synchronized boolean remove(String key) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }
        Bitmap bitmap = mMemoryCache.remove(key);
        if (bitmap == null) {
            return false;
        }
        bitmap.recycle();
        return true;
    }

    @Override
    public synchronized Bitmap get(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        Bitmap bitmap = mMemoryCache.get(key);
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        return bitmap;
    }

    @Override
    public synchronized void clear() {
        mMemoryCache.evictAll();
    }

    @Override
    public synchronized long size() {
        return mMemoryCache.hitCount();
    }
}
