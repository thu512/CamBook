package com.mustdo.cambook.Ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.databinding.ActivityFindPwdBinding;

public class FindPwdActivity extends Activity {

    ActivityFindPwdBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_find_pwd);

        //글자입력시  지우기 버튼 생성
        binding.id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
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

}

