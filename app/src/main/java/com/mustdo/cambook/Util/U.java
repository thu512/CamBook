package com.mustdo.cambook.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Changjoo on 2017-09-16.
 */

public class U {
    private static final U ourInstance = new U();

    public static U getInstance() {
        return ourInstance;
    }

    private U() {}


    String SAVE_TAG = "SETTING";
    public void setBoolean(Context context, String key, boolean value)
    {
        SharedPreferences.Editor editor = context.getSharedPreferences(SAVE_TAG, 0).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    public boolean getBoolean(Context context, String key)
    {
        return context.getSharedPreferences(SAVE_TAG, 0).getBoolean(key, false);
    }

    //ArrayList를 SP에 저장

    public void saveSharedPreferences_Data(Context context, String key, ArrayList<String> dic) {



        SharedPreferences pref =

                context.getSharedPreferences(SAVE_TAG,0);

        SharedPreferences.Editor edit = pref.edit();

        Set<String> set = new HashSet<String>();

        set.addAll(dic);

        edit.putStringSet(key, set);

        edit.commit();

    }

    //꺼내기

    public ArrayList<String> loadSharedPreferencesData(Context context, String key) {



        SharedPreferences pref =

                context.getSharedPreferences(SAVE_TAG,0);

        Set<String> set = pref.getStringSet(key, null);

        return set==null? new ArrayList<>(): new ArrayList<>(set);

    }




    //팝업 라이브러리
    public void showPopup3(Context context, String title, String msg,
                           String cName, SweetAlertDialog.OnSweetClickListener cEvent,
                           String oName, SweetAlertDialog.OnSweetClickListener oEvent
    ){
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(msg)
                .setConfirmText(cName)
                .setConfirmClickListener(cEvent)
                .setCancelText(oName)
                .setCancelClickListener(oEvent)
                .show();

    }

    //에러타입
    public void showPopup1(Context context, String title, String msg,
                           String cName, SweetAlertDialog.OnSweetClickListener cEvent
    ){
        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(title)
                .setContentText(msg)
                .setConfirmText(cName)
                .setConfirmClickListener(cEvent)
                .show();

    }


    //성공 타입
    public void showPopup2(Context context, String title, String msg,
                           String cName, SweetAlertDialog.OnSweetClickListener cEvent
    ){
        new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .setContentText(msg)
                .setConfirmText(cName)
                .setConfirmClickListener(cEvent)
                .show();

    }



    public void toast(Context context,String msg){
        Toast.makeText(context, ""+msg,Toast.LENGTH_LONG).show();
    }


    public void log(String msg){
        Log.d("TTT",""+msg);
    }
}
