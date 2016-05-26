package com.simon.lib.cacheloader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * @author mengsun
 * @date 2016-5-26 21:48:57
 */
class ContentHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cache.db";
    private static final int DATABASE_VERSION = 1;

    ContentHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DataCache.TABLE_CACHE + "(" + BaseColumns._ID + " INTEGER " +
                "PRIMARY KEY," + DataCache.COLUMN_CACHE_KEY + " text," + DataCache
                .COLUMN_CACHE_FILE + " text," + DataCache.COLUMN_CACHE_FILE_CRC + " long);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
