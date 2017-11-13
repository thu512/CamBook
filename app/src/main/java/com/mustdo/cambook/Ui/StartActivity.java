package com.mustdo.cambook.Ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.Util.U;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class StartActivity extends Activity {
    FirebaseAuth user;
    private String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA};
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        user = FirebaseAuth.getInstance();


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!U.getInstance().getBoolean(StartActivity.this,"first")){
                    U.getInstance().setBoolean(StartActivity.this,"first",true);
                    U.getInstance().setBoolean(StartActivity.this,"autosave",false);
                    U.getInstance().setBoolean(StartActivity.this,"startphoto",false);

                }
                //권한 요청 메소드
                checkPermissions();


            }
        },3000);

    }
    public void startApp(){
        if(user.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }else{
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
        finish();
    }
    private void checkPermissions(){
        int result;
        List<String> permissionList = new ArrayList<>();        //String형의 permissionList만듬
        for(String pm: permissions){                            //3개 권한 가지는 permissions배열 foreach문으로 넣음
            result = ContextCompat.checkSelfPermission(this,pm);

            if(result != PackageManager.PERMISSION_GRANTED){ //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
                permissionList.add(pm);
            }
        }
        if(!permissionList.isEmpty()){ //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청한다.
            ActivityCompat.requestPermissions(this,permissionList.toArray(new String[permissionList.size()]),MULTIPLE_PERMISSIONS);
            return;
        }
        startApp();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean f1=false;
        boolean f2=false;
        boolean f3=false;
        switch (requestCode){
            case MULTIPLE_PERMISSIONS: {
                if(grantResults.length > 0){
                    for(int i=0; i<permissions.length;i++){
                        if(permissions[i].equals(this.permissions[0])){
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                                popup();
                            }else{
                                f1=true;
                            }
                        }else if(permissions[i].equals(this.permissions[1])){
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                                popup();
                            }else{
                                f2=true;
                            }
                        }else if(permissions[i].equals(this.permissions[2])){
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                                popup();
                            }else{
                                f3=true;
                            }
                        }
                    }
                }else{
                    popup();
                }

                if(f1&&f2&&f3){
                    startApp();
                }
                return;
            }
        }
    }


    public void popup(){
        U.getInstance().showPopup3(this,
                "알림",
                "권한사용을 동의해주셔야 이용이 가능합니다.",
                "확인",
                new SweetAlertDialog.OnSweetClickListener(){
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        finish();
                    }
                },
                null,
                null
        );
    }
}
