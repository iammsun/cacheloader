package com.simon.lib.cacheloader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.simon.lib.cacheloader.util.IOUtils;
import com.simon.lib.cacheloader.util.Utils;

import java.io.File;

/**
 * @author mengsun
 * @date 2016-5-26 21:48:57
 */
class DataCache implements ICache<byte[]> {

    private static final String TAG = "data_cache";

    static final String TABLE_CACHE = "cache";

    static final String COLUMN_CACHE_KEY = "key";
    static final String COLUMN_CACHE_FILE = "file";
    static final String COLUMN_CACHE_FILE_CRC = "crc";

    private final ContentHelper mContentHelper;
    private final Context mContext;

    DataCache(Context context, ContentHelper contentHelper) {
        mContext = context.getApplicationContext();
        mContentHelper = contentHelper;
    }

    @Override
    public boolean cache(String key, byte[] data) {
        if (data == null) {
            Log.d(TAG, "data is null, failed to cache");
            return false;
        }
        String filePath = Utils.getCacheDir(mContext) + java.util.UUID.randomUUID();
        try {
            IOUtils.write(data, filePath);
        } catch (Exception e) {
            Log.d(TAG, "failed cache, write to file: " + filePath);
            return false;
        }
        long crc = IOUtils.getCRC(data);
        if (crc <= 0) {
            Log.d(TAG, "failed cache, get crc: " + crc);
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_CACHE_KEY, key);
        values.put(COLUMN_CACHE_FILE, filePath);
        values.put(COLUMN_CACHE_FILE_CRC, crc);

        String selection = COLUMN_CACHE_KEY + "=?";
        String[] selectionArgs = new String[]{
                key
        };

        boolean result = true;
        if (mContentHelper.getWritableDatabase().update(TABLE_CACHE, values, selection,
                selectionArgs) == 0) {
            result = mContentHelper.getWritableDatabase().insert(TABLE_CACHE, null,
                    values) != -1;
        }
        return result;
    }

    @Override
    public boolean remove(String key) {
        String selection = COLUMN_CACHE_KEY + "=?";
        String[] selectionArgs = new String[]{
                key
        };
        Cursor c = null;
        try {
            c = mContentHelper.getWritableDatabase().query(TABLE_CACHE, null,
                    selection, selectionArgs, null, null, null);
            if (c != null && c.moveToNext()) {
                String filePath = c.getString(c.getColumnIndex(COLUMN_CACHE_FILE));
                IOUtils.delete(filePath);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return mContentHelper.getWritableDatabase().delete(TABLE_CACHE, selection,
                selectionArgs) > 0;
    }

    @Override
    public byte[] get(String key) {
        String selection = COLUMN_CACHE_KEY + "=?";
        String[] selectionArgs = new String[]{
                key
        };
        String filePath = null;
        long crc = 0;
        Cursor c = null;
        try {
            c = mContentHelper.getWritableDatabase().query(TABLE_CACHE, null,
                    selection, selectionArgs, null, null, null);
            if (c != null && c.moveToNext()) {
                filePath = c.getString(c.getColumnIndex(COLUMN_CACHE_FILE));
                crc = c.getLong(c.getColumnIndex(COLUMN_CACHE_FILE_CRC));
            } else {
                return null;
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        if (TextUtils.isEmpty(filePath) || crc <= 0) {
            Log.d(TAG, String.format("params[%s, %s] error!", filePath, crc));
            return null;
        }
        byte[] data = IOUtils.readWithCRC(filePath, crc);
        if (data == null) {
            Log.d(TAG, "file not exist or crc check error!");
        }
        return data;
    }

    @Override
    public void clear() {
        mContentHelper.getWritableDatabase().delete(TABLE_CACHE, null, null);
        IOUtils.delete(Utils.getCacheDir(mContext));
    }

    @Override
    public long size() {
        File root = new File(Utils.getCacheDir(mContext));
        return IOUtils.getLength(root);
    }
}
