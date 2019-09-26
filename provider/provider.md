### 基本操作

#### 声明Provider，

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