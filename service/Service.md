

## Service



### 不同开启方式的生命周期

#### 1.startService

​	`onCreate` -> `onStartCommand` -> `onStart`(已废弃)-> `onDestroy`

连续调用多次`startService`，`onCreate`只会执行一次，`onStartCommand`和`onStart`会执行多次

#### 2.bindService

​	`onCreate` -> `onBind` -> `onServiceConnected`(如果`Service`返回了`Binder`) -> `onUnbind` -> `onDestroy`

####3.startService&bindService

​	可以先`startService`再`bindService`，或者先bind再start，声明周期是一样的，`onCreate`都只会执行一次

> 如果混合开启Service，unbind之后不stopService，再次bindService，会回调rebind方法

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

`onServiceConnected`方法只有在`Service`的`onBind`没有返回`null`的时候调用

`onServiceDisconnected`方法在服务异常终止的时候回调，可以尝试下`killProcess()`;



### 死亡代理

如果服务异常终止了怎么办？客户端如何收到通知？

`IBinder.DeathRecipient`就是一个通知者，它在IBinder终止的时候会回调`binderDied`方法到客户端，前提是客户端已经提前注册了这个消息通知。

```java
private ServiceConnection mConn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        bookManager = IBookManager.Stub.asInterface(service);
        try {
            service.linkToDeath(mDeathRecipient,0);
        } catch (RemoteException e) {
            e.printStackTrace();
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
					 //com.leo.service.IBookManager$Stub$Proxy@6154ef1
					 //Binder:11688_1
            if (bookManager != null) {
                bookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            }
        }
    };
```



### onStartCommand返回值

```java
@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
```

通常返回值直接返回super,看一下返回了什么

```java
public @StartResult int onStartCommand(Intent intent, @StartArgFlags int flags, int startId) {
    onStart(intent, startId);
    return mStartCompatibility ? START_STICKY_COMPATIBILITY : START_STICKY;
}
```

根据mStartCompatibility来决定返回具体的值

```java
mStartCompatibility = getApplicationInfo().targetSdkVersion
        < Build.VERSION_CODES.ECLAIR;
```

也就是Android 2.0（5）版本为界限

我们能返回的值一共4个,测试的时候在Service中抛个异常

```java
@IntDef(flag = false, prefix = { "START_" }, value = {
        START_STICKY_COMPATIBILITY,
        START_STICKY,
        START_NOT_STICKY,
        START_REDELIVER_INTENT,
})
```

> START_STICKY_COMPATIBILITY; //异常重启的时候只会调用onCreate
>
> START_STICKY;//异常重启的时候会调用onCreate和onStartCommand，但是intent=null
>
> START_NOT_STICKY;//异常重启的时候不会调用onCreate和onStartCommand
>
> START_REDELIVER_INTENT;//异常重启的时候会调用onCreate和onStartCommand,一并回传intent