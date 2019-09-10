// INotify.aidl
package com.leo.service;
import com.leo.service.Book;
// Declare any non-default types here with import statements
interface INotify {
    //用于服务向客户端推送数据
    void notifyBooks(in List<Book> obj);
}
