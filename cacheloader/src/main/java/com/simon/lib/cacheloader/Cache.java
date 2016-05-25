
package com.simon.lib.cacheloader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.simon.lib.cacheloader.util.IOUtils;
import com.simon.lib.cacheloader.util.Utils;

import java.io.File;

public class Cache {

    private static final String TAG = "CACHE";

    private static final String TABLE_CACHE = "cache";

    private static final String TABLE_ICON = "icon";

    private static final String COLUMN_CACHE_URL = "url";
    private static final String COLUMN_CACHE_FILE = "file";
    private static final String COLUMN_CACHE_FILE_CRC = "crc";

    private static final String COLUMN_ICON_URL = "url";
    private static final String COLUMN_ICON_DATA = "data";
    private static final String COLUMN_ICON_WIDTH = "width";
    private static final String COLUMN_ICON_HEIGHT = "height";

    private static final String DATABASE_NAME = "cache.db";
    private static final int DATABASE_VERSION = 1;

    private final SQLiteOpenHelper mSQLiteOpenHelper;
    private final Context mContext;

    private Cache(Context context) {
        mContext = context.getApplicationContext();
        mSQLiteOpenHelper = new SQLiteOpenHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION) {

            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("CREATE TABLE " + TABLE_CACHE + "(" + BaseColumns._ID
                        + " INTEGER PRIMARY KEY," + COLUMN_CACHE_URL + " text,"
                        + COLUMN_CACHE_FILE + " text," + COLUMN_CACHE_FILE_CRC
                        + " long);");
                db.execSQL("CREATE TABLE " + TABLE_ICON + "(" + BaseColumns._ID
                        + " INTEGER PRIMARY KEY," + COLUMN_ICON_URL + " text,"
                        + COLUMN_ICON_DATA + " BLOB," + COLUMN_ICON_WIDTH + " float,"
                        + COLUMN_ICON_HEIGHT + " float);");
            }

