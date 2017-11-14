package com.mustdo.cambook.Ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mustdo.cambook.Model.Subject;
import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.Util.U;
import com.mustdo.cambook.databinding.ActivityTimeTableBinding;

import java.util.ArrayList;

public class TimeTableActivity extends Activity {
    private ActivityTimeTableBinding binding;
    public static int ADD_CODE = 1000;
    public static int DELETE_CODE = 1001;
    private static Context context;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    private TextView s1[] = new TextView[12]; //월
    private TextView s2[] = new TextView[12]; //화
    private TextView s3[] = new TextView[12]; //수
    private TextView s4[] = new TextView[12]; //목
    private TextView s5[] = new TextView[12]; //금


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_time_table);

        initView();

        //뒤로가기
        binding.back.setOnClickListener(view -> finish());

        //시간표추가
        binding.add.setOnClickListener(view -> {
            startActivityForResult(new Intent(context, AddSubjectActivity.class), ADD_CODE);
        });

        //시간표 삭제
        binding.delete.setOnClickListener(view -> {
            startActivityForResult(new Intent(context, DeleteSubjectActivity.class), DELETE_CODE);
        });


    }

    //화면 갱신시마다 시간표 불러와서 셋팅
    @Override
    protected void onResume() {
        super.onResume();
        settingTimetable();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //과목 추가됨
        if (requestCode == ADD_CODE) {
            //과목 중복 체크 통과시 입력후 셋팅
            if(data!=null){
                String item = data.getStringExtra("item");
                String subject = data.getStringExtra("subject");
                String s_time = data.getStringExtra("s_time");
                String e_time = data.getStringExtra("e_time");
                String color = data.getStringExtra("color");

                //중복확인
                if (!checkDuplication(item, s_time, e_time)) {
                    U.getInstance().toast(this, "중복된 시간표가 존재 합니다.");
                    return;
                }

                //과목명 리스트 생성(중복없이) -> 디렉 생성 및 과목삭제 리스트 생성용
                makeList(subject);

                //firestore 삽입
                insertFirestore(item, s_time, e_time, subject, color);
            }
        }

        //과목 삭제됨
        if (requestCode == DELETE_CODE) {
            if(data!=null){
                U.getInstance().log("새로고침");
                Intent intent = new Intent(TimeTableActivity.this, TimeTableActivity.class);
                finish();
                startActivity(intent);
            }
        }

    }


    //firestore 입력
    public void insertFirestore(String item, String s_time, String e_time, String subject, String color) {
        showPd();

        Subject sub = new Subject(subject, item, s_time, e_time, color);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("timeTables").document(user.getUid()).collection("subjects")
                .add(sub)
                .addOnSuccessListener(aVoid -> {
                            stopPd();
                            U.getInstance().toast(getApplicationContext(), "시간표가 저장되었습니다.");
                            settingTimetable();
                        }
                )
                .addOnFailureListener(e -> {
                            stopPd();
                            U.getInstance().toast(getApplicationContext(), "" + e.getMessage());
                        }
                );
    }

    //firestore에서 시간표데이터 불러오기 및 셋팅
    public void settingTimetable() {
        showPd();


        CollectionReference colRef = db.collection("timeTables").document(user.getUid()).collection("subjects");

        colRef.get().addOnSuccessListener(documentSnapshots -> {
                    for (DocumentSnapshot doc : documentSnapshots.getDocuments()) {
                        Subject sub = doc.toObject(Subject.class);
                        U.getInstance().log("" + sub.toString());

                        //최초 실행인경우 기존 시간표 불러와서 리스트 생성
                        if (!U.getInstance().getBoolean(this, "setList")) {
                            makeList(sub.getSubject());
                        }
                        //firestore에서 꺼내와서 화면에 셋팅
                        drawTable(sub.getItem(), sub.getS_time(), sub.getE_time(), sub.getSubject(), sub.getColor());
                    }


                    if (!U.getInstance().getBoolean(this, "setList")) {

                        U.getInstance().setBoolean(this, "setList", true);
                    }
                    U.getInstance().log("" + U.getInstance().loadSharedPreferencesData(this, "subject").toString());
                    stopPd();
                }
        )
                .addOnFailureListener(e -> {
                    U.getInstance().toast(TimeTableActivity.this, "" + e.getMessage());
                    stopPd();
                });
    }


    //시간표 그리기
    public void drawTable(String item, String s_time, String e_time, String subject, String color) {
        int start = Integer.parseInt(s_time);
        int end = Integer.parseInt(e_time);

        switch (item) {
            case "월":

                for (int j = start; j <= end; j++) {
                    s1[j].setText(subject);
                    s1[j].setBackgroundColor(Color.parseColor(color));
                }
                break;
            case "화":
                for (int j = start; j <= end; j++) {
                    s2[j].setText(subject);
                    s2[j].setBackgroundColor(Color.parseColor(color));
                }
                break;
            case "수":
                for (int j = start; j <= end; j++) {
                    s3[j].setText(subject);
                    s3[j].setBackgroundColor(Color.parseColor(color));
                }
                break;
            case "목":
                for (int j = start; j <= end; j++) {
                    s4[j].setText(subject);
                    s4[j].setBackgroundColor(Color.parseColor(color));
                }
                break;
            case "금":
                for (int j = start; j <= end; j++) {
                    s5[j].setText(subject);
                    s5[j].setBackgroundColor(Color.parseColor(color));
                }
                break;
            default:
                break;
        }
    }

    //과목명 리스트 생성(중복없이)
    public void makeList(String subject) {
        ArrayList<String> subs = U.getInstance().loadSharedPreferencesData(this, "subject");
        for (String sub : subs) {
            if (sub.equals(subject)) {
                return;
            }
        }
        subs.add(subject);
        U.getInstance().saveSharedPreferences_Data(this, "subject", subs);
    }

    //중복확인
    public boolean checkDuplication(String week, String stime, String etime) {
        int start;
        int end;
        switch (week) {
            case "월":
                start = Integer.parseInt(stime);
                end = Integer.parseInt(etime);
                for (int i = start; i <= end; i++) {
                    if (!s1[i].getText().toString().equals("")) {
                        return false;
                    }
                }
                return true;
            case "화":
                start = Integer.parseInt(stime);
                end = Integer.parseInt(etime);
                for (int i = start; i <= end; i++) {
                    if (!s2[i].getText().toString().equals("")) {
                        return false;
                    }
                }
                return true;

            case "수":
                start = Integer.parseInt(stime);
                end = Integer.parseInt(etime);
                for (int i = start; i <= end; i++) {
                    if (!s3[i].getText().toString().equals("")) {
                        return false;
                    }
                }
                return true;

            case "목":
                start = Integer.parseInt(stime);
                end = Integer.parseInt(etime);
                for (int i = start; i <= end; i++) {
                    if (!s4[i].getText().toString().equals("")) {
                        return false;
                    }
                }
                return true;

            case "금":
                start = Integer.parseInt(stime);
                end = Integer.parseInt(etime);
                for (int i = start; i <= end; i++) {
                    if (!s5[i].getText().toString().equals("")) {
                        return false;
                    }
                }
                return true;

            default:
                return true;


        }
    }


    //==============================================================================================
    public void initView() {
        s1[1] = binding.textview11;
        s1[2] = binding.textview12;
        s1[3] = binding.textview13;
        s1[4] = binding.textview14;
        s1[5] = binding.textview15;
        s1[6] = binding.textview16;
        s1[7] = binding.textview17;
        s1[8] = binding.textview18;
        s1[9] = binding.textview19;
        s1[10] = binding.textview110;
        s1[11] = binding.textview111;

        s2[1] = binding.textview21;
        s2[2] = binding.textview22;
        s2[3] = binding.textview23;
        s2[4] = binding.textview24;
        s2[5] = binding.textview25;
        s2[6] = binding.textview26;
        s2[7] = binding.textview27;
        s2[8] = binding.textview28;
        s2[9] = binding.textview29;
        s2[10] = binding.textview210;
        s2[11] = binding.textview211;

        s3[1] = binding.textview31;
        s3[2] = binding.textview32;
        s3[3] = binding.textview33;
        s3[4] = binding.textview34;
        s3[5] = binding.textview35;
        s3[6] = binding.textview36;
        s3[7] = binding.textview37;
        s3[8] = binding.textview38;
        s3[9] = binding.textview39;
        s3[10] = binding.textview310;
        s3[11] = binding.textview311;

        s4[1] = binding.textview41;
        s4[2] = binding.textview42;
        s4[3] = binding.textview43;
        s4[4] = binding.textview44;
        s4[5] = binding.textview45;
        s4[6] = binding.textview46;
        s4[7] = binding.textview47;
        s4[8] = binding.textview48;
        s4[9] = binding.textview49;
        s4[10] = binding.textview410;
        s4[11] = binding.textview411;

        s5[1] = binding.textview51;
        s5[2] = binding.textview52;
        s5[3] = binding.textview53;
        s5[4] = binding.textview54;
        s5[5] = binding.textview55;
        s5[6] = binding.textview56;
        s5[7] = binding.textview57;
        s5[8] = binding.textview58;
        s5[9] = binding.textview59;
        s5[10] = binding.textview510;
        s5[11] = binding.textview511;


    }
}
