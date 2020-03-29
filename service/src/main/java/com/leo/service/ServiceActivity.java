package com.leo.service;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.List;

/**
 * <p>Date:2019-09-10.11:28</p>
 * <p>Author:niu bao</p>
 * <p>Desc:</p>
 */
public class ServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
    }


    public void start(View view) {
        Intent intent = new Intent(this, BookService.class);
        startService(intent);
    }

    private boolean bindService;

    public void bind(View view) {
        //打开外部应用的Service
//        Intent intent = new Intent();
//        intent.setComponent(new ComponentName("com.leo.ipc", "com.leo.ipc.service.AidlService"));
        //打开内部Service
        Intent intent = new Intent(this, BookService.class);
        bindService = bindService(intent, mConn, BIND_AUTO_CREATE);
        System.out.println("bindService : " + bindService);

    }

    public void kill(View view) {
        Intent intent = new Intent(this, BookService.class);
        stopService(intent);
    }

    private int pid;
    public void killProcess(View view) {
        System.out.println("杀死进程 ："+pid);
        Process.killProcess(pid);
    }

    public void unbind(View view) {
        if (bindService) {
            unbindService(mConn);
            bindService = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bindService) {
            unbindService(mConn);
            bindService = false;
        }
    }

    private IBookManager bookManager;
    private ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bookManager = IBookManager.Stub.asInterface(service);
            System.out.println("------onServiceConnected-------");
            System.out.println(Thread.currentThread().getName());
            try {
                //用于实现服务推送数据
                bookManager.register(mCallback);
                //服务进程
                service.linkToDeath(mDeathRecipient,0);
                pid = bookManager.getPid();
                System.out.println("pid : " + pid);
                //本地应用进程
                System.out.println("pid : " + Process.myPid());
                if (bookManager.getBookList() == null) return;
                for (Book book : bookManager.getBookList()) {
                    System.out.println("book : " + book.getName());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                System.out.println(service.getInterfaceDescriptor());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Crash 异常终止的情况下会调用,比如手动killProcess
            System.out.println("------onServiceDisconnected-------");
            System.out.println(Thread.currentThread().getName());
        }
    };

    INotify.Stub mCallback = new INotify.Stub() {
        @Override
        public void notifyBooks(List<Book> obj) throws RemoteException {
            System.out.println("数据延时接收");
            if (obj!= null){
                for (Book book : obj) {
                    System.out.println(book.toString());
                }
            }
        }
    };

    //设置死亡代理，服务端异常终止的时候，能够通知到客户端
    IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            //死亡通知不在UI线程，在Binder线程池中
            System.out.println("-----binderDied-----:" + bookManager);
            System.out.println(Thread.currentThread().getName());
            if (bookManager != null) {
                bookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            }
        }
    };
}
