package com.mustdo.cambook.Ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.Util.U;
import com.mustdo.cambook.databinding.ActivityFindPwdBinding;

public class FindPwdActivity extends Activity {

    private ActivityFindPwdBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_find_pwd);


        binding.submit.setOnClickListener(view -> {
            if (!isVaild()) {
                return;
            }

            //비밀번호 재설정 메일 전송
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String emailAddress = binding.id.getText().toString();

            auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(task -> {
                        U.getInstance().log("이메일 전송 성공");
                        U.getInstance().showPopup2(FindPwdActivity.this, "비밀번호 재설정", "비밀번호 재설정 메일이 전송되었습니다.",
                                "확인",
                                sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    finish();
                                });
                    })
                    .addOnFailureListener(e -> U.getInstance().toast(getApplicationContext(), "" + e.getMessage()));
        });


        //글자입력시  지우기 버튼 생성
        binding.id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                if (s.length() >= 1) {
                    binding.emailDelete.setVisibility(View.VISIBLE);
                } else {
                    binding.emailDelete.setVisibility(View.INVISIBLE);
                }
            }
        });


        //뒤로가기
        binding.back.setOnClickListener((view) -> finish());

    }

    //x버튼 클릭시 입력한데이터 삭제
    public void textDelete(View view) {
        if (view.equals(binding.emailDelete)) {
            binding.id.setText("");
        }
    }


    //공백 검증
    public boolean isVaild() {
        String email = binding.id.getText().toString();


        if (TextUtils.isEmpty(email)) {
            binding.id.setError("이메일을 입력하세요.");
            return false;
        }

        return true;
    }

}

