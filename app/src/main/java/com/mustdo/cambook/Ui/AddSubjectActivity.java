package com.mustdo.cambook.Ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.Util.U;
import com.mustdo.cambook.databinding.ActivityAddSubjectBinding;

import java.util.ArrayList;

import petrov.kristiyan.colorpicker.ColorPicker;

import static com.mustdo.cambook.Ui.TimeTableActivity.ADD_CODE;

public class AddSubjectActivity extends Activity {

    private ActivityAddSubjectBinding binding;
    private String bgColor = "";
    private ArrayAdapter<String> adapter;
    private ArrayAdapter<String> adapter1;
    private ArrayAdapter<String> adapter2;
    private String item = "";     //요일
    private String s_time = "";   //시작 교시
    private String e_time = "";   //끝 교시
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_subject);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        binding.back.setOnClickListener((view -> finish()));

        initAdapter();

        binding.spinner.setAdapter(adapter);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                item = (String) parent.getSelectedItem();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        binding.spinner1.setAdapter(adapter1);
        binding.spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                s_time = (String) parent.getSelectedItem();

                if (parent.getSelectedItem().toString().equals("시작 교시")) {

                } else {
                    adapter2.clear();
                    adapter2.add("끝 교시");
                    for (int i = Integer.parseInt(s_time); i <= 11; i++) {
                        adapter2.add(Integer.toString(i));
                    }
                    binding.spinner2.setAdapter(adapter2);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.spinner2.setAdapter(adapter2);
        binding.spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                e_time = (String) parent.getSelectedItem();
                if (parent.getSelectedItem().toString().equals("끝 교시")) {

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    //어댑터 초기화
    public void initAdapter() {
        adapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add("요일을 선택하세요.");
        adapter.add("월");
        adapter.add("화");
        adapter.add("수");
        adapter.add("목");
        adapter.add("금");


        adapter1 = new ArrayAdapter<String>(this, R.layout.spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter1.add("시작 교시");
        adapter1.add("1");
        adapter1.add("2");
        adapter1.add("3");
        adapter1.add("4");
        adapter1.add("5");
        adapter1.add("6");
        adapter1.add("7");
        adapter1.add("8");
        adapter1.add("9");
        adapter1.add("10");
        adapter1.add("11");


        adapter2 = new ArrayAdapter<String>(this, R.layout.spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.add("시작교시를 선택하세요.");
    }




    //확인 버튼 클릭
    public void onSubmit(View view) {
        if (!isVaild()) {
            return;
        }

        Intent i = new Intent();
        i.putExtra("item",item);
        i.putExtra("subject",binding.editText.getText().toString());
        i.putExtra("s_time",s_time);
        i.putExtra("e_time",e_time);
        i.putExtra("color",bgColor);

        setResult(ADD_CODE, i);
        finish();
    }

    //공백 검증
    public boolean isVaild() {
        if (TextUtils.isEmpty(binding.editText.getText().toString())) {
            binding.editText.setError("과목명을 입력하세요.");
            return false;
        }
        if (binding.spinner.equals("요일을 선택하세요.")) {
            U.getInstance().toast(this, "요일을 선택해주세요.");
            return false;
        }
        if (binding.spinner1.equals("시작 교시")) {
            U.getInstance().toast(this, "시작 교시를 선택해주세요.");
            return false;
        }
        if (binding.spinner2.equals("시작교시를 선택하세요.")) {
            U.getInstance().toast(this, "시작 교시를 선택해주세요.");
            return false;
        }
        if (binding.spinner2.equals("끝 교시")) {
            U.getInstance().toast(this, "끝 교시를 선택해주세요.");
            return false;
        }
        if (bgColor.equals("")) {
            U.getInstance().toast(this, "배경색을 선택해주세요.");
            return false;
        }

        return true;
    }

    //색상선택기 색상 리스트
    public void onSelectColor(View view) {
        final ColorPicker colorPicker = new ColorPicker(AddSubjectActivity.this);
        final ArrayList<String> colors = new ArrayList<>();
        colors.add("#DA5F7E");
        colors.add("#F5905D");
        colors.add("#6CBE90");
        colors.add("#94D355");
        colors.add("#EB5C5C");
        colors.add("#BD927F");
        colors.add("#3A8CBE");
        colors.add("#52BBBE");
        colors.add("#A4A0C4");
        colors.add("#AFB0A6");

        colors.add("#FFD9FA");
        colors.add("#CEFBC9");
        colors.add("#FAE0D4");
        colors.add("#BFA0ED");
        colors.add("#E5D85C");

        colorPicker.setTitle("색상을 선택하세요.")
                .setColors(colors)
                .setDefaultColorButton(Color.parseColor("#f84c44"))
                .setColumns(5)
                .setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
                    @Override
                    public void setOnFastChooseColorListener(int position, int color) {

                        bgColor = colors.get(position);
                        U.getInstance().log("parse_color  " + bgColor);
                        binding.colorView.setBackgroundColor(Color.parseColor(colors.get(position)));
                    }

                    @Override
                    public void onCancel() {

                    }
                }).setRoundColorButton(true).show();
    }

}
