## Activity



### 1.生命周期

#### 正常情况

* onCreate ： Activity正在被创建
* onStart ：Activity正在启动，还没有出现在前台，但是可见
* onResume ： Activity出现在前台活动
* onPause ： Activity正在停止，onPause先执行完，新Activity的onResume才会执行
* onStop ： Activity已经停止、即将停止
* onRestart ： Activity正在重新创建，从不可见变为可见，比如从别的Activity返回或按下Home的时候。
* onDestroy ： Activity即将被销毁

<img src="./pics/activity_lifecycle.png">

* 如果B是透明主题或者窗口主题就不会回调A的onStop方法
* 先回调A的onPause才回调B的onCreate

### 2.启动模式



#### Standard 标准启动模式

以标准模式启动就会产生对应的实例，不做任何操作，但是以Application开启Activity会报错（FLAG_ACTIVITY_NEW_TASK），没有对应的任务栈。

```shell
Running activities (most recent first):
      TaskRecord{458741 #137 A=com.leo.component U=0 StackId=13 sz=5}
        Run #4: ActivityRecord{aee6d91 u0 com.leo.component/com.leo.activity.ActActivity t137}
        Run #3: ActivityRecord{827e46 u0 com.leo.component/com.leo.activity.ActActivity t137}
        Run #2: ActivityRecord{35cb04 u0 com.leo.component/com.leo.activity.ActActivity t137}
        Run #1: ActivityRecord{42f5226 u0 com.leo.component/com.leo.activity.ActActivity t137}
        Run #0: ActivityRecord{20b2fb2 u0 com.leo.component/.MainActivity t137}
```


但是Android7-8.1是不会报错的，他会判断传入的bundle是否为空

9.0以上又不允许了。

#### SingleTop

ActivityA和ActivityB互相开启，随后ActivityA连续开启自身，发现ActivityA不再创建，但是会回调`onNewIntent`方法

```shell
Running activities (most recent first):
      TaskRecord{86343e2 #139 A=com.leo.component U=0 StackId=15 sz=6}
        Run #5: ActivityRecord{fcfcd4b u0 com.leo.component/com.leo.activity.ActActivity t139}
        Run #4: ActivityRecord{48e575b u0 com.leo.component/com.leo.activity.ResultActivity t139}
        Run #3: ActivityRecord{525f0c8 u0 com.leo.component/com.leo.activity.ActActivity t139}
        Run #2: ActivityRecord{54f3f75 u0 com.leo.component/com.leo.activity.ResultActivity t139}
        Run #1: ActivityRecord{f89596f u0 com.leo.component/com.leo.activity.ActActivity t139}
        Run #0: ActivityRecord{725259c u0 com.leo.component/.MainActivity t139}
```



#### SingleTask

将ActivityA和ActivityB依次打开，然后在ActivityB中启动ActivityA

```
 Running activities (most recent first):
      TaskRecord{b2a72ca #140 A=com.leo.component U=0 StackId=16 sz=3}
        Run #2: ActivityRecord{d1b6ace u0 com.leo.component/com.leo.activity.ResultActivity t140}
        Run #1: ActivityRecord{c2473d3 u0 com.leo.component/com.leo.activity.ActActivity t140}
        Run #0: ActivityRecord{edf1928 u0 com.leo.component/.MainActivity t140}
```



发现ActivityB被弹出栈，被调用了相对应的onStop和onDestory方法

```
Running activities (most recent first):
      TaskRecord{b2a72ca #140 A=com.leo.component U=0 StackId=16 sz=2}
        Run #1: ActivityRecord{c2473d3 u0 com.leo.component/com.leo.activity.ActActivity t140}
        Run #0: ActivityRecord{edf1928 u0 com.leo.component/.MainActivity t140}
```

这个时候A再开启自身生命周期如下

```
2020-03-28 14:13:31.328 8401-8401/com.leo.component E/ActActivity: onPause
2020-03-28 14:13:31.328 8401-8401/com.leo.component E/ActActivity: onNewIntent
2020-03-28 14:13:31.329 8401-8401/com.leo.component E/ActActivity: onResume
```

#### SingleInstance

单独的栈，只有自己一个实例

### 3.显示意图和隐式意图

#### 隐式意图

