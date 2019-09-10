package com.leo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Date:2019-09-10.11:33</p>
 * <p>Author:niu bao</p>
 * <p>Desc:</p>
 */
public class BookService extends Service {

    private List<Book> mList = new ArrayList<>();

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("------onBind------");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("------onCreate------");
        System.out.printf("onCreate thread : %s", Thread.currentThread().getName());
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("------onUnbind------");
        return super.onUnbind(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("------onStartCommand------");
        return super.onStartCommand(intent, flags, startId);
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
                obj.notifyBooks(mList);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }
}
