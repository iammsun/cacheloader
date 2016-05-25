package com.simon.lib.cacheloader;

import android.content.Context;
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
 * Created by sunmeng on 16/5/24.
 */
public class DownLoadManager {

    private static DownLoadManager sInstance;

    public static final DownLoadManager init(Context context, int flag) {
        synchronized (DownLoadManager.class) {
            if (sInstance == null) {
                sInstance = new DownLoadManager(context, flag);
            }
            return sInstance;
        }
    }

    public static final DownLoadManager getInstance() {
        return sInstance;
    }

    private static final int POOL_SIZE = 5;
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(POOL_SIZE);

    private final List<DownLoader> mLoaders = new ArrayList<>();
    private final Map<DownLoader, Future> mFutures = new HashMap<>();
    private final Map<DownLoader, WeakReference<Callback>> mCallbacks = new HashMap<>();

    private static final int FLAG_LOAD_BASE = 0x0001;

    public static final int FLAG_LOAD_FROM_CACHE = FLAG_LOAD_BASE;

    public static final int FLAG_CACHE_AFTER_LOAD = FLAG_LOAD_BASE << 1;

    private Handler mUIHandler;
    private int mFlag;

    private DownLoadManager(Context context, int flag) {
        mUIHandler = new Handler(Looper.getMainLooper());
        mFlag = flag;
        Cache.init(context);
    }

    public void load(final String url, final Callback callback) {
        onNewRequest(url, callback, mFlag);
    }

    public void load(final String url, final Callback callback, final int flag) {
        onNewRequest(url, callback, flag);
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

    private void onNewRequest(final String url, final Callback callback, final int flag) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                DownLoader downLoader = new DownLoader(url, (flag & FLAG_LOAD_FROM_CACHE) != 0,
                        (flag &
                                FLAG_CACHE_AFTER_LOAD) != 0) {
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
            mFutures.put(loader, EXECUTOR_SERVICE.submit(loader));
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