* action 可以有多个，Intent匹配一个即可
* category，可以有多个，必须完全匹配，没有的话系统会提供DEFAULT
* data setData和setType会相互清空，提供了setDataAndType方法。

### 4.状态保存恢复

Activity中的实现是：为bundle存储一个WINDOW_HIERARCHY_TAG为Key的Bundle对象，来源是Window的saveHierarchyState方法

```java
protected void onSaveInstanceState(Bundle outState) {
    outState.putBundle(WINDOW_HIERARCHY_TAG, mWindow.saveHierarchyState());

    outState.putInt(LAST_AUTOFILL_ID, mLastAutofillId);
    Parcelable p = mFragments.saveAllState();
    if (p != null) {
        outState.putParcelable(FRAGMENTS_TAG, p);
    }
    if (mAutoFillResetNeeded) {
        outState.putBoolean(AUTOFILL_RESET_NEEDED, true);
        getAutofillManager().onSaveInstanceState(outState);
    }
    getApplication().dispatchActivitySaveInstanceState(this, outState);
}
```

来到PhoneWindow中

```java
public Bundle saveHierarchyState() {
    Bundle outState = new Bundle();
    if (mContentParent == null) {
        return outState;
    }

    SparseArray<Parcelable> states = new SparseArray<Parcelable>();
  	//最主要的方法是在这里
    mContentParent.saveHierarchyState(states);
    outState.putSparseParcelableArray(VIEWS_TAG, states);

    // Save the focused view ID.
    final View focusedView = mContentParent.findFocus();
    if (focusedView != null && focusedView.getId() != View.NO_ID) {
        outState.putInt(FOCUSED_ID_TAG, focusedView.getId());
    }

    // save the panels
    SparseArray<Parcelable> panelStates = new SparseArray<Parcelable>();
    savePanelState(panelStates);
    if (panelStates.size() > 0) {
        outState.putSparseParcelableArray(PANELS_TAG, panelStates);
    }

    if (mDecorContentParent != null) {
        SparseArray<Parcelable> actionBarStates = new SparseArray<Parcelable>();
        mDecorContentParent.saveToolbarHierarchyState(actionBarStates);
        outState.putSparseParcelableArray(ACTION_BAR_TAG, actionBarStates);
    }

    return outState;
}
```

然后通过View的saveHierarchyState方法分发事件，通知每个子View去保存状态

```java
public void saveHierarchyState(SparseArray<Parcelable> container) {
    dispatchSaveInstanceState(container);
}
```

```java
protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
    if (mID != NO_ID && (mViewFlags & SAVE_DISABLED_MASK) == 0) {
        mPrivateFlags &= ~PFLAG_SAVE_STATE_CALLED;
        Parcelable state = onSaveInstanceState();
        if ((mPrivateFlags & PFLAG_SAVE_STATE_CALLED) == 0) {
            throw new IllegalStateException(
                    "Derived class did not call super.onSaveInstanceState()");
        }
        if (state != null) {
            container.put(mID, state);
        }
    }
}
```

有两个要注意的地方，一个是mID必须有值，也就是View必须设置ID，第二个是View的状态存储在SparseArray中，ID不能有重复的。

恢复状态逻辑也类似，onRestoreInstanceState方法中通过WINDOW_HIERARCHY_TAG去取Bundle对象，通过Window进行遍历下发给每个子View



### 5.Activity栈

`taskAffinity`指定任务栈，不能和包名重复，会新建一个栈存储Activity

### 6.Activity启动过程

* ContextImpl#startActivity()

* Instrumentation#execStartActivity()

* AMS#startActivity()

* AMS#startActivityAsUser()

* ActivityStarter#execute()

* ActivityStarter#startActivity()//重载方法继续跟进

* ActivityStarter#startActivityUnchecked()

* ActivityStack#startActivityLocked()

* ActivityStack#ensureActivitiesVisibleLocked

* ActivityStack#makeVisibleAndRestartIfNeeded

* ActivityStackSupervisor#startSpecificActivityLocked

