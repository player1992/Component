## 广播

### 一、广播分类

#### 1.普通广播

intent要setPackageName

#### 2.有序广播

优先级

#### 3.粘性广播

需要申请权限

#### 4.本地广播

不涉及IPC，本地集合，进行遍历的调用

### 二、动态注册与静态注册

推荐动态注册，广播可以随着组件消失



### 广播工作流程

#### 1.注册流程

* ContextImpl#registerReceiver 调用的是`registerReceiverInternal`方法

```java
private Intent registerReceiverInternal(BroadcastReceiver receiver, int userId,
            IntentFilter filter, String broadcastPermission,
            Handler scheduler, Context context, int flags) {
        IIntentReceiver rd = null;
        if (receiver != null) {//构建一个 ReceiverDispatcher对象
            if (mPackageInfo != null && context != null) {
                if (scheduler == null) {
                    scheduler = mMainThread.getHandler();
                }
                rd = mPackageInfo.getReceiverDispatcher(
                    receiver, context, scheduler,
                    mMainThread.getInstrumentation(), true);
            } else {
                if (scheduler == null) {
                    scheduler = mMainThread.getHandler();
                }
              //特别注意这里，接收的时候要用
                rd = new LoadedApk.ReceiverDispatcher(
                        receiver, context, scheduler, null, true).getIIntentReceiver();
            }
        }
        try {
            final Intent intent = ActivityManager.getService().registerReceiver(
                    mMainThread.getApplicationThread(), mBasePackageName, rd, filter,
                    broadcastPermission, userId, flags);
            if (intent != null) {
                intent.setExtrasClassLoader(getClassLoader());
                intent.prepareToEnterProcess();
            }
            return intent;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
```

先构建一个`ReceiverDispatcher`对象，因为这里进行的是IPC操作，所以不直接操作`BroadcastReceiver`

实现类是

```java
static final class ReceiverDispatcher {
    final static class InnerReceiver extends IIntentReceiver.Stub {}
}
```

然后调用了AMS的`registerReceiver`方法，方法很长，找到下面两句

```java
synchronized (this) {
    ReceiverList rl = mRegisteredReceivers.get(receiver.asBinder());
    if (rl == null) {
        rl = new ReceiverList(this, callerApp, callingPid, callingUid,
                userId, receiver);
        if (rl.app != null) {
            final int totalReceiversForApp = rl.app.receivers.size();
            if (totalReceiversForApp >= MAX_RECEIVERS_ALLOWED_PER_APP) {
                throw new IllegalStateException("Too many receivers, total of "
                        + totalReceiversForApp + ", registered for pid: "
                        + rl.pid + ", callerPackage: " + callerPackage);
            }
            rl.app.receivers.add(rl);
        } else {
            try {
                receiver.asBinder().linkToDeath(rl, 0);
            } catch (RemoteException e) {
                return sticky;
            }
            rl.linkedToDeath = true;
        }
        mRegisteredReceivers.put(receiver.asBinder(), rl);
    } 
    ......
    BroadcastFilter bf = new BroadcastFilter(filter, rl, callerPackage,
            permission, callingUid, userId, instantApp, visibleToInstantApps);
    if (rl.containsFilter(filter)) {
			......
    } else {
        mReceiverResolver.addFilter(bf);
    }
```

最终为`IntentFilter`构早一个`BroadcastFilter`，和receiver分别保存在了AMS的集合中。

```java
final HashMap<IBinder, ReceiverList> mRegisteredReceivers = new HashMap<>();
final IntentResolver<BroadcastFilter, BroadcastFilter> mReceiverResolver
        = new IntentResolver<BroadcastFilter, BroadcastFilter>() 
```



#### 2.发送和接收

```java
@Override
public void sendBroadcast(Intent intent) {
		......
        ActivityManager.getService().broadcastIntent(
                mMainThread.getApplicationThread(), intent, resolvedType, null,
                Activity.RESULT_OK, null, null, null, AppOpsManager.OP_NONE, null, false, false,
                getUserId());
    ......
}
```

随后调用AMS的`broadcastIntentLocked`方法，方法很长，最后有一段

```java
int NR = registeredReceivers != null ? registeredReceivers.size() : 0;
if (!ordered && NR > 0) {
    if (isCallerSystem) {
        checkBroadcastFromSystem(intent, callerApp, callerPackage, callingUid,
                isProtectedBroadcast, registeredReceivers);
    }
    final BroadcastQueue queue = broadcastQueueForIntent(intent);
    BroadcastRecord r = new BroadcastRecord(queue, intent, callerApp,
            callerPackage, callingPid, callingUid, callerInstantApp, resolvedType,
            requiredPermissions, appOp, brOptions, registeredReceivers, resultTo,
            resultCode, resultData, resultExtras, ordered, sticky, false, userId);
    final boolean replaced = replacePending
            && (queue.replaceParallelBroadcastLocked(r) != null);
    if (!replaced) {
      	//添加到普通广播
        queue.enqueueParallelBroadcastLocked(r);
        queue.scheduleBroadcastsLocked();
    }
    registeredReceivers = null;
    NR = 0;
}
```

将满足条件的`IntentFilter`添加到`BroadcastQueue`中，如果有满足条件的广播并且是普通的广播就进行操作。

如果是有序广播会进行优先级的排序

