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

    public static final int FLAG_LOAD_WITH_CACHE = FLAG_LOAD_BASE;

    public static final int FLAG_LOAD_CANCELLABLE = FLAG_LOAD_BASE << 1;

    private static final int POOL_SIZE = 4;
    private final ExecutorService mExecutorService;

    private final List<DownLoader> mDescLoaders = new ArrayList<>();
    private final List<DownLoader> mAscLoaders = new ArrayList<>();
    private final Map<DownLoader, Future> mFutures = new HashMap<>();
    private final Map<DownLoader, Callback> mCallbacks = new HashMap<>();

    private Handler mUIHandler;
    private ICache<byte[]> mDiskCache;
    private ICache<Bitmap> mMemCache;
    private int mFlags;

    private DownLoadManager(Context context, Configuration config) {
        mUIHandler = new Handler(Looper.getMainLooper());
        ContentHelper contentHelper = new ContentHelper(context);
        mDiskCache = new DataCache(context, contentHelper);
        mMemCache = new BitmapCache(config == null || config.cacheSize <= 0 ? Runtime.getRuntime()
                .maxMemory() / 3 : config.cacheSize);
        if (config != null) {
            mFlags = config.flags;
        }
        mExecutorService = Executors.newFixedThreadPool(config == null || config.threadPoolSize
                <= 0 ? POOL_SIZE : config.threadPoolSize);
    }

    boolean isLoadWithCache(int flags) {
        return (flags & FLAG_LOAD_WITH_CACHE) == FLAG_LOAD_WITH_CACHE;
    }

    boolean isCancellable() {
        return (mFlags & FLAG_LOAD_CANCELLABLE) == FLAG_LOAD_CANCELLABLE;
    }

    public ICache<byte[]> getDiskCache() {
        return mDiskCache;
    }

    public ICache<Bitmap> getMemCache() {
        return mMemCache;
    }

    public synchronized boolean cancel(DownLoader downLoader) {
        if (!isCancellable()) {
            return false;
        }
        Callback callback = mCallbacks.get(downLoader);
        if (remove(downLoader)) {
            onNext();
        }
        notifyCancelled(callback);
        return true;
    }

    public DownLoader load(final String url, final Callback<byte[]> callback) {
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
        return downLoader;
    }

    public DownLoader load(final String url, final ImageLoader.ImageLoaderOption option, final
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
        return downLoader;
    }

    private synchronized void onNewRequest(final DownLoader downLoader, final Callback callback) {
        if (isCancellable() || (isLoadWithCache(downLoader.mFlags) && mDiskCache.get(downLoader
                .mUrl) != null)) {
            mAscLoaders.add(downLoader);
        } else {
            mDescLoaders.add(downLoader);
        }
        mCallbacks.put(downLoader, callback);
        notifyStart(callback);
        onNext();
    }

    private synchronized void onResult(final Object data, final DownLoader loader) {
        if (mFutures.get(loader) == null) {
            return;
        }
        notifyResult(mCallbacks.get(loader), data);
        remove(loader);
        onNext();
    }

    private synchronized void onError(final Throwable ex, final DownLoader loader) {
        if (mFutures.get(loader) != null && ex instanceof InterruptedIOException) {
            return;
        }
        if (mFutures.get(loader) == null) {
            return;
        }
        notifyError(mCallbacks.get(loader), ex);
        remove(loader);
        onNext();
    }

    private void notifyCancelled(final Callback callback) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onCancel(Callback.CANCEL_CODE_MANUAL);
                }
            }
        });
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

    private synchronized long getTasksSize() {
        return mDescLoaders.size() + mAscLoaders.size();
    }

    private synchronized void suspendLowPriorityTask() {
        if (mFutures.size() < POOL_SIZE || getTasksSize() <= mFutures.size()) {
            return;
        }
        if (!mDescLoaders.isEmpty()) {
            for (int index = 0; index < mDescLoaders.size(); index++) {
                DownLoader loader = mDescLoaders.get(index);
                if (mFutures.get(loader) == null) {
                    continue;
                }
                mFutures.remove(loader).cancel(true);
                return;
            }
        } else {
            for (int index = mAscLoaders.size() - 1; index >= 0; index--) {
                DownLoader loader = mAscLoaders.get(index);
                if (mFutures.get(loader) == null) {
                    continue;
                }
                mFutures.remove(loader).cancel(true);
                return;
            }
        }

    }

    private synchronized void onNext() {
        if (getTasksSize() == 0) {
            return;
        }
        suspendLowPriorityTask();
        startNext();
    }

    private synchronized void startNext() {
        for (int index = 0; index < mAscLoaders.size(); index++) {
            DownLoader loader = mAscLoaders.get(index);
            if (mFutures.get(loader) != null) {
                continue;
            }
            mFutures.put(loader, mExecutorService.submit(loader));
            return;
        }
        for (int index = mDescLoaders.size() - 1; index >= 0; index--) {
            DownLoader loader = mDescLoaders.get(index);
            if (mFutures.get(loader) != null) {
                continue;
            }
            mFutures.put(loader, mExecutorService.submit(loader));
            return;
        }
    }

    private synchronized boolean remove(DownLoader loader) {
        mAscLoaders.remove(loader);
        mDescLoaders.remove(loader);
        mCallbacks.remove(loader);
        Future f = mFutures.remove(loader);
        if (f != null) {
            f.cancel(true);
            return true;
        }
        return false;
    }
}