* ActivityStackSupervisor#realStartActivityLocked

  ```java
   final ClientTransaction clientTransaction = ClientTransaction.obtain(app.thread,r.appToken);
  				//这里添加了LaunchActivityItem
  				clientTransaction.addCallback(LaunchActivityItem.obtain(new Intent(r.intent),
                  System.identityHashCode(r),r.info,mergedConfiguration.getGlobalConfiguration(),
                  mergedConfiguration.getOverrideConfiguration(), r.compat,r.launchedFromPackage, 							task.voiceInteractor, app.repProcState, r.icicle, r.persistentState, results, 							newIntents, mService.isNextTransitionForward(),profilerInfo));
  
              final ActivityLifecycleItem lifecycleItem;
              if (andResume) {//ResumeActivityItem被添加到了setLifecycleStateRequest方法中
  							lifecycleItem =ResumeActivityItem.obtain(mService.isNextTransitionForward());
              } else {
                  lifecycleItem = PauseActivityItem.obtain();
              }
             clientTransaction.setLifecycleStateRequest(lifecycleItem);
  					mService.getLifecycleManager().scheduleTransaction(clientTransaction);
  ```

  * 封装ClientTransaction，调用clientTransaction.addCallback(ClientTransactionItem item)

  * ClientTransactionItem是一个抽象类，子类有LaunchActivityItem、ResumeActivityItem、PauseActivityItem等对应Activity声明周期的类

    

* ActivityManagerService.getLifecycleManager().scheduleTransaction(ClientTransaction)

  ```java
  void scheduleTransaction(ClientTransaction transaction) throws RemoteException {
          final IApplicationThread client = transaction.getClient();
          transaction.schedule();//调用schedule方法
          if (!(client instanceof Binder)) {
              transaction.recycle();
          }
      }
  ```

* ClientTransaction内部的schedule方法

  ```java
  public void schedule() throws RemoteException {
          mClient.scheduleTransaction(this);//mClient是ApplicationThread，还要回去再看
  }
  ```

* ApplicationThread的scheduleTransaction

  ```java
  public void scheduleTransaction(ClientTransaction transaction) throws RemoteException {
      ActivityThread.this.scheduleTransaction(transaction);
  }
  ```

* ActivityThread本身并没有这个方法，在它的父类中定义了

  ```java
  void scheduleTransaction(ClientTransaction transaction) {
          transaction.preExecute(this);
          sendMessage(ActivityThread.H.EXECUTE_TRANSACTION, transaction);
  }
  ```

  ```java
  case EXECUTE_TRANSACTION:
      final ClientTransaction transaction = (ClientTransaction) msg.obj;
      mTransactionExecutor.execute(transaction);
      if (isSystem()) {
          transaction.recycle();
      }
      break;
  ```

* TransactionExecutor.execute(ClientTransaction transaction)方法

```java
public void execute(ClientTransaction transaction) {
        final IBinder token = transaction.getActivityToken();
  		 //这里就要执行CallBack方法
        executeCallbacks(transaction);
			 //这里也很重要，刚才添加的ResumeActivityItem，随后要调用onResume方法
			 executeLifecycleState(transaction);
        mPendingActions.clear();
    }
```

```java
 public void executeCallbacks(ClientTransaction transaction) {
        final List<ClientTransactionItem> callbacks = transaction.getCallbacks();
        ......
        final int size = callbacks.size();
        for (int i = 0; i < size; ++i) {
            final ClientTransactionItem item = callbacks.get(i);
					 //真正执行了ClientTransactionItem的execute方法
            item.execute(mTransactionHandler, token, mPendingActions);
            item.postExecute(mTransactionHandler, token, mPendingActions);
            ......
        }
    }
```

然后再回头看下LaunchActivityItem的execute方法

```java
public void execute(ClientTransactionHandler client, IBinder token,
            PendingTransactionActions pendingActions) {
		......
     client.handleLaunchActivity(r, pendingActions, null /* customIntent */);
}
```

client是ClientTransactionHandler类型，其实就是ActivityThread的父类，真正的调用对象还是ActivityThread。

回到handleLaunchActivity方法

```java
public Activity handleLaunchActivity(ActivityClientRecord r,
            PendingTransactionActions pendingActions, Intent customIntent) {
        ......
        final Activity a = performLaunchActivity(r, customIntent);
				......
        return a;
}
```

然后是performLaunchActivity

