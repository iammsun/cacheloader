package com.simon.lib.cacheloader;

import android.graphics.Bitmap;

import com.simon.lib.cacheloader.util.BitmapUtils;
import com.simon.lib.cacheloader.util.Utils;

/**
 * @author mengsun
 * @date 2016-5-26 21:48:57
 */
public class ImageLoader extends DownLoader {

    public static class ImageLoaderOption {
        private float width;
        private float height;
        private float cornerRate;

        public ImageLoaderOption width(float width) {
            this.width = width;
            return this;
        }

        public ImageLoaderOption height(float height) {
            this.height = height;
            return this;
        }

        public ImageLoaderOption cornerRate(float cornerRate) {
            this.cornerRate = cornerRate;
            return this;
        }
    }

    private final ImageLoaderOption mOption;

    ImageLoader(String url, int flags, ImageLoaderOption option) {
        super(url, flags);
        mOption = option;
    }

    private String getCacheKey() {
        return Utils.getBitmapCacheKey(mUrl, mOption.width, mOption.height, mOption.cornerRate);
    }

    @Override
    public void run() {
        byte[] data = null;
        if (mDownLoadManager.isLoadWithCache(mFlags)) {
            Bitmap bitmap = mDownLoadManager.getMemCache().get(getCacheKey());
            if (bitmap != null) {
                onLoadComplete(bitmap, null);
                return;
            }
            data = mDownLoadManager.getDiskCache().get(getCacheKey());
        }
        if (data == null) {
            super.run();
        } else {
            onLoadComplete(BitmapUtils.decodeBitmap(data), null);
        }
    }

    @Override
    final void onLoadComplete(byte[] data, Throwable ex) {
        Bitmap bitmap = null;
        if (data != null) {
            bitmap = BitmapUtils.decodeCroppedRoundBitmap(data, mOption.width, mOption.height,
                    mOption.cornerRate);
        }
        if (bitmap != null) {
            mDownLoadManager.getDiskCache().cache(getCacheKey(), BitmapUtils.decodeBytes(bitmap));
            mDownLoadManager.getMemCache().cache(getCacheKey(), bitmap);
        }
        onLoadComplete(bitmap, ex);
    }

    void onLoadComplete(Bitmap bitmap, Throwable ex) {
    }
}
