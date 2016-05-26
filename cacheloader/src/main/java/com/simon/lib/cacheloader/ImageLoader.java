package com.simon.lib.cacheloader;

import android.graphics.Bitmap;

import com.simon.lib.cacheloader.util.BitmapUtils;
import com.simon.lib.cacheloader.util.Utils;

/**
 * Created by sunmeng on 16/5/26.
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

    @Override
    public void run() {
        byte[] data = null;
        if (mDownLoadManager.isLoadCache(mFlags)) {
            data = mDownLoadManager.getCache().get(Utils.getBitmapCacheKey(mUrl, mOption.width,
                    mOption.height, mOption.cornerRate));
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
        if (bitmap != null && mDownLoadManager.isCache(mFlags)) {
            mDownLoadManager.getCache().cache(Utils.getBitmapCacheKey(mUrl, mOption.width,
                    mOption.height, mOption.cornerRate), BitmapUtils.decodeBytes(bitmap));
        }
        onLoadComplete(bitmap, ex);
    }

    void onLoadComplete(Bitmap bitmap, Throwable ex) {
    }
}
