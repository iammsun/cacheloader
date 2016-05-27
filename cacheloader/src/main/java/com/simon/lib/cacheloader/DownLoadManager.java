package com.simon.lib.cacheloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
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
    private final Map<DownLoader, WeakReference<Callback>> mCallbacks = new HashMap<>();

    private Handler mUIHandler;
    private ICache<byte[]> mCache;
    private int mFlags;
    private long mCacheSize;

    private DownLoadManager(Context context, Configuration config) {
        mUIHandler = new Handler(Looper.getMainLooper());
        ContentHelper contentHelper = new ContentHelper(context);
        mCache = new DataCache(context, contentHelper);
        if (config != null) {
            mFlags = config.flags;
            mCacheSize = config.cacheSize;
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

    public void load(final String url, final Callback<byte[]> callback) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
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
                mLoaders.add(downLoader);
                mCallbacks.put(downLoader, new WeakReference(callback));
                next();
            }
        });
    }


    public void load(final String url, final ImageLoader.ImageLoaderOption option, final
    Callback<Bitmap> callback) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
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
                mLoaders.add(downLoader);
                mCallbacks.put(downLoader, new WeakReference(callback));
                next();
            }
        });
    }

    private void onResult(final byte[] data, final DownLoader loader) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mFutures.get(loader) == null) {
                    return;
                }
                WeakReference<Callback> callback = mCallbacks.get(loader);
                if (callback != null && callback.get() != null) {
                    callback.get().onResult(data);
                }
                executeDone(loader);
            }
        });
    }

    private void onResult(final Bitmap bitmap, final DownLoader loader) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mFutures.get(loader) == null) {
                    return;
                }
                WeakReference<Callback> callback = mCallbacks.get(loader);
                if (callback != null && callback.get() != null) {
                    callback.get().onResult(bitmap);
                }
                executeDone(loader);
            }
        });
    }

    private void onError(final Throwable ex, final DownLoader loader) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mFutures.get(loader) == null) {
                    return;
                }
                WeakReference<Callback> callback = mCallbacks.get(loader);
                if (callback != null && callback.get() != null) {
                    callback.get().onError(ex);
                }
                executeDone(loader);
            }
        });
    }

    private void next() {
        if (mLoaders.isEmpty()) {
            return;
        }
        if (mFutures.size() >= POOL_SIZE && mLoaders.size() > mFutures.size()) {
            cancelDirty();
        }
        executeNext();
    }

    private void executeNext() {
        for (int index = mLoaders.size() - 1; index >= 0; index--) {
            DownLoader loader = mLoaders.get(index);
            if (mFutures.get(loader) != null) {
                continue;
            }
            mFutures.put(loader, mExecutorService.submit(loader));
            return;
        }
    }

    private void executeDone(DownLoader loader) {
        mLoaders.remove(loader);
        mCallbacks.remove(loader);
        Future f = mFutures.remove(loader);
        if (f != null) {
            f.cancel(true);
        }
        next();
    }

    private void cancelDirty() {
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
