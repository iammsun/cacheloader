package com.simon.lib.cacheloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author mengsun
 * @date 2016-5-26 21:48:57
 */
public class DownLoadManager {

    private static DownLoadManager sInstance;

    public static class Configuration {
        private int flags;
        private long cacheSize;
        private int threadPoolSize;

        public Configuration setFlags(int flags) {
            this.flags = flags;
            return this;
        }

        public Configuration setCacheSize(long cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

        public Configuration setThreadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
            return this;
        }
    }

    public static final void init(Context context, Configuration config) {
        synchronized (DownLoadManager.class) {
            if (sInstance == null) {
                sInstance = new DownLoadManager(context, config);
            }
        }
    }

    public static final DownLoadManager getInstance() {
        synchronized (DownLoadManager.class) {
            return sInstance;
        }
    }

    private static final int FLAG_LOAD_BASE = 0x0001;

    public static final int FLAG_LOAD_FROM_CACHE = FLAG_LOAD_BASE;

    public static final int FLAG_CACHE_AFTER_LOAD = FLAG_LOAD_BASE << 1;

    private static final int POOL_SIZE = 10;
    private final ExecutorService mExecutorService;

    private final List<DownLoader> mLoaders = new ArrayList<>();
    private final Map<DownLoader, Future> mFutures = new HashMap<>();
    private final Map<DownLoader, Callback> mCallbacks = new HashMap<>();

    private Handler mUIHandler;
    private ICache<byte[]> mCache;
    private ICache<Bitmap> mMemCache;
    private int mFlags;

    private DownLoadManager(Context context, Configuration config) {
        mUIHandler = new Handler(Looper.getMainLooper());
        ContentHelper contentHelper = new ContentHelper(context);
        mCache = new DataCache(context, contentHelper);
        mMemCache = new BitmapCache(config == null || config.cacheSize <= 0 ? Runtime.getRuntime()
                .maxMemory() / 3 : config.cacheSize);
        if (config != null) {
            mFlags = config.flags;
        }
        mExecutorService = Executors.newFixedThreadPool(config == null || config.threadPoolSize
                <= 0 ? POOL_SIZE : config.threadPoolSize);
    }

    boolean isLoadCache(int flags) {
        return (flags & FLAG_LOAD_FROM_CACHE) == FLAG_LOAD_FROM_CACHE;
    }

    boolean isCache(int flags) {
        return (flags & FLAG_CACHE_AFTER_LOAD) == FLAG_CACHE_AFTER_LOAD;
    }

    public ICache<byte[]> getCache() {
        return mCache;
    }

    public ICache<Bitmap> getMemCache() {
        return mMemCache;
    }

    public void load(final String url, final Callback<byte[]> callback) {
        DownLoader downLoader = new DownLoader(url, mFlags) {
            @Override
            void onLoadComplete(final byte[] data, final Throwable ex) {
                if (ex != null) {
                    onError(ex, this);
                } else {
                    onResult(data, this);
                }
            }
        };
        onNewRequest(downLoader, callback);
    }


    public void load(final String url, final ImageLoader.ImageLoaderOption option, final
    Callback<Bitmap> callback) {
        DownLoader downLoader = new ImageLoader(url, mFlags, option) {
            @Override
            void onLoadComplete(Bitmap bitmap, final Throwable ex) {
                if (ex != null) {
                    onError(ex, this);
                } else {
                    onResult(bitmap, this);
                }
            }
        };
        onNewRequest(downLoader, callback);
    }

    private synchronized void onNewRequest(final DownLoader downLoader, final Callback callback) {
        mLoaders.add(downLoader);
        mCallbacks.put(downLoader, callback);
        notifyStart(callback);
        next();
    }

    private synchronized void onResult(final Object data, final DownLoader loader) {
        if (mFutures.get(loader) == null) {
            return;
        }
        notifyResult(mCallbacks.get(loader), data);
        executeDone(loader);
    }

    private synchronized void onError(final Throwable ex, final DownLoader loader) {
        if (mFutures.get(loader) != null && ex instanceof InterruptedIOException) {
            return;
        }
        if (mFutures.get(loader) == null) {
            return;
        }
        notifyError(mCallbacks.get(loader), ex);
        executeDone(loader);
    }

    private void notifyStart(final Callback callback) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onStart();
                }
            }
        });
    }

    private void notifyResult(final Callback callback, final Object data) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onResult(data);
                }
            }
        });
    }

    private void notifyError(final Callback callback, final Throwable ex) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onError(ex);
                }
            }
        });
    }

    private synchronized void next() {
        if (mLoaders.isEmpty()) {
            return;
        }
        if (mFutures.size() >= POOL_SIZE && mLoaders.size() > mFutures.size()) {
            cancelDirty();
        }
        executeNext();
    }

    private synchronized void executeNext() {
        for (int index = mLoaders.size() - 1; index >= 0; index--) {
            DownLoader loader = mLoaders.get(index);
            if (mFutures.get(loader) != null) {
                continue;
            }
            mFutures.put(loader, mExecutorService.submit(loader));
            return;
        }
    }

    private synchronized void executeDone(DownLoader loader) {
        mLoaders.remove(loader);
        mCallbacks.remove(loader);
        Future f = mFutures.remove(loader);
        if (f != null) {
            f.cancel(true);
        }
        next();
    }

    private synchronized void cancelDirty() {
        for (int index = 0; index < mLoaders.size(); index++) {
            DownLoader loader = mLoaders.get(index);
            if (mFutures.get(loader) == null) {
                continue;
            }
            mFutures.remove(loader).cancel(true);
            return;
        }
    }
}
