package com.mustdo.cambook.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.mustdo.cambook.R;
import com.mustdo.cambook.Util.U;

import java.util.ArrayList;

/**
 * Created by Changjoo on 2017-08-18.
 */

public class ImgMoveDialog extends Dialog {


    private Button btn_left; //탈퇴
    private Button btn_right; //건의하기


    private View.OnClickListener mLeftClickListener;
    private View.OnClickListener mRightClickListener;
    private ArrayList<String> subject;
    private ArrayAdapter<String> adapter;
    private Spinner spinnerSub;



    private  String select="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 다이얼로그 외부 화면 흐리게 표현
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.5f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_img_move);
        spinnerSub = (Spinner)findViewById(R.id.spinnerSub);
        btn_left = (Button) findViewById(R.id.btn_left);
        btn_right = (Button) findViewById(R.id.btn_right);

        //과목리스트 받아오기
        subject = U.getInstance().loadSharedPreferencesData(getContext(), "subject");
        adapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        for (int i = 0; i < subject.size(); i++) {
            adapter.add(subject.get(i));
        }
        spinnerSub.setAdapter(adapter);
        spinnerSub.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                select = parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        // 클릭 이벤트 셋팅
        if (mLeftClickListener != null && mRightClickListener != null) {
            btn_left.setOnClickListener(mLeftClickListener);
            btn_right.setOnClickListener(mRightClickListener);
        }

    }

    // 클릭버튼이 확인과 취소 두개일때 생성자 함수로 이벤트를 받는다
    public ImgMoveDialog(Context context,
                         View.OnClickListener leftListener,
                         View.OnClickListener rightListener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mLeftClickListener = leftListener;
        this.mRightClickListener = rightListener;
    }


    public String getSelect() {
        return select;
    }

    public void setSelect(String select) {
        this.select = select;
    }
}
