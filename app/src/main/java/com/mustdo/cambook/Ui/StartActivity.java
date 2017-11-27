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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StartActivity extends Activity {
    FirebaseAuth user;
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수
    private String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if(execCmd() || checkFile()){
            finish();
        }

        user = FirebaseAuth.getInstance();



        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!U.getInstance().getBoolean(StartActivity.this,"first")){
                    U.getInstance().setBoolean(StartActivity.this,"autosave",true);
                    U.getInstance().setBoolean(StartActivity.this,"startphoto",false);

                }
                //권한 요청 메소드
                checkPermissions();


            }
        },3000);

    }
    public void startApp(){
        if(user.getCurrentUser()!=null){
            if(user.getCurrentUser().isEmailVerified()){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }else{
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
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
        boolean f1=true;
        boolean f2=true;
        boolean f3=true;
        switch (requestCode){
            case MULTIPLE_PERMISSIONS: {
                if(grantResults.length > 0){
                    for(int i=0; i<permissions.length;i++){
                        if(permissions[i].equals(this.permissions[0])){
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                                popup();
                                f1=false;
                            }else{
                                f1=true;
                            }
                        }else if(permissions[i].equals(this.permissions[1])){
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                                popup();
                                f2=false;
                            }else{
                                f2=true;
                            }
                        }else if(permissions[i].equals(this.permissions[2])){
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                                popup();
                                f3=false;
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
                sweetAlertDialog -> finish(),
                null,
                null
        );
    }



    public boolean execCmd() {
        boolean flag = false;
        try {
            Runtime.getRuntime().exec("su");
            flag = true;
        } catch(Exception e) {
            flag = false;
        }

        return flag;
    }

    public boolean checkFile() {
        String[] arrayOfString = {"/system/bin/.ext", "/system/xbin/.ext","/sbin/su","/data/data/com.noshufou.android.su",
        "/system/app/SuperUser.apk","/system/xbin/su","/system/bin/su"};
        int i=0;
        while(true) {
            if(i >= arrayOfString.length)
                return false;
            if(new File(arrayOfString[i]).exists())
                return true;

            i++;
        }
    }


}
