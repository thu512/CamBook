package com.mustdo.cambook.Ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mustdo.cambook.List.PickPhotoView;
import com.mustdo.cambook.Model.DownloadUrl;
import com.mustdo.cambook.Model.Subject;
import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.Util.U;
import com.mustdo.cambook.databinding.ActivityMainBinding;
import com.sandrios.sandriosCamera.internal.SandriosCamera;
import com.sandrios.sandriosCamera.internal.configuration.CameraConfiguration;
import com.werb.pickphotoview.util.PickConfig;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private ActivityMainBinding binding;
    private static final int CAPTURE_MEDIA = 368;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseStorage firebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        //앱설치후 처음 실행 시 무조건 시간표 액티비티로 이동 -> 과목 리스트, 디렉터리, 사진동기화 작업 필요.
        if (!U.getInstance().getBoolean(this, "first")) {
            U.getInstance().setBoolean(this, "first", true);
            startActivity(new Intent(getApplicationContext(), TimeTableActivity.class));
        }

        //사진촬영으로 바로 이동
        if (U.getInstance().getBoolean(this, "startphoto")) {
            launchCamera();
        }
        //계정설정
        binding.btn5.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), AccountActivity.class)));

        //사진촬영
        binding.btn1.setOnClickListener(view -> {
            launchCamera();
        });

        //앨범
        binding.btn2.setOnClickListener(view -> {


            new PickPhotoView.Builder(MainActivity.this)
                    .setPickPhotoSize(6)
                    .setShowCamera(false)
                    .setSpanCount(4)
                    .setLightStatusBar(false)
                    .setStatusBarColor("#305E98")
                    .setToolbarColor("#5185C7")
                    .setToolbarIconColor("#ffffff")
                    .setClickSelectable(false)
                    .start();

        });

        //시간표
        binding.btn3.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), TimeTableActivity.class)));

        //설정
        binding.btn4.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), SettingActivity.class)));
    }


    // showImagePicker is boolean value: Default is true
    private void launchCamera() {
        new SandriosCamera(this, CAPTURE_MEDIA)
                .setShowPicker(false)
                .setMediaAction(CameraConfiguration.MEDIA_ACTION_PHOTO)
                .enableImageCropping(true) // Default is false.
                .launchCamera();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //카메라
        if (requestCode == CAPTURE_MEDIA && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH);
            String[] path = filePath.split("/");
            String fileName = path[path.length - 1];
            String date = fileName.split("_")[1] + fileName.split("_")[2] + fileName.split("_")[3];
            String time = fileName.split("_")[4];


            U.getInstance().log("사진 경로: " + data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH) + " 날짜: " + date);
            U.getInstance().toast(this, "Media captured.");


            //사진을 저장할 과목명 디렉토리 설정
            String day = null;
            try {
                day = U.getInstance().getDateDay(date);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //과목명 유추 및 사진 해당 과목 디렉에 저장 및 firebaseStorage업로드
            getSubjectSave(filePath, fileName, day, time);
        }


        //앨범
        if(resultCode == 0){
            return;
        }
        if(data == null){
            return;
        }
        if (requestCode == PickConfig.PICK_PHOTO_DATA) {
            ArrayList<String> selectPaths = (ArrayList<String>) data.getSerializableExtra(PickConfig.INTENT_IMG_LIST_SELECT);
            // do something u want
        }
    }

    //해당요일 시간에 포함되는 과목명 리턴 / 없으면 기타...ls

    public void getSubjectSave(String filePath, String fileName, String day, String tm) {
        showPd();

        int time = Integer.parseInt(tm) - 8;


        //firestore에서 불러와서 과목 시간들과 바로 비교
        CollectionReference colRef = db.collection("timeTables").document(user.getUid()).collection("subjects");

        colRef.get().addOnSuccessListener(documentSnapshots -> {

            String result = "기타";

            for (DocumentSnapshot doc : documentSnapshots.getDocuments()) {
                Subject sub = doc.toObject(Subject.class);
                U.getInstance().log("" + sub.toString());
                boolean f = false;
                if (day.equals(sub.getItem())) {
                    int s_time = Integer.parseInt(sub.getS_time());
                    int e_time = Integer.parseInt(sub.getE_time());
                    for (int i = s_time; i <= e_time; i++) {
                        if (time == i) {
                            U.getInstance().log("" + sub.getSubject());
                            result = sub.getSubject();
                            f = true;
                            break;
                        }
                    }
                    if (f) break;
                }
            }


            // 과목별 사진 저장소 path
            File mediaStorageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_DCIM), result);

            //사진을 해당 과목 으로 이동
            U.getInstance().moveFile(filePath, mediaStorageDir.getPath(), fileName);

            //firestorage에 저장
            uploadStorage( mediaStorageDir.getPath() + "/" + fileName, fileName, result);

            stopPd();
        }).addOnFailureListener(e -> {
            U.getInstance().toast(MainActivity.this, "" + e.getMessage());
            stopPd();
        });
    }

    //firestorage에 저장
    public void uploadStorage(String filePath, String fileName, String subject) {
        StorageReference storageRef = firebaseStorage.getReferenceFromUrl("gs://cambook-31402.appspot.com/");


        Uri file = Uri.fromFile(new File(filePath));


        StorageReference userSubjectRef = storageRef.child(user.getUid()).child(subject).child(fileName);
        UploadTask uploadTask = userSubjectRef.putFile(file);

        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            U.getInstance().log("Upload is " + progress + "% done");
        }).addOnPausedListener(taskSnapshot -> {
            U.getInstance().log("Upload is paused");
        }).addOnFailureListener(e -> {
            U.getInstance().log("" + e.getMessage());
        }).addOnSuccessListener(taskSnapshot -> {

            //여기서firestore에 다운로드 url저장
            insertFirestore(subject,taskSnapshot.getDownloadUrl().toString(),fileName);

            U.getInstance().toast(getApplicationContext(), "업로드 성공");
        });

    }

    //firestore 입력
    public void insertFirestore(String subject, String url, String fileName) {
        DownloadUrl d = new DownloadUrl(url,fileName);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("photos").document(user.getUid()).collection(subject)
                .add(d)
                .addOnSuccessListener(aVoid -> {

                    U.getInstance().toast(getApplicationContext(), "사진이 저장되었습니다.");

                })
                .addOnFailureListener(e -> {

                    U.getInstance().toast(getApplicationContext(), "" + e.getMessage());
                });
    }

    //==============================================================================================
    //백키에 대응하는 메소드
    @Override
    public void onBackPressed() {
        //아래코드를 막으면 현재 화면의 종료처리가 중단됨
        //super.onBackPressed();
        if (!isFirstEnd) {
            //최초한번 백키를 눌렀다.
            isFirstEnd = true;
            //3초후에 초기화된다.(최초로 한번 백키를 눌렀던 상황이)
            handler.sendEmptyMessageDelayed(1, 3000);
            U.getInstance().toast(this, "뒤로가기를 한번 더 누르시면 종료됩니다.");
        } else {
            super.onBackPressed();
        }
    }

    boolean isFirstEnd; //백키를 한번 눌렀나?

    //핸들러, 메세지를 던져서 큐에 담고 하나씩 꺼내서 처리하는 메시징 시스템
    Handler handler = new Handler() {
        //이 메소드는 큐에 메세지가 존재하면 뽑아서 호출된다.
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) { //최초로 백키를 한번 눌렀다.

            } else if (msg.what == 1) { //3초가 지났다. 다시 초기화.
                isFirstEnd = false;
            }
        }
    };
}
