package com.mustdo.cambook.Ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.databinding.ActivityTimeTableBinding;

public class TimeTableActivity extends Activity {
    ActivityTimeTableBinding binding;
    private static int ADD_CODE=1000;
    private static int DELETE_CODE=1000;
    private static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_time_table);

        //뒤로가기
        binding.back.setOnClickListener(view -> finish());

        //시간표추가
        binding.add.setOnClickListener(view -> {
            startActivityForResult(new Intent(context, AddSubjectActivity.class),ADD_CODE);
        });

        //시간표 삭제
        binding.delete.setOnClickListener(view -> {
            startActivityForResult(new Intent(context, DeleteSubjectActivity.class),DELETE_CODE);
        });



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //과목 추가됨
        if(requestCode == ADD_CODE){
            settingTimetable();
        }

        //과목 삭제됨
        else if(requestCode == DELETE_CODE){
            settingTimetable();
        }

    }

    //firestore에서 시간표데이터 불러와서 화면에 셋팅
    public void settingTimetable(){

    }
}
