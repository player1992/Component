package com.leo.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

/**
 * <p>Date:2020-03-29.16:26</p>
 * <p>Author:niu bao</p>
 * <p>Desc:</p>
 */
public class ReceiverActivity extends Activity {

    private TargetReceiver mTargetReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        mTargetReceiver = new TargetReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.leo.receiver.TARGET");
        registerReceiver(new TargetReceiver(),filter);


        //粘性广播，不需要提前注册,先发送后注册是他的特点
        Intent intent = new Intent();
        intent.setAction("com.leo.receiver.STICK");
        intent.setPackage(getPackageName());
        sendStickyBroadcast(intent);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mTargetReceiver);
    }

    /**
     * 8.0以上做了限制，推荐使用隐式意图，因为显示意图的Manifest会有缓存在系统中，存在被唤醒的风险
     * 而动态注册，广播随着组件的生命周期一起消失
     */
    public void send(View view) {
        Intent intent = new Intent();
        intent.setAction("com.leo.receiver.TARGET");
        intent.setPackage(getPackageName());
//        intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        sendBroadcast(intent);
        sendOrderedBroadcast(intent, null);
    }

    public void sendStick(View view) {
        StickReceiver stickReceiver = new StickReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.leo.receiver.STICK");
        registerReceiver(stickReceiver,intentFilter);
        //后注册，但是可以接收到
    }
}
