## 广播



### 普通广播

intent要setPackageName

### 有序广播

优先级

### 粘性广播

需要申请权限

### 本地广播

不涉及IPC，本地集合，进行遍历的调用

### 动态注册与静态注册

推荐动态注册，广播可以随着组件消失







AmS调用发送广播流程

### 注意

默认不会发给已经关闭的应用

```java
Intent.FLAG_EXCLUDE_STOPPED_PACKAGES
```

如果要调起已经关闭的应用要添加`FLAG_INCLUDE_STOPPED_PACKAGES`,二者共存的时候以`FLAG_INCLUDE_STOPPED_PACKAGES`为准