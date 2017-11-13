package com.mustdo.cambook.Ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mustdo.cambook.Model.User;
import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.Util.U;
import com.mustdo.cambook.databinding.ActivityJoinBinding;

public class JoinActivity extends Activity{
    ActivityJoinBinding binding;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_join);
        Intent intent = getIntent();
        final Boolean anony = intent!=null? intent.getBooleanExtra("Anony",false):false;

        U.getInstance().log(""+anony);
        firebaseAuth = FirebaseAuth.getInstance();

        binding.btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(anony){
                    onAnLinkEmail();
                }else{
                    onEmailSignUp();
                }

            }
        });
    }


    public void onEmailSignUp(){

        if(!isVaild()){
            return;
        }
        showPd();
        final String email = binding.email.getText().toString();
        final String password = binding.pwd.getText().toString();
        final String name = binding.name.getText().toString();

        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //실시간 디비에 회원 정보 삽입
                    insertUserInfo(firebaseAuth.getCurrentUser(),password,email,name);

                }else{
                    stopPd();
                    U.getInstance().toast(getApplicationContext(), "가입실패.\n"+task.getException().getMessage());

                }

            }
        });

    } //이메일 회원가입
    public void insertUserInfo(FirebaseUser user,String pwd, String email, String name){
        User u = new User(user.getUid(),pwd,email,name);

        //firestore 입력
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .set(u)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        stopPd();
                        U.getInstance().toast(getApplicationContext(), "firestone ok");

                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        stopPd();
                        U.getInstance().toast(getApplicationContext(), "firestone fail"+e.getMessage());

                    }
                } );
    }

    public void onAnLinkEmail(){
        showPd();
        if(!isVaild()){
            return;
        }
        showPd();
        final String email = binding.email.getText().toString();
        final String password = binding.pwd.getText().toString();
        final String name = binding.name.getText().toString();

        getUser().linkWithCredential(EmailAuthProvider.getCredential(email,password))
                .addOnCompleteListener(new OnCompleteListener<AuthResult>(){
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //연결이 성공되었으니, 허가된 서버로 이동
                            U.getInstance().log("연결완료");
                            U.getInstance().toast(JoinActivity.this,"이메일연결 성공.");

                            //firestore 업데이트 및 메인화면으로 이동
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(firebaseAuth.getCurrentUser().getUid())
                            .update(
                                    "email", email,
                                    "pwd",password,
                                    "name",name
                            ).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    stopPd();
                                    Intent intent = new Intent(getApplicationContext(),StartActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            stopPd();

                        }else{
                            //실패 사유 보여주기
                            stopPd();
                            U.getInstance().toast(JoinActivity.this,""+task.getException().getMessage());
                        }

                    }
                });


    } //익명계정에 이메일 비번 연결

    public boolean isVaild(){
        String email = binding.email.getText().toString();
        String password = binding.pwd.getText().toString();
        String name = binding.name.getText().toString();


        if(TextUtils.isEmpty(email)){
            binding.email.setError("이메일을 입력하세요.");
            return false;
        }
        if(TextUtils.isEmpty(password)){
            binding.pwd.setError("비밀번호를 입력하세요.");
            return false;
        }

        if(TextUtils.isEmpty(password)){
            binding.name.setError("이름을 입력하세요.");
            return false;
        }

        return true;
    }
}
