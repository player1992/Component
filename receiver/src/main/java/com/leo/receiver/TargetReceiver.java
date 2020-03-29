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
public class TargetReceiver extends BroadcastReceiver {
    private static final String TAG = "TargetReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG,"onReceive :"+Thread.currentThread().getName());
//        try {
//            //Skipped 1199 frames!  The application may be doing too much work on its main thread.
//            Thread.sleep(10 * 1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String resultData = getResultData();
        resultData = resultData == null? "TargetData":resultData;
        Log.e(TAG,resultData);
        setResultData("TargetReceiver");
    }
}
