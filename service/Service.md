

## Service



### 不同开启方式的生命周期

#### 1.startService

​	`onCreate` -> `onStartCommand` -> `onStart`(已废弃)-> `onDestroy`

连续调用多次`startService`，`onCreate`只会执行一次，`onStartCommand`和`onStart`会执行多次

#### 2.bindService

​	`onCreate` -> `onBind` -> `onServiceConnected`(如果`Service`返回了`Binder`) -> `onUnbind` -> `onDestroy`

####3.startService&bindService

​	可以先`startService`再`bindService`，或者先bind再start，声明周期是一样的，`onCreate`都只会执行一次

#### 4.关闭Service

	> `startService` -> `stopService`
	>
	> `bindService` -> `unbindService`
	>
	> 二者混合方式开启的`Service`，必须`stopService`和`unbindService`都调用，否则不会执行`onDestroy`方法



### ServiceConnection

```java
private ServiceConnection mConn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        IBookManager bookManager = IBookManager.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        //Crash 异常终止的情况下会调用
        System.out.println("------onServiceDisconnected-------");
        System.out.println(Thread.currentThread().getName());
    }
};
```

`ServiceConnection` 的`onServiceConnected`方法只有在`Service`的`onBind`没有返回`null`的时候调用