```java
private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
        ActivityInfo aInfo = r.activityInfo;
        ......
        ComponentName component = r.intent.getComponent();
        if (component == null) {
            component = r.intent.resolveActivity(
                mInitialApplication.getPackageManager());
            r.intent.setComponent(component);
        }
        if (r.activityInfo.targetActivity != null) {
            component = new ComponentName(r.activityInfo.packageName,
                    r.activityInfo.targetActivity);
        }
        //为Activity准备Context，实际调用ContextImpl.createActivityContext#new ContextImpl
        ContextImpl appContext = createBaseContextForActivity(r);
        Activity activity = null;
        try {
        		//加载Activity
            java.lang.ClassLoader cl = appContext.getClassLoader();
            activity = mInstrumentation.newActivity(
                    cl, component.getClassName(), r.intent);
            StrictMode.incrementExpectedActivityCount(activity.getClass());
            r.intent.setExtrasClassLoader(cl);
            r.intent.prepareToEnterProcess();
            if (r.state != null) {
                r.state.setClassLoader(cl);
            }
        } catch (Exception e) {
           ......
        }
        try {
	          //准备后面要用的Application，makeApplication一般不会重复创建
            Application app = r.packageInfo.makeApplication(false, mInstrumentation);
            if (activity != null) {
                CharSequence title = r.activityInfo.loadLabel(appContext.getPackageManager());
							......
                Window window = null;
                if (r.mPendingRemoveWindow != null && r.mPreserveWindow) {
                    window = r.mPendingRemoveWindow;
                    r.mPendingRemoveWindow = null;
                    r.mPendingRemoveWindowManager = null;
                }
                appContext.setOuterContext(activity);
                //回调attach方法
                activity.attach(appContext, this, getInstrumentation(), r.token,
                        r.ident, app, r.intent, r.activityInfo, title, r.parent,
                        r.embeddedID, r.lastNonConfigurationInstances, config,
                        r.referrer, r.voiceInteractor, window, r.configCallback);
                ......
                int theme = r.activityInfo.getThemeResource();
                if (theme != 0) {//设置主题
                    activity.setTheme(theme);
                }
							//回调onCreate方法
                activity.mCalled = false;
                if (r.isPersistable()) {
                    mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);
                } else {
                    mInstrumentation.callActivityOnCreate(activity, r.state);
                }
                r.activity = activity;
            }
            r.setState(ON_CREATE);
            mActivities.put(r.token, r);

        } catch (Exception e) {
            ......
        }
        return activity;
    }
```

至此Activity的启动过程结束，onCreate方法和onResume方法都会被执行,



其他的声明周期会在cycleToPath方法调用的performLifecycleSequence方法中找到，

```java
private void performLifecycleSequence(ActivityClientRecord r, IntArray path) {
        final int size = path.size();
        for (int i = 0, state; i < size; i++) {
            state = path.get(i);
            log("Transitioning to state: " + state);
            switch (state) {
                case ON_CREATE:
                    mTransactionHandler.handleLaunchActivity(r, mPendingActions,
                            null /* customIntent */);
                    break;
                case ON_START:
                    mTransactionHandler.handleStartActivity(r, mPendingActions);
                    break;
                case ON_RESUME:
                    mTransactionHandler.handleResumeActivity(r.token, false /* finalStateRequest */,
                            r.isForward, "LIFECYCLER_RESUME_ACTIVITY");
                    break;
                case ON_PAUSE:
                    mTransactionHandler.handlePauseActivity(r.token, false /* finished */,
                            false /* userLeaving */, 0 /* configChanges */, mPendingActions,
                            "LIFECYCLER_PAUSE_ACTIVITY");
                    break;
                case ON_STOP:
                    mTransactionHandler.handleStopActivity(r.token, false /* show */,
                            0 /* configChanges */, mPendingActions, false /* finalStateRequest */,
                            "LIFECYCLER_STOP_ACTIVITY");
                    break;
                case ON_DESTROY:
                    mTransactionHandler.handleDestroyActivity(r.token, false /* finishing */,
                            0 /* configChanges */, false /* getNonConfigInstance */,
                            "performLifecycleSequence. cycling to:" + path.get(size - 1));
                    break;
                case ON_RESTART:
                    mTransactionHandler.performRestartActivity(r.token, false /* start */);
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected lifecycle state: " + state);
            }
        }
    }
```



