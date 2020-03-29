package com.leo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * <p>Date:2020-03-29.16:44</p>
 * <p>Author:niu bao</p>
 * <p>Desc:</p>
 */
public class LowReceiver extends BroadcastReceiver {
    private static final String TAG = "LowReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String resultData = getResultData();
        Log.e(TAG,resultData);
        setResultData("LowReceiver");
    }
}
