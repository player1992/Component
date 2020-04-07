package com.leo.provider;

/**
 * <p>Date:2020-04-07.15:11</p>
 * <p>Author:niu bao</p>
 * <p>Desc:</p>
 */
public class User {
    String name;
    int sex;
    int id;

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", sex=" + sex +
                ", id=" + id +
                '}';
    }
}
