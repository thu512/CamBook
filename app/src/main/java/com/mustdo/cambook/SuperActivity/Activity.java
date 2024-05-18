package com.mustdo.cambook.SuperActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Changjoo on 2017-08-07.
 */

//슈퍼클래스 재정의

public class Activity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#305E98"));
        }

    }

    //로딩
    SweetAlertDialog pd;
    public void showPd(){
        if(pd == null){
            pd = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pd.setCancelable(false);
            pd.setContentText("잠시만 기다려주세요...");

        }
        pd.show();
    }

    public void stopPd(){
        if(pd != null && pd.isShowing()){
            pd.dismiss();
        }
    }


    //현재로그인한 fb의 유저 정보
    public FirebaseUser getUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public void signOut(){
        FirebaseAuth.getInstance().signOut();
    }

}
