package com.mustdo.cambook.Model;

/**
 * Created by lcj on 2017. 11. 14..
 */

public class Subject {
    String subject;
    String item; //요일
    String s_time;
    String e_time;
    String color;

    @Override
    public String toString() {
        return "Subject{" +
                "subject='" + subject + '\'' +
                ", item='" + item + '\'' +
                ", s_time='" + s_time + '\'' +
                ", e_time='" + e_time + '\'' +
                ", color='" + color + '\'' +
                '}';
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getS_time() {
        return s_time;
    }

    public void setS_time(String s_time) {
        this.s_time = s_time;
    }

    public String getE_time() {
        return e_time;
    }

    public void setE_time(String e_time) {
        this.e_time = e_time;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Subject() {
    }

    public Subject(String subject, String item, String s_time, String e_time, String color) {
        this.subject = subject;
        this.item = item;
        this.s_time = s_time;
        this.e_time = e_time;
        this.color = color;
    }
}
