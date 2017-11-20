package com.mustdo.cambook.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    //초기화
    public void removeAllPreferences(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences(SAVE_TAG, 0).edit();
        editor.clear();
        editor.commit();
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
        //Log.d("TTT",""+msg);
    }


    //파일 디렉토리 이동
    public void moveFile(String inputFile, String outputPath, String outputFile) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(inputFile);
            out = new FileOutputStream(outputPath +"/"+ outputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputFile).delete();


        }

        catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }


    //특정 날짜 요일 구하기
    public String getDateDay(String date) throws Exception {


        String day = "" ;

        SimpleDateFormat dateFormat = null;
        try {
            dateFormat = new SimpleDateFormat("yyyyMMdd");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Date nDate = dateFormat.parse(date) ;

        Calendar cal = Calendar.getInstance() ;
        cal.clear();
        cal.setTime(nDate);

        int dayNum = cal.get(Calendar.DAY_OF_WEEK) ;

        switch(dayNum){
            case 1:
                day = "일";
                break ;
            case 2:
                day = "월";
                break ;
            case 3:
                day = "화";
                break ;
            case 4:
                day = "수";
                break ;
            case 5:
                day = "목";
                break ;
            case 6:
                day = "금";
                break ;
            case 7:
                day = "토";
                break ;

        }

        return day ;
    }

}
