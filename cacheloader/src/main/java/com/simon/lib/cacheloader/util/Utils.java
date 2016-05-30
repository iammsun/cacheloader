package com.simon.lib.cacheloader.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;

/**
 * @author mengsun
 * @date 2015-11-17 17:00:04
 */
public class Utils {

    public static String getCacheDir(Context context) {
        return context.getExternalCacheDir().getPath() + File.separator + "cacheloader"
                + File.separator;
    }

    public static String getBitmapCacheKey(String url, float width, float height, float
            cornerRate) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return new StringBuffer(url).append("_").append(width).append("_").append(height)
                .append("_").append(cornerRate).toString();
    }

    public static DisplayMetrics getScreen(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getMetrics(dm);
        return dm;
    }
}
