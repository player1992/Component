package com.leo.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

/**
 * <p>Date:2020-03-29.22:40</p>
 * <p>Author:niu bao</p>
 * <p>Desc:</p>
 */
public class ResultActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setTextSize(20);
        textView.setText("Hello !!!");
        setContentView(textView);
    }
}
