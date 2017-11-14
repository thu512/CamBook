package com.mustdo.cambook.Ui;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.Util.U;
import com.mustdo.cambook.databinding.ActivityAddSubjectBinding;

import java.util.ArrayList;

import petrov.kristiyan.colorpicker.ColorPicker;

public class AddSubjectActivity extends Activity {

    private ActivityAddSubjectBinding binding;
    private String bgColor="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_subject);

        binding.back.setOnClickListener((view -> finish()));


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


        colorPicker.setTitle("색상을 선택하세요.")
                .setColors(colors)
                .setDefaultColorButton(Color.parseColor("#f84c44"))
                .setColumns(5)
                .setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
                    @Override
                    public void setOnFastChooseColorListener(int position, int color) {

                        bgColor = colors.get(position);
                        U.getInstance().log("parse_color  " +bgColor);
                        binding.colorView.setBackgroundColor(Color.parseColor(colors.get(position)));
                    }

                    @Override
                    public void onCancel() {

                    }
                }).setRoundColorButton(true).show();
    }

}
