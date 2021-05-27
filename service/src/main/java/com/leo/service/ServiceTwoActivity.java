package com.leo.service;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ServiceTwoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_two);
    }

    public void start(View view) {
        Intent intent = new Intent(this, BookService.class);
        bindService(intent, null, BIND_AUTO_CREATE);
    }
}
