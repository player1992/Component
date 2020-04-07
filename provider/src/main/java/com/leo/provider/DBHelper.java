package com.leo.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

/**
 * <p>Date:2020-04-07.14:44</p>
 * <p>Author:niu bao</p>
 * <p>Desc:</p>
 */
public class DBHelper extends SQLiteOpenHelper {
    
    private static final String BOOK_PROVIDER_DB = "book_provider.db";
    public static final String BOOK_TABLE_NAME = "book";
    public static final String USER_TABLE_NAME = "user";

    private static final int VERSION = 1;
    
    private static final String CREATE_BOOK_TABLE = "CREATE TABLE IF NOT EXISTS "+ BOOK_TABLE_NAME+
            "(_id INTEGER PRIMARY KEY,"+"name TEXT)";
    private static final String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS "+ USER_TABLE_NAME+
            "(_id INTEGER PRIMARY KEY,"+"name TEXT," +"sex INT)";
    public DBHelper(@Nullable Context context) {
        super(context, BOOK_PROVIDER_DB, null, VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_BOOK_TABLE);
            db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
