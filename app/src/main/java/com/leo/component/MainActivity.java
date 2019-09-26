package com.leo.component;

import android.Manifest;
import android.content.ContentProvider;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.leo.activity.ActActivity;
import com.leo.provider.ContentProviderActivity;
import com.leo.service.ServiceActivity;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String [] pre = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(pre,0);
        }
    }

    public void activity(View view) {
        Intent intent = new Intent(this, ActActivity.class);
        startActivity(intent);
    }

    public void service(View view) {
        Intent intent = new Intent(this, ServiceActivity.class);
        startActivity(intent);
    }


    public void contentProvider(View view) {
        Intent intent = new Intent(this, ContentProviderActivity.class);
        startActivity(intent);
    }
}
