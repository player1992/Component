package com.leo.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

public class ResultActivity extends Activity {

    private static final String TAG = "ResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Log.e(TAG, "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.e(TAG,"onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG,"onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG,"onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG,"onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
    }

    public void setResultFinish(View view) {
        Intent intent = new Intent();
        intent.putExtra("key","OK");
        setResult(001,intent);
        finish();
    }


    public void startOrigin(View view) {
        Intent intent = new Intent(getApplicationContext(), ActActivity.class);
        startActivity(intent);
    }
    public void startMain(View view) {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.leo.component", "com.leo.component.MainActivity");
        intent.setComponent(componentName);
        startActivity(intent);
    }
}
