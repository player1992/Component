package com.leo.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * <p>Date:2019-09-26.09:47</p>
 * <p>Author:niu bao</p>
 * <p>Desc:
 * onCreate : thread:main
 * 其他方法都在Binder线程池
 * </p>
 */
public class BookProvider extends ContentProvider {
    private static final String TAG = "BookProvider";
    private static final String AUTHORITIES = "com.leo.provider.bp";
    private static final Uri BOOK_CONTENT_URI = Uri.parse("content://" + AUTHORITIES + "/book");
    private static final Uri USER_CONTENT_URI = Uri.parse("content://" + AUTHORITIES + "/user");
    private static final int BOOK_URI_CODE = 0;
    private static final int USER_URI_CODE = 1;


    private static final UriMatcher mMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        //UriMatcher将Code和authorities以及表名进行关联
        mMatcher.addURI(AUTHORITIES, "book", BOOK_URI_CODE);
        mMatcher.addURI(AUTHORITIES, "user", USER_URI_CODE);
    }

    private SQLiteDatabase mDb;
    private Context mCtx;

    @Override
    public boolean onCreate() {
        //主线程
        Log.e(TAG, "onCreate : thread:" + Thread.currentThread().getName());
        mCtx = getContext();
        init();

        return false;
    }

    private void init() {
        mDb = new DBHelper(mCtx).getWritableDatabase();
        mDb.execSQL("delete from " + DBHelper.BOOK_TABLE_NAME);
        mDb.execSQL("delete from " + DBHelper.USER_TABLE_NAME);
        mDb.execSQL("insert into user values(1,'Bob',1);");
        mDb.execSQL("insert into user values(2,'James',1);");
        mDb.execSQL("insert into book values(1,'Android');");
        mDb.execSQL("insert into book values(2,'iOS');");
        mDb.execSQL("insert into book values(3,'Html');");
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Log.e(TAG, "query : thread:" + Thread.currentThread().getName());
        String tableName = getTableName(uri);
        if (tableName == null) return null;
        return mDb.query(tableName, projection, selection, selectionArgs, null, null, sortOrder, null);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Log.e(TAG, "insert : thread:" + Thread.currentThread().getName());
        String tableName = getTableName(uri);
        if (tableName == null) return null;
        mDb.insert(tableName, null, values);
        mCtx.getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.e(TAG, "delete : thread:" + Thread.currentThread().getName());
        String tableName = getTableName(uri);
        if (tableName == null) return 0;
        int count = mDb.delete(tableName, selection, selectionArgs);
        if (count > 0){
            mCtx.getContentResolver().notifyChange(uri,null);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.e(TAG, "update : thread:" + Thread.currentThread().getName());
        String tableName = getTableName(uri);
        if (tableName == null) return 0;
        int count = mDb.update(tableName, values, selection, selectionArgs);
        if (count > 0){
            mCtx.getContentResolver().notifyChange(uri,null);
        }
        return count;
    }


    public String getTableName(Uri uri) {
        int code = mMatcher.match(uri);
        if (code == USER_URI_CODE) {
            return DBHelper.USER_TABLE_NAME;
        } else if (code == BOOK_URI_CODE) {
            return DBHelper.BOOK_TABLE_NAME;
        }
        return null;
    }
}
