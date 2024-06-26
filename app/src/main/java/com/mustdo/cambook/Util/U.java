package com.mustdo.cambook.Util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

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

public class U extends Application{
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
        Log.d("TTT",""+msg);
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

            Bitmap bitmap = getRotatedPhoto(inputFile); //inputfile 사진 회전
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,out); //회전한 사진을 out에 저장


            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputFile).delete();
        }

        catch (FileNotFoundException e) {
            //toast(getApplicationContext(), "사진 저장실패.");
            FirebaseCrash.log("사진 저장 에러(파일이동): "+e.getMessage());
            FirebaseCrash.log("사진 저장 에러(파일이동): "+e.getLocalizedMessage());
            Log.e("tag", e.getMessage());
        }
        catch (Exception e) {
            //toast(getApplicationContext(), "사진 저장실패.");
            FirebaseCrash.log("사진 저장 에러(파일이동): "+e.getMessage());
            FirebaseCrash.log("사진 저장 에러(파일이동): "+e.getLocalizedMessage());
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


    //사진 회전 메소드
    public Bitmap getRotatedPhoto(String path){
        try{
            Bitmap image = BitmapFactory.decodeFile(path);

            ExifInterface exif= new ExifInterface(path);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            Bitmap bmRotated = rotateBitmap(image, exifOrientation);

            return bmRotated;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    //회전된 각도에 따라 사진 회전
    public Bitmap rotateBitmap(Bitmap bitmap, int orientation){
        Matrix matrix = new Matrix();
        switch (orientation){
            case ExifInterface.ORIENTATION_NORMAL: return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1,1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1,1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1,1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1,1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;

        }
        try{
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),matrix,true);
            bitmap.recycle();
            return bmRotated;
        }catch (OutOfMemoryError e){
            e.printStackTrace();
            return null;
        }
    }

    public static Uri convertContentToFileUri(Context ctx, Uri uri) throws Exception {
        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(uri, null, null, null, null);
            cursor.moveToNext();
            return Uri.fromFile(new File(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))));
        } finally {
            if(cursor != null)
                cursor.close();
        }
    }

}
