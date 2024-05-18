package com.mustdo.cambook.Ui;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mustdo.cambook.Model.User;
import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.Util.U;
import com.mustdo.cambook.databinding.ActivityJoinBinding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoinActivity extends Activity {
    private ActivityJoinBinding binding;
    private FirebaseAuth firebaseAuth;
    private Boolean pw_check = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_join);
        binding.back.setOnClickListener(view -> finish());

        Intent intent = getIntent();
        final Boolean anony = intent != null ? intent.getBooleanExtra("Anony", false) : false;

        U.getInstance().log("" + anony);
        firebaseAuth = FirebaseAuth.getInstance();

        //익명에서 전환인지, 신규인지
        binding.btn1.setOnClickListener(view -> {
            if (anony) {
                onAnLinkEmail();
            } else {
                onEmailSignUp();
            }
        });


        //비밀번호 검증
        binding.pwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                boolean f1 = false;
                boolean f2 = false;

                if (s.length() >= 6 && s.length() <= 16) {
                    binding.pwdMax.setTextColor(Color.parseColor("#ffe715"));
                    f1 = true;
                } else {
                    binding.pwdMax.setTextColor(Color.parseColor("#9b9b9b"));
                    f1 = false;
                }


                Pattern p = Pattern.compile("([a-zA-Z])");
                Matcher m = p.matcher(s);
                Pattern p1 = Pattern.compile("([0-9])");
                Matcher m1 = p1.matcher(s);
                Pattern p2 = Pattern.compile("([!,@,#,$,%,^,&,*,?,_,~])");
                Matcher m2 = p2.matcher(s);
                if (m.find() && m1.find() && m2.find()) {
                    binding.pwdEng.setTextColor(Color.parseColor("#ffe715"));
                    f2 = true;
                } else {
                    binding.pwdEng.setTextColor(Color.parseColor("#9b9b9b"));
                    f2 = false;
                }

                if (f1 && f2) {

                    //비밀번호 조건식 완료
                    pw_check = true;
                } else {
                    pw_check = false;
                }
            }

        });
    }

    //이메일 회원가입
    public void onEmailSignUp() {

        if (!isVaild()) {
            return;
        }
        showPd();
        final String email = binding.email.getText().toString();
        final String password = binding.pwd.getText().toString();
        final String name = binding.name.getText().toString();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //실시간 디비에 회원 정보 삽입
                insertUserInfo(firebaseAuth.getCurrentUser(), password, email, name);
            } else {
                stopPd();
                U.getInstance().toast(getApplicationContext(), "가입실패.\n" + task.getException().getMessage());
            }
        });

    }


    //firestore
    public void insertUserInfo(FirebaseUser user, String pwd, String email, String name) {
        User u = new User(user.getUid(), pwd, email, name);

        //firestore 입력
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .set(u)
                .addOnSuccessListener(aVoid -> {
                    stopPd();
                    //U.getInstance().toast(getApplicationContext(), "firestone ok");
                    if (user.isEmailVerified()) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        U.getInstance().showPopup2(JoinActivity.this,
                                "이메일 인증",
                                "이메일 인증 후 사용해 주세요.",
                                "확인",
                                sweetAlertDialog -> {
                                    user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        U.getInstance().toast(getApplicationContext(), "인증 메일이 전송되었습니다.");
                                                    }
                                                    finish();
                                                }
                                            });
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                        );
                    }
                })
                .addOnFailureListener(e -> {
                    stopPd();
                    U.getInstance().toast(getApplicationContext(), "firestone fail" + e.getMessage());
                });
    }


    //익명계정에 이메일 비번 연결
    public void onAnLinkEmail() {
        showPd();
        if (!isVaild()) {
            return;
        }
        showPd();
        final String email = binding.email.getText().toString();
        final String password = binding.pwd.getText().toString();
        final String name = binding.name.getText().toString();

        getUser().linkWithCredential(EmailAuthProvider.getCredential(email, password))
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                //연결이 성공되었으니, 허가된 서버로 이동
                                U.getInstance().log("연결완료");
                                U.getInstance().toast(JoinActivity.this, "이메일연결 성공.");

                                //firestore 업데이트 및 메인화면으로 이동
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users").document(firebaseAuth.getCurrentUser().getUid())
                                        .update(
                                                "email", email,
                                                "pwd", password,
                                                "name", name
                                        )
                                        .addOnSuccessListener(aVoid -> {
                                            stopPd();
                                            if (getUser().isEmailVerified()) {
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                U.getInstance().showPopup2(JoinActivity.this,
                                                        "이메일 인증",
                                                        "이메일 인증 후 사용해 주세요.",
                                                        "확인",
                                                        sweetAlertDialog -> {
                                                            getUser().sendEmailVerification()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                U.getInstance().toast(getApplicationContext(), "인증 메일이 전송되었습니다.");
                                                                            }
                                                                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                            startActivity(intent);
                                                                            finish();
                                                                        }
                                                                    });
                                                            sweetAlertDialog.dismissWithAnimation();
                                                        }
                                                );
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            stopPd();
                                            U.getInstance().toast(getApplicationContext(), "firestore fail" + e.getMessage());
                                        });
                            } else {
                                //실패 사유 보여주기
                                stopPd();
                                U.getInstance().toast(JoinActivity.this, "" + task.getException().getMessage());
                            }
                        }
                );
    }

    public boolean isVaild() {
        String email = binding.email.getText().toString();
        String password = binding.pwd.getText().toString();
        String name = binding.name.getText().toString();


        if (TextUtils.isEmpty(email)) {
            binding.email.setError("이메일을 입력하세요.");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            binding.pwd.setError("비밀번호를 입력하세요.");
            return false;
        }
        if(!pw_check){
            binding.pwd.setError("비밀번호를 확인해주세요.");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            binding.name.setError("이름을 입력하세요.");
            return false;
        }

        return true;
    }
}
