package com.leo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * <p>Date:2019-09-10.11:33</p>
 * <p>Author:niu bao</p>
 * <p>Desc:</p>
 */
public class BookService extends Service {

    private List<Book> mList = new ArrayList<>();
    Timer  mStartTimer = new Timer();
    Timer  mBindTimer = new Timer();
    Task mStartTask = new Task("------start running------");
    Task mBindTask = new Task("------bind running------");
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("------onBind------");
        mBindTimer.scheduleAtFixedRate(mBindTask, 2000,2000);
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        System.out.println("------onCreate------");
        System.out.printf("onCreate thread : %s\n", Thread.currentThread().getName());
        mStartTimer.scheduleAtFixedRate( mStartTask, 2000, 2000);
        //测试异常重启情况
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                throw new NullPointerException("Crash Message");
//            }
//        }, 3000);
    }

    @Override
    public void onRebind(Intent intent) {
        System.out.println("------onRebind------");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("------onUnbind------");
        mBindTimer.cancel();
        return true;
    }

    @Override
    public void onDestroy() {
        System.out.println("------onDestroy------");
        mStartTimer.cancel();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("------onStartCommand------");
        System.out.println("------intent------"+intent);
        System.out.println("------startId------"+startId);
//        startService之后没有stopService，异常杀死进程的时候会重启Service，
//        return START_STICKY_COMPATIBILITY; //只会调用onCreate
//        return START_STICKY;//异常重启的时候会调用onCreate和onStartCommand，但是intent=null，startId也不一致
//        return START_NOT_STICKY;//异常重启的时候不会调用onCreate和onStartCommand
//        return START_REDELIVER_INTENT;//异常重启的时候会调用onCreate和onStartCommand,一并回传intent
        return Service.START_NOT_STICKY;//不要再重启服务了
    }


    private IBookManager.Stub mBinder = new IBookManager.Stub() {
        @Override
        public void add(Book p) {
            mList.add(p);
        }

        @Override
        public List<Book> getBookList() {
            return mList;
        }

        @Override
        public int getPid() {
            return Process.myPid();
        }

        @Override
        public void register(final INotify notify) throws RemoteException {
            Message message = Message.obtain();
            message.obj = notify;
            mHandler.sendMessageDelayed(message, 5000);
        }
    };
    private HandlerN mHandler = new HandlerN();

    class HandlerN extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            INotify obj = (INotify) msg.obj;
            try {
                mBinder.add(new Book("Android 开发艺术探索"));
                mBinder.add(new Book("程序员的数学"));
                mBinder.add(new Book("数据结构与算法分析"));
                mBinder.add(new Book("Http权威指南"));
                if (obj != null) {
                    //确保服务没有异常终止
                    obj.notifyBooks(mList);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

    static class Task extends TimerTask{
        private String msg;
        public Task(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            System.out.println(msg);
        }
    }

}
