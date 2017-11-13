package com.mustdo.cambook.Model;

/**
 * Created by lcj on 2017. 11. 13..
 */

public class User {
    String uid;
    String pwd;
    String email;
    String name;

    public User(String uid, String pwd, String email, String name) {
        this.uid = uid;
        this.pwd = pwd;
        this.email = email;
        this.name = name;
    }

    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", pwd='" + pwd + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
