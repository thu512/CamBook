package com.mustdo.cambook.Ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mustdo.cambook.Model.Subject;
import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.Util.U;
import com.mustdo.cambook.databinding.ActivityDeleteSubjectBinding;

import java.io.File;
import java.util.ArrayList;

import static com.mustdo.cambook.Ui.TimeTableActivity.DELETE_CODE;


public class DeleteSubjectActivity extends Activity {
    private ActivityDeleteSubjectBinding binding;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> subject;
    private ArrayList<String> deleteList;
    private String select;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_delete_subject);

        //뒤로가기
        binding.back.setOnClickListener((view -> finish()));


        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        //과목리스트 받아오기
        subject = U.getInstance().loadSharedPreferencesData(this, "subject");

        //지워야될 Firestore 문서id를 담을 배열
        deleteList = new ArrayList<>();



        adapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        adapter.add("과목 선택");
        for (int i = 0; i < subject.size(); i++) {
            adapter.add(subject.get(i));
        }
        binding.spinner.setAdapter(adapter);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                select = parent.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        //완료 버튼 클릭
        binding.button.setOnClickListener(view -> {
                    if (select.equals("과목 선택")) {
                        U.getInstance().toast(DeleteSubjectActivity.this, "삭제할 과목을 선택해주세요.");
                        return;
                    }
                    //firestore 에서 과목 삭제 / sp에서 과목 삭제
                    deleteSp(select);
                    deleteFirestore(select);
                    rmDir(select);
                }
        );
    }


    //1. 지워야될 문서 목록 가져옴 -> 문서들 지움 -> 시간표 화면으로 복귀
    public void deleteFirestore(String subject) {
        showPd();
        CollectionReference colRef = db.collection("timeTables").document(user.getUid()).collection("subjects");

        colRef.get().addOnSuccessListener(documentSnapshots -> {
                    for (DocumentSnapshot doc : documentSnapshots.getDocuments()) {
                        Subject sub = doc.toObject(Subject.class);
                        U.getInstance().log("" + sub.toString());

                        if (sub.getSubject().equals(subject)) {
                            deleteList.add(doc.getId());
                        }
                    }

                    //삭제 시작
                    for (String id : deleteList) {
                        removeData(id);
                    }
                    stopPd();
                    Intent i = new Intent(getApplicationContext(), TimeTableActivity.class);
                    i.putExtra("ok", 1);
                    setResult(DELETE_CODE, i);
                    finish();

                }
        )
                .addOnFailureListener(e -> {
                    U.getInstance().toast(DeleteSubjectActivity.this, "" + e.getMessage());
                    stopPd();
                });
    }


    //실제 FireStore데이터 삭제
    public void removeData(String uid) {
        db.collection("timeTables")
                .document(user.getUid())
                .collection("subjects")
                .document(uid)
                .delete().addOnSuccessListener(aVoid -> {
            //삭제 성공
        }).addOnFailureListener(e -> {
            //삭제 실패
            U.getInstance().toast(getApplicationContext(), "" + e.getMessage());
        });
    }


    //sp에서 과목 삭제
    public void deleteSp(String subject) {
        ArrayList<String> subs = U.getInstance().loadSharedPreferencesData(this, "subject");
        for (int i = 0; i < subs.size(); i++) {
            if (subs.get(i).equals(subject)) {
                subs.remove(i);
                break;
            }
        }
        U.getInstance().saveSharedPreferences_Data(this, "subject", subs);
    }

    //디렉제거
    public void rmDir(String sub){
        //외부 저장소에 이 앱을 통해 촬영된 사진만 저장할 directory 경로와 File을 연결

        File mediaStorageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_DCIM),sub);

        File[] childFileList = mediaStorageDir.listFiles();

        for(File childFile : childFileList)
        {
            childFile.delete();    //하위 파일
        }
        mediaStorageDir.delete();    //root 삭제

    }
}
