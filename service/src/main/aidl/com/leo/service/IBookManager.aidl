// IBookManager.aidl
package com.leo.service;
import com.leo.service.Book;
import com.leo.service.INotify;
// Declare any non-default types here with import statements

interface IBookManager {
    void add(in Book p);
    List<Book> getBookList();
    int getPid();
    //服务向固定的客户端接收数据
    void register(INotify notify);
}