```java
int NT = receivers != null ? receivers.size() : 0;
int it = 0;
ResolveInfo curt = null;
BroadcastFilter curr = null;
while (it < NT && ir < NR) {
    if (curt == null) {
        curt = (ResolveInfo)receivers.get(it);
    }
    if (curr == null) {
        curr = registeredReceivers.get(ir);
    }
    if (curr.getPriority() >= curt.priority) {
        // Insert this broadcast record into the final list.
        receivers.add(it, curr);
        ir++;
        curr = null;
        it++;
        NT++;
    } else {
        // Skip to the next ResolveInfo in the final list.
        it++;
        curt = null;
    }
}
//然后添加到的是有序广播的队列
queue.enqueueOrderedBroadcastLocked(r);
queue.scheduleBroadcastsLocked();
```

然后接着看`BroadcastQueue`的`scheduleBroadcastsLocked`方法

```java
public void scheduleBroadcastsLocked() {
    if (mBroadcastsScheduled) {
        return;
    }
    mHandler.sendMessage(mHandler.obtainMessage(BROADCAST_INTENT_MSG, this));
    mBroadcastsScheduled = true;
}
```

利用`Handler`发了个消息

```java
case BROADCAST_INTENT_MSG: {
    if (DEBUG_BROADCAST) Slog.v(
            TAG_BROADCAST, "Received BROADCAST_INTENT_MSG");
    processNextBroadcast(true);
} break;
```

继续跟进`processNextBroadcastLocked`方法

```java
while (mParallelBroadcasts.size() > 0) {
    r = mParallelBroadcasts.remove(0);
    r.dispatchTime = SystemClock.uptimeMillis();
    r.dispatchClockTime = System.currentTimeMillis();
    final int N = r.receivers.size();
    for (int i=0; i<N; i++) {
        Object target = r.receivers.get(i);
        deliverToRegisteredReceiverLocked(r, (BroadcastFilter)target, false, i);
    }
    addBroadcastToHistoryLocked(r);
}
```

遍历普通广播的集合，调用`deliverToRegisteredReceiverLocked`方法，内部又调用了`performReceiveLocke`方法

```java
void performReceiveLocked(ProcessRecord app, IIntentReceiver receiver,
        Intent intent, int resultCode, String data, Bundle extras,
        boolean ordered, boolean sticky, int sendingUser) throws RemoteException {
    if (app != null) {
        if (app.thread != null) {
            try {
                app.thread.scheduleRegisteredReceiver(receiver, intent, resultCode,
                        data, extras, ordered, sticky, sendingUser, app.repProcState);
            } catch (RemoteException ex) {
                throw ex;
            }
        } else {
            throw new RemoteException("app.thread must not be null");
        }
    } else {
        receiver.performReceive(intent, resultCode, data, extras, ordered,
                sticky, sendingUser);
    }
```

如果是IPC的话就调用`ApplicationThread`的`scheduleRegisteredReceiver`方法

```java
public void scheduleRegisteredReceiver(IIntentReceiver receiver, Intent intent,
        int resultCode, String dataStr, Bundle extras, boolean ordered,
        boolean sticky, int sendingUser, int processState) throws RemoteException {
    receiver.performReceive(intent, resultCode, dataStr, extras, ordered,
            sticky, sendingUser);
}
```

直接执行`IIntentReceiver`的`performReceive`方法，在注册广播的时候已经知道这是一个Binder类，实现对象是`ReceiverDispatcher.InnerReceiver extends IIntentReceiver.Stub`

```java
@Override
public void performReceive(Intent intent, int resultCode, String data,
        Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
    final LoadedApk.ReceiverDispatcher rd;
    if (intent == null) {
        rd = null;
    } else {
        rd = mDispatcher.get();
    }
    if (rd != null) {
        rd.performReceive(intent, resultCode, data, extras,
                ordered, sticky, sendingUser);
    } else {
       ......
    }
}
```

```java
public void performReceive(Intent intent, int resultCode, String data,
        Bundle extras, boolean ordered, boolean sticky, int sendingUser) {
    final Args args = new Args(intent, resultCode, data, extras, ordered,
            sticky, sendingUser);
    if (intent == null || !mActivityThread.post(args.getRunnable())) {
        if (mRegistered && ordered) {
            IActivityManager mgr = ActivityManager.getService();
            args.sendFinished(mgr);
        }
    }
}
```

将Intent封装在`Args`类中，而`mActivityThread`也是一个Handler类,并不是`ActivityThread`

```java
public final Runnable getRunnable() {
    return () -> {
        final BroadcastReceiver receiver = mReceiver;
        final boolean ordered = mOrdered;
        final IActivityManager mgr = ActivityManager.getService();
        final Intent intent = mCurIntent;
        mCurIntent = null;
        mDispatched = true;
        mPreviousRunStacktrace = new Throwable("Previous stacktrace");
        if (receiver == null || intent == null || mForgotten) {
            if (mRegistered && ordered) {
                sendFinished(mgr);
            }
            return;
        }
        try {
            ClassLoader cl = mReceiver.getClass().getClassLoader();
            intent.setExtrasClassLoader(cl);
            intent.prepareToEnterProcess();
            setExtrasClassLoader(cl);
            receiver.setPendingResult(this);
            receiver.onReceive(mContext, intent);//最终调用了onReceive方法
        } catch (Exception e) {
						。。。。。。
        }
    };
}
```

到此发送接收流程就结束了。

### 三、注意

AMS内部的`broadcastIntentLocked`方法默认不会发给已经关闭的应用

```java
intent.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
```

如果要调起已经关闭的应用要添加`FLAG_INCLUDE_STOPPED_PACKAGES`,二者共存的时候以`FLAG_INCLUDE_STOPPED_PACKAGES`为准