



### 添加方式

#### 1.布局添加

```xml
<fragment
    android:name="com.leo.fragment.FirstFragment"
    android:background="#ccc"
    android:id="@+id/mainView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

#### 2.手动添加

```java
FragmentTransaction transaction = getFragmentManager().beginTransaction();
transaction.replace(R.id.mainView,new MainFragment());
transaction.commit();
```



### FragmentTransaction事务

#### 1.开启方式

```java
FragmentTransaction transaction = getFragmentManager().beginTransaction();
transaction.replace(R.id.mainView,new ThirdFragment());
transaction.addToBackStack("Second");
transaction.commit();
```



#### 2.方法

* add 添加，纯粹的覆盖，之前的UI还在
* replace 是remove和add的结合
* remove 移除
* show 显示视图
* hide 不会调用销毁的生命周期，保存视图
* addToBackStack 添加回退栈，类似Activity的返回

#### 3.replace的生命周期

A 调用replace（B）,B 调用replace（C）,而且二者都调用addToBackStack()方法，随后再按返回键。

```
FirstFragment: onAttach
FirstFragment: onCreate
FirstFragment: onViewCreated
FirstFragment: onStart
FirstFragment: onResume
SecondFragment: onAttach
SecondFragment: onCreate
FirstFragment: onPause
FirstFragment: onStop
FirstFragment: onDestroyView
SecondFragment: onViewCreated
SecondFragment: onStart
SecondFragment: onResume
ThirdFragment: onAttach
ThirdFragment: onCreate
SecondFragment: onPause
SecondFragment: onStop
SecondFragment: onDestroyView
ThirdFragment: onViewCreated
ThirdFragment: onStart
ThirdFragment: onResume
ThirdFragment: onPause
ThirdFragment: onStop
ThirdFragment: onDestroyView
ThirdFragment: onDestroy
ThirdFragment: onDetach
SecondFragment: onViewCreated
SecondFragment: onStart
SecondFragment: onResume
SecondFragment: onPause
SecondFragment: onStop
SecondFragment: onDestroyView
SecondFragment: onDestroy
SecondFragment: onDetach
FirstFragment: onViewCreated
FirstFragment: onStart
FirstFragment: onResume
FirstFragment: onPause
FirstFragment: onStop
FirstFragment: onDestroyView
FirstFragment: onDestroy
FirstFragment: onDetach
```



#### 4.commitAllowingStateLoss

activity调用了onSaveInstanceState()之后，再commit一个事务就会出现该异常。那如果不想抛出异常，也可以很简单调用commitAllowingStateLoss()方法来略过这个检查就可以了.也得区分场景，银行的APP肯定不能没钱了。



### Activity解析<fragment>标签流程

Activity添加布局的时候使用LayoutInflater,继而调用LayoutInflater的createViewFromTag方法，然后内部的几个Factory的onCreateView方法

```java
View createViewFromTag(View parent, String name, Context context, AttributeSet attrs,
        boolean ignoreThemeAttr) {
    if (name.equals("view")) {
        name = attrs.getAttributeValue(null, "class");
    }
    try {
        View view;
        if (mFactory2 != null) {
            view = mFactory2.onCreateView(parent, name, context, attrs);
        } else if (mFactory != null) {
            view = mFactory.onCreateView(name, context, attrs);
        } else {
            view = null;
        }
        //以上两步之后都没有创建View，比如Fragment标签
        if (view == null && mPrivateFactory != null) {
            view = mPrivateFactory.onCreateView(parent, name, context, attrs);
        }
        if (view == null) {
            ...反射实例View
        }
        return view;
    } catch (InflateException e) {
     ,,,,,,
    }
}
```

但是具体调用哪个Factory呢？

在Activity的attach方法内部有一行代码

```java
mWindow.getLayoutInflater().setPrivateFactory(this);
```

意味着Activity要实现Factory2接口,覆写onCreateView方法，

```java
public interface Factory2 extends Factory 
```

Factory2是继承自Factory，实现一个版本兼容，再往下看，就和Fragment有联系了

```java
public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
    if (!"fragment".equals(name)) {
        return onCreateView(name, context, attrs);
    }

    return mFragments.onCreateView(parent, name, context, attrs);
}
```

后续就到了FragmentManager的onCreateView

```java
@Override
public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
    if (!"fragment".equals(name)) {
        return null;
    }

    String fname = attrs.getAttributeValue(null, "class");
    TypedArray a =
            context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.Fragment);
    if (fname == null) {
        fname = a.getString(com.android.internal.R.styleable.Fragment_name);
    }
    int id = a.getResourceId(com.android.internal.R.styleable.Fragment_id, View.NO_ID);
    String tag = a.getString(com.android.internal.R.styleable.Fragment_tag);
    a.recycle();

    int containerId = parent != null ? parent.getId() : 0;
    //ID的设置检验
    if (containerId == View.NO_ID && id == View.NO_ID && tag == null) {
        throw new IllegalArgumentException(attrs.getPositionDescription()
                + ": Must specify unique android:id, android:tag, or have a parent with"
                + " an id for " + fname);
    }

    Fragment fragment = id != View.NO_ID ? findFragmentById(id) : null;
    if (fragment == null && tag != null) {
        fragment = findFragmentByTag(tag);
    }
    if (fragment == null && containerId != View.NO_ID) {
        fragment = findFragmentById(containerId);
    }
    if (fragment == null) {
        fragment = mContainer.instantiate(context, fname, null);
        fragment.mFromLayout = true;
        fragment.mFragmentId = id != 0 ? id : containerId;
        fragment.mContainerId = containerId;
        fragment.mTag = tag;
        fragment.mInLayout = true;
        fragment.mFragmentManager = this;
        fragment.mHost = mHost;
        fragment.onInflate(mHost.getContext(), attrs, fragment.mSavedFragmentState);
        addFragment(fragment, true);
    } else if (fragment.mInLayout) {
        // A fragment already exists and it is not one we restored from
        // previous state.
        throw new IllegalArgumentException(attrs.getPositionDescription()
                + ": Duplicate id 0x" + Integer.toHexString(id)
                + ", tag " + tag + ", or parent id 0x" + Integer.toHexString(containerId)
                + " with another fragment for " + fname);
    } else {
        fragment.mInLayout = true;
        fragment.mHost = mHost;
        if (!fragment.mRetaining) {
            fragment.onInflate(mHost.getContext(), attrs, fragment.mSavedFragmentState);
        }
    }
    //回调Fragment的声明周期方法
    if (mCurState < Fragment.CREATED && fragment.mFromLayout) {
        moveToState(fragment, Fragment.CREATED, 0, 0, false);
    } else {
        moveToState(fragment);
    }
    if (id != 0) {
        fragment.mView.setId(id);
    }
    if (fragment.mView.getTag() == null) {
        fragment.mView.setTag(tag);
    }//最后返回Fragment的View，添加给LayoutInflater
    return fragment.mView;
}
```



### 其他问题



* 使用onSaveStateInstance存储状态，避免内存重启
* hide和show效率更高，但是要注意上面的问题