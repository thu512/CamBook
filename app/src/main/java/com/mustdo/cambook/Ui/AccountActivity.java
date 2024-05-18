package com.mustdo.cambook.Ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mustdo.cambook.Model.User;
import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.Util.U;
import com.mustdo.cambook.databinding.ActivityAccountBinding;

import java.io.File;


public class AccountActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener {

    private ActivityAccountBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account);
        initGoogleLoginInit();

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        if(user!=null){
            if (user.isAnonymous()) {
                binding.email.setVisibility(View.VISIBLE);
                binding.idbox.setText("비회원 입니다.");
            } else {
                DocumentReference userRef = db.collection("users").document(user.getUid());
                userRef.get().addOnSuccessListener(documentSnapshots -> {
                    if(documentSnapshots != null){
                        User user = documentSnapshots.toObject(User.class);
                        binding.idbox.setText("" + user.getName());
                    }

                }).addOnFailureListener(e -> {
                    U.getInstance().toast(AccountActivity.this, "" + e.getMessage());
                });

            }
        }



        binding.email.setOnClickListener(view -> {
                    Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
                    intent.putExtra("Anony", true);
                    startActivity(intent);
                }
        );


        binding.logout.setOnClickListener(view -> {

            if(user == null){
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            else if(user.isAnonymous()){
                U.getInstance().showPopup3(AccountActivity.this,
                        "경고", "비회원 로그아웃 시 데이터가 사라집니다.",
                        "확인",(sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            logOut();
                        }),
                        "취소", sweetAlertDialog -> sweetAlertDialog.dismissWithAnimation());
            }else {
                logOut();
            }

        });
    }

    public void logOut(){
        //사진 파일 삭제
        removeFiles();
        //sp삭제
        U.getInstance().removeAllPreferences(this);

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                status -> U.getInstance().log("구글 로그 아웃"));
        signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    //저장된 디렉 사진 삭제
    public void removeFiles()
    {
        // 과목별 사진 저장소 path
        File dir = new File(this.getExternalFilesDir(Environment.DIRECTORY_DCIM),"");

        //Log.d("TTT",dir.getPath());



        String[] children = dir.list();
//        for(String n : children){
//            //Log.d("TTT",""+n);
//        }

        if (children != null) {
            for(String n : children){
                File mediaStorageDir = new File(dir,n);
                //Log.d("TTT",""+mediaStorageDir.getPath());
                File[] childFileList = mediaStorageDir.listFiles();
                for(File childFile : childFileList)
                {
                    childFile.delete();    //하위 파일
                }
                mediaStorageDir.delete();
            }
        }//if

//        children = dir.list();
//        for(String n : children){
//            Log.d("TTT","삭제후 "+n);
//        }

    }//public void removeFiles()



    private GoogleApiClient mGoogleApiClient;   // 구글 로그인 담당 객체(Api 담당 객체)

    // google
    // 초기화
    public void initGoogleLoginInit() {
        // [START config_signin]
        // Configure Google Sign In
        String str = getString(
                this.getResources().getIdentifier(
                        "default_web_client_id",
                        "string",
                        this.getPackageName()
                ));

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(str)
                .requestEmail()
                .build();
        // [END config_signin]
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



}
