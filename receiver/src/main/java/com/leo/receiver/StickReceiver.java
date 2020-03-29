package com.leo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * <p>Date:2020-03-29.17:59</p>
 * <p>Author:niu bao</p>
 * <p>Desc:</p>
 */
public class StickReceiver extends BroadcastReceiver {
    private static final String TAG = "StickReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG,"StickReceiver");
    }
}
