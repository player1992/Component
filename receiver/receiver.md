

AmS调用发送广播流程



#### 注意

默认不会发给已经关闭的应用

```java
Intent.FLAG_EXCLUDE_STOPPED_PACKAGES
```

如果要调起已经关闭的应用要添加`FLAG_INCLUDE_STOPPED_PACKAGES`,二者共存的时候以`FLAG_INCLUDE_STOPPED_PACKAGES`为准