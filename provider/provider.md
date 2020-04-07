### 基本操作

#### 声明Provider

声明自己的类，继承自ContentProvider，覆写相关方法。

```java
public class BookProvider extends ContentProvider {
    private static final String TAG = "BookProvider";
    @Override
    public boolean onCreate() {
        //主线程
        Log.e(TAG,"onCreate : thread:"+ Thread.currentThread().getName());
        return false;
    }
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //Binder线程池
        Log.e(TAG,"query : thread:"+ Thread.currentThread().getName());
        return null;
    }
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Log.e(TAG,"insert : thread:"+ Thread.currentThread().getName());
        return null;
    }
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.e(TAG,"delete : thread:"+ Thread.currentThread().getName());
        return 0;
    }
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        Log.e(TAG,"update : thread:"+ Thread.currentThread().getName());
        return 0;
    }
}
```

#### 清单文件注册

```xml
<provider
    android:name="com.leo.provider.BookProvider"
    android:permission="com.leo.provider.bookProvider"//外部权限声明
    android:process=":provider"
    android:authorities="com.leo.provider.bp" //唯一authorities/>
```

#### 使用

```java
Uri uri = Uri.parse("content://com.leo.provider.bp");//content://后面跟唯一authorities
getContentResolver().query(uri, null, null, null, null);
```

#### 注意

`onCreate`方法运行在主线程，CRUD方法运行在`Binder`线程池.



### ContentResolver/ContentProvider/ContentObserver

*  ContentResolver提供统一管理ContentProvider的方法，假如ContentProvider很多的话，访问方式是一致的
* ContentProvider 内容访问者
* ContentObserver观察数据变化进行通知



### 流程分析



* `getContentResolver()`

  返回的是ApplicationContentResolver，最终还是调用以下方法

```java
@Override
protected IContentProvider acquireProvider(Context context, String auth) {
    return mMainThread.acquireProvider(context,
            ContentProvider.getAuthorityWithoutUserId(auth),
            resolveUserIdFromAuthority(auth), true);
}
```

随后调到ActivityThread中

```java
public final IContentProvider acquireProvider(
        Context c, String auth, int userId, boolean stable) {
    final IContentProvider provider = acquireExistingProvider(c, auth, userId, stable);
    if (provider != null) {
        return provider;
    }
    ContentProviderHolder holder = null;
    try {
        synchronized (getGetProviderLock(auth, userId)) {
            holder = ActivityManager.getService().getContentProvider(
                    getApplicationThread(), auth, userId, stable);
        }
    } catch (RemoteException ex) {
        throw ex.rethrowFromSystemServer();
    }
    if (holder == null) {
        Slog.e(TAG, "Failed to find provider info for " + auth);
        return null;
    }
    holder = installProvider(c, holder, holder.info,
            true /*noisy*/, holder.noReleaseNeeded, stable);
    return holder.provider;
}
```

如果找到了就返回，如果还没有启动就通知AMS启动Provider，再调用installProvider修改引用计数



* ActivityThread的main方法中，有下面代码

```java
ActivityThread thread = new ActivityThread();
thread.attach(false, startSeq);
```

然后会通知到AMS

```java
final IActivityManager mgr = ActivityManager.getService();
try {
    mgr.attachApplication(mAppThread, startSeq);
} catch (RemoteException ex) {
    throw ex.rethrowFromSystemServer();
}
```

在AMS中会开启新的进程，并回调Application的bindApplication方法，只做了一件事

```java
sendMessage(H.BIND_APPLICATION, data);
```

H类接收到消息，调用handleBindApplication方法，方法较长，有一段代码如下

```java
Application app;
try {
    //创建Application
    app = data.info.makeApplication(data.restrictedBackupMode, null);
    app.setAutofillCompatibilityEnabled(data.autofillCompatibilityEnabled);
    mInitialApplication = app;
    if (!data.restrictedBackupMode) {
        if (!ArrayUtils.isEmpty(data.providers)) {
            //installProvider，并调用onCreate方法
            installContentProviders(app, data.providers);
            mH.sendEmptyMessageDelayed(H.ENABLE_JIT, 10*1000);
        }
    }
    try {
        mInstrumentation.onCreate(data.instrumentationArgs);
    }
    try {
        //Application的onCreate方法
        mInstrumentation.callApplicationOnCreate(app);
    } catch (Exception e) {
			......
    }
}
```



继续看下

```java
private void installContentProviders(
        Context context, List<ProviderInfo> providers) {
    final ArrayList<ContentProviderHolder> results = new ArrayList<>();

    for (ProviderInfo cpi : providers) {
        if (DEBUG_PROVIDER) {
            StringBuilder buf = new StringBuilder(128);
            buf.append("Pub ");
            buf.append(cpi.authority);
            buf.append(": ");
            buf.append(cpi.name);
            Log.i(TAG, buf.toString());
        }
        ContentProviderHolder cph = installProvider(context, null, cpi,
                false /*noisy*/, true /*noReleaseNeeded*/, true /*stable*/);
				......
    }
}
```

```java
private ContentProviderHolder installProvider(Context context,
        ContentProviderHolder holder, ProviderInfo info,
        boolean noisy, boolean noReleaseNeeded, boolean stable) {
    ContentProvider localProvider = null;
    IContentProvider provider;
    if (holder == null || holder.provider == null) {
        if (DEBUG_PROVIDER || noisy) {
            Slog.d(TAG, "Loading provider " + info.authority + ": "
                    + info.name);
        }
        Context c = null;
        ApplicationInfo ai = info.applicationInfo;
        if (context.getPackageName().equals(ai.packageName)) {
            c = context;
        } else if (mInitialApplication != null &&
                mInitialApplication.getPackageName().equals(ai.packageName)) {
            c = mInitialApplication;
        } else {
            try {
                c = context.createPackageContext(ai.packageName,
                        Context.CONTEXT_INCLUDE_CODE);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
     
        try {
            final java.lang.ClassLoader cl = c.getClassLoader();
            LoadedApk packageInfo = peekPackageInfo(ai.packageName, true);
            if (packageInfo == null) {
                // System startup case.
                packageInfo = getSystemContext().mPackageInfo;
            }
            localProvider = packageInfo.getAppFactory()
                    .instantiateProvider(cl, info.name);
            provider = localProvider.getIContentProvider();
          
            //这里会调用provider
            localProvider.attachInfo(c, info);
        } catch (java.lang.Exception e) {
           ......
            return null;
        }
    } else {
        provider = holder.provider;
        if (DEBUG_PROVIDER) Slog.v(TAG, "Installing external provider " + info.authority + ": "
                + info.name);
    }
```

```java
private void attachInfo(Context context, ProviderInfo info, boolean testing) {
    mNoPerms = testing;
    if (mContext == null) {
        mContext = context;
        if (context != null) {
            mTransport.mAppOpsManager = (AppOpsManager) context.getSystemService(
                    Context.APP_OPS_SERVICE);
        }
        mMyUid = Process.myUid();
        if (info != null) {
            setReadPermission(info.readPermission);
            setWritePermission(info.writePermission);
            setPathPermissions(info.pathPermissions);
            mExported = info.exported;
            mSingleUser = (info.flags & ProviderInfo.FLAG_SINGLE_USER) != 0;
            setAuthorities(info.authority);
        }
        ContentProvider.this.onCreate();
    }
}
```