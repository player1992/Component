package com.leo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

/**
 * <p>Date:2020-03-29.21:52</p>
 * <p>Author:niu bao</p>
 * <p>Desc:</p>
 */
public class FragmentMainActivity extends FragmentActivity {
    private static final String TAG = "FragmentMainActivity";
    FragmentManager manager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        manager = getSupportFragmentManager();
    }

    public void title(View view) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.mainView,new FirstFragment());
        transaction.addToBackStack("Main");
        transaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG,"onActivityResult");
    }
}
