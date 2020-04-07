package com.leo.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class ContentProviderActivity extends AppCompatActivity {

    private static final String TAG = "ContentProviderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_provider);


        Uri bookUri = Uri.parse("content://com.leo.provider.bp/book");
        ContentValues values = new ContentValues();
        values.put("_id", "4");
        values.put("name", "Core Java Volume");
        getContentResolver().insert(bookUri,values);


        Cursor bookCursor = getContentResolver().query(bookUri, new String[]{"_id", "name"}, null, null, null);
        while (bookCursor.moveToNext()){
            Book book = new Book();
            book.id = bookCursor.getInt(0);
            book.name= bookCursor.getString(1);
            Log.e(TAG,book.toString());
        }
        bookCursor.close();


        Uri userUri = Uri.parse("content://com.leo.provider.bp/user");
        ContentValues uValue = new ContentValues();
        uValue.put("_id", "3");
        uValue.put("name", "Chris");
        uValue.put("sex", "0");
        getContentResolver().insert(userUri, uValue);

        Cursor userCursor = getContentResolver().query(userUri, new String[]{"_id", "name","sex"}, null, null, null);
        while (userCursor.moveToNext()){
            User user = new User();
            user.id = userCursor.getInt(0);
            user.name= userCursor.getString(1);
            user.sex= userCursor.getInt(2);
            Log.e(TAG,user.toString());
        }
        userCursor.close();


//        E/ContentProviderActivity: Book{id=1, name='Android'}
//        E/ContentProviderActivity: Book{id=2, name='iOS'}
//        E/ContentProviderActivity: Book{id=3, name='Html'}
//        E/ContentProviderActivity: Book{id=4, name='Core Java Volume'}
//        E/ContentProviderActivity: User{name='Bob', sex=1, id=1}
//        E/ContentProviderActivity: User{name='James', sex=1, id=2}
//        E/ContentProviderActivity: User{name='Chris', sex=0, id=3}


    }
}
