/**
 * @author mengsun
 * @date 2015-11-17 17:00:04
 */

package com.simon.lib.cacheloader.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.text.TextUtils;
import android.util.LruCache;

import java.io.File;

public class Utils {

    public static void enforceThread(Looper looper, boolean match) {
        if (match == (looper == Looper.myLooper())) {
            return;
        }
        throw new RuntimeException(Looper.myLooper().getThread().getName() + " thread!");
    }

    public static void enforceNonUIThread() {
        enforceThread(Looper.getMainLooper(), false);
    }

    public static void enforceUIThread() {
        enforceThread(Looper.getMainLooper(), true);
    }

    public static String getHttpCacheDir(Context context) {
        return context.getExternalCacheDir().getPath() + File.separator + "http_cache"
                + File.separator;
    }

    public static String getBitmapCacheKey(String url, float width, float height, float roundPx) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return new StringBuffer(url).append("_").append(width).append("_").append(height)
                .append("_").append(roundPx).toString();
    }

    public static void cacheBitmap(LruCache<String, Bitmap> memoryCache, String key,
            Bitmap bitmap) {
        if (memoryCache == null || TextUtils.isEmpty(key) || bitmap == null) {
            return;
        }
        synchronized (memoryCache) {
            Bitmap cache = memoryCache.get(key);
            if (cache != null) {
                cache.recycle();
            }
            memoryCache.put(key, bitmap);
        }
    }

    public static Bitmap loadCachedBitmap(LruCache<String, Bitmap> memoryCache, String key) {
        if (memoryCache == null || TextUtils.isEmpty(key)) {
            return null;
        }
        synchronized (memoryCache) {
            return memoryCache.get(key);
        }
    }
}