            @Override
            public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

            }

        };
    }

    private static Cache sInstance;

    static void init(Context context) {
        synchronized (Cache.class) {
            if (sInstance == null) {
                sInstance = new Cache(context);
            }
        }
    }

    static boolean cache(String url, byte[] data) {
        Utils.enforceNonUIThread();

        if (data == null) {
            Log.d(TAG, "data is null, failed to cahce");
            return false;
        }
        String filePath = Utils.getHttpCacheDir(sInstance.mContext)
                + java.util.UUID.randomUUID();
        if (!IOUtils.write(data, filePath)) {
            Log.d(TAG, "failed cache, write to file: " + filePath);
            return false;
        }
        long crc = IOUtils.getCRC(data);
        if (crc <= 0) {
            Log.d(TAG, "failed cache, get crc: " + crc);
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_CACHE_URL, url);
        values.put(COLUMN_CACHE_FILE, filePath);
        values.put(COLUMN_CACHE_FILE_CRC, crc);

        String selection = COLUMN_CACHE_URL + "=?";
        String[] selectionArgs = new String[]{
                url
        };

        boolean result = true;
        if (sInstance.mSQLiteOpenHelper.getWritableDatabase().update(TABLE_CACHE, values, selection,
                selectionArgs) == 0) {
            result = sInstance.mSQLiteOpenHelper.getWritableDatabase().insert(TABLE_CACHE, null,
                    values) != -1;
        }

        if (result) {
            removeIconCache(url);
        }

        return result;
    }

    static byte[] loadCache(String url) {
        Utils.enforceNonUIThread();

        String selection = COLUMN_CACHE_URL + "=?";
        String[] selectionArgs = new String[]{
                url
        };
        String filePath = null;
        long crc = 0;
        Cursor c = null;
        try {
            c = sInstance.mSQLiteOpenHelper.getWritableDatabase().query(TABLE_CACHE, null,
                    selection, selectionArgs, null, null, null);
            if (c != null && c.moveToNext()) {
                filePath = c.getString(c.getColumnIndex(COLUMN_CACHE_FILE));
                crc = c.getLong(c.getColumnIndex(COLUMN_CACHE_FILE_CRC));
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.d(TAG, "failed to load cache, remove it: " + url, e);
            removeCache(url);
            return null;
        } finally {
            if (c != null) {
                c.close();
            }
        }
        if (TextUtils.isEmpty(filePath) || crc <= 0) {
            Log.d(TAG, String.format("params[%s, %s] error!", filePath, crc));
            removeCache(url);
            return null;
        }
        byte[] data = IOUtils.readWithCRC(filePath, crc);
        if (data == null) {
            Log.d(TAG, "file not exist or crc check error!");
            removeCache(url);
        }
        return data;
    }

    private static void removeCache(String url) {
        Utils.enforceNonUIThread();

        // delete cache file
        String selection = COLUMN_CACHE_URL + "=?";
        String[] selectionArgs = new String[]{
                url
        };

        Cursor c = null;
        try {
            c = sInstance.mSQLiteOpenHelper.getWritableDatabase().query(TABLE_CACHE, null,
                    selection, selectionArgs, null, null, null);
            if (c != null && c.moveToNext()) {
                String filePath = c.getString(c.getColumnIndex(COLUMN_CACHE_FILE));
                IOUtils.delete(filePath);
            }
        } catch (Exception e) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        sInstance.mSQLiteOpenHelper.getWritableDatabase().delete(TABLE_CACHE, selection,
                selectionArgs);
    }

    static boolean cacheIcon(String url, byte[] data, float width, float height) {
        Utils.enforceNonUIThread();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ICON_DATA, data);
        values.put(COLUMN_ICON_URL, url);
        values.put(COLUMN_ICON_WIDTH, width);
        values.put(COLUMN_ICON_HEIGHT, height);

        String selection = COLUMN_ICON_URL + "=? and " + COLUMN_ICON_WIDTH + "=? and "
                + COLUMN_ICON_HEIGHT + "=?";
        String[] selectionArgs = new String[]{
                url, String.valueOf(width), String.valueOf(height)
        };
        if (sInstance.mSQLiteOpenHelper.getWritableDatabase().update(TABLE_ICON, values, selection,
                selectionArgs) == 0) {
            return sInstance.mSQLiteOpenHelper.getWritableDatabase().insert(TABLE_ICON, null,
                    values) != -1;
        } else {
            return true;
        }
    }

    private static void removeIconCache(String url) {
        Utils.enforceNonUIThread();
        String selection = COLUMN_ICON_URL + "=?";
        String[] selectionArgs = new String[]{
                url
        };
        sInstance.mSQLiteOpenHelper.getWritableDatabase().delete(TABLE_ICON, selection,
                selectionArgs);
    }

    public static void clear() {
        Utils.enforceNonUIThread();
        sInstance.mSQLiteOpenHelper.getWritableDatabase().delete(TABLE_CACHE, null, null);
        sInstance.mSQLiteOpenHelper.getWritableDatabase().delete(TABLE_ICON, null, null);
        IOUtils.delete(Utils.getHttpCacheDir(sInstance.mContext));
    }

    public static long getCacheSize() {
        File root = new File(Utils.getHttpCacheDir(sInstance.mContext));
        return IOUtils.getLength(root);
    }

    static byte[] loadIconCache(String url, float width, float height) {
        Utils.enforceNonUIThread();
        Cursor c = null;
        String selection = COLUMN_ICON_URL + "=? and " + COLUMN_ICON_WIDTH + "=? and "
                + COLUMN_ICON_HEIGHT + "=?";
        String[] selectionArgs = new String[]{
                url, String.valueOf(width), String.valueOf(height)
        };
        try {
            c = sInstance.mSQLiteOpenHelper.getWritableDatabase().query(TABLE_ICON, null, selection,
                    selectionArgs, null, null, null);
            if (c != null && c.moveToNext()) {
                return c.getBlob(c.getColumnIndex(COLUMN_ICON_DATA));
            }
        } catch (Exception e) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return null;
    }

}
