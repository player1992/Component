package com.leo.activity;

import android.app.Activity;
import android.os.Bundle;

/**
 * <p>Date:2020-03-28.14:14</p>
 * <p>Author:niu bao</p>
 * <p>Desc:</p>
 */
public class SingleInstanceActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);
    }
}
