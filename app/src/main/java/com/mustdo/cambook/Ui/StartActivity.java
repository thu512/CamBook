package com.mustdo.cambook.Ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mustdo.cambook.Model.DownloadUrl;
import com.mustdo.cambook.Model.Subject;
import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.Util.U;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class StartActivity extends Activity {
    FirebaseAuth user;
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수
    private String[] permissions = {android.Manifest.permission.CAMERA, android.Manifest.permission.READ_PHONE_STATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        Boolean f1 = false;  //바꿔주기
        Boolean f2 = false;

        if (execCmd() || checkFile()) {

            U.getInstance().showPopup3(this,
                    "알림",
                    "비정상적인 접근입니다.",
                    "확인",
                    new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            StartActivity.this.finishAffinity();
                        }
                    },
                    null,
                    null
            );
        } else {
            f1 = true;
        }


        //네트워크 연결체크
        ConnectivityManager manager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


        // wifi 또는 모바일 네트워크 어느 하나라도 연결이 되어있다면,
        if (wifi.isConnected() || mobile.isConnected()) {
            //("연결됨" , "연결이 되었습니다.);
            f2 = true;
            U.getInstance().log("네트워크 연결됨");
        } else {
            U.getInstance().showPopup3(this,
                    "알림",
                    "네트워크연결을 확인해주세요.",
                    "확인",
                    new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            StartActivity.this.finishAffinity();
                        }
                    },
                    null,
                    null
            );

        }


        if (f1 && f2) {
            user = FirebaseAuth.getInstance();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!U.getInstance().getBoolean(StartActivity.this, "first")) {
                        U.getInstance().setBoolean(StartActivity.this, "autosave", true);
                        U.getInstance().setBoolean(StartActivity.this, "startphoto", false);
                        U.getInstance().setBoolean(StartActivity.this, "sound", false);
                    }
                    //권한 요청 메소드
                    checkPermissions();


                }
            }, 3000);
        }


    }

    public void startApp() {
        if (user.getCurrentUser() != null) {
            if (user.getCurrentUser().isAnonymous()) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));

                //여기서 다른앱에서 공유받은 인텐트를 통해 사진 저장시킴
                receiveIntent(getIntent());
            } else if (user.getCurrentUser().isEmailVerified()) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                //여기서 다른앱에서 공유받은 인텐트를 통해 사진 저장시킴
                receiveIntent(getIntent());


            } else {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        } else {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
        finish();
    }

    private void checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();        //String형의 permissionList만듬
        for (String pm : permissions) {                            //3개 권한 가지는 permissions배열 foreach문으로 넣음
            result = ContextCompat.checkSelfPermission(this, pm);

            if (result != PackageManager.PERMISSION_GRANTED) { //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) { //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청한다.
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return;
        }
        startApp();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean f1 = true;
        boolean f2 = true;
        boolean f3 = true;
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                popup();
                                f1 = false;
                            } else {
                                f1 = true;
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                popup();
                                f2 = false;
                            } else {
                                f2 = true;
                            }
                        }
                    }
                } else {
                    popup();
                }

                if (f1 && f2) {
                    startApp();
                }
                return;
            }
        }
    }


    public void popup() {
        U.getInstance().showPopup3(this,
                "알림",
                "권한사용을 동의해주셔야 이용이 가능합니다.",
                "확인",
                sweetAlertDialog -> finish(),
                null,
                null
        );
    }


    public boolean execCmd() {
        boolean flag = false;
        try {
            Runtime.getRuntime().exec("su");
            flag = true;
        } catch (Exception e) {
            flag = false;
        }

        return flag;
    }

    public boolean checkFile() {
        String[] arrayOfString = {"/system/bin/.ext", "/system/xbin/.ext", "/sbin/su", "/data/data/com.noshufou.android.su",
                "/system/app/SuperUser.apk", "/system/xbin/su", "/system/bin/su"};
        int i = 0;
        while (true) {
            if (i >= arrayOfString.length)
                return false;
            if (new File(arrayOfString[i]).exists())
                return true;

            i++;
        }
    }


    //여기서 다른앱에서 공유받은 인텐트를 통해 사진 저장시킴
    public void receiveIntent(Intent intent) {

        if (intent != null) {
            String action = intent.getAction();
            String mimeType = intent.getType();
            if (action != null && (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) && mimeType != null) {
                U.getInstance().log("" + action);
                U.getInstance().log("" + mimeType);
                Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);


                InputStream inputStream = null;
                OutputStream out = null;

                try {

                    Uri u = U.getInstance().convertContentToFileUri(this, uri);
                    U.getInstance().log("uri: " + u.toString());
                    U.getInstance().log("uri: " + u.getPath());

                    //입력 스트림 오픈
                    inputStream = getContentResolver().openInputStream(u);


                    ExifInterface exif = new ExifInterface(u.getPath());

                    U.getInstance().log("" + exif.getAttribute(ExifInterface.TAG_DATETIME));

                    //사진파일의 exif정보가 있을때
                    if (exif.getAttribute(ExifInterface.TAG_DATETIME) != null) {
                        String date = exif.getAttribute(ExifInterface.TAG_DATETIME);


                        SimpleDateFormat old = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                        SimpleDateFormat new_date = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                        Date d = old.parse(date);

                        String date_new = new_date.format(d);
                        U.getInstance().log("date: " + date_new);

                        String day = U.getInstance().getDateDay(
                                date.split(" ")[0].split(":")[0]
                                        + date.split(" ")[0].split(":")[1]
                                        + date.split(" ")[0].split(":")[2]);

                        U.getInstance().log("day: " + day);


                        int time = Integer.parseInt(date.split(" ")[1].split(":")[0]) - 8;
                        U.getInstance().log("시간: " + time);

                        getSubject(day, time, date_new, u);

                    }
                    //사진파일의 exif정보가 없을때
                    else {
                        saveExternalImg(u);
                    }


                } catch (Exception e) {
                    U.getInstance().log("1" + e.getLocalizedMessage());
                    U.getInstance().toast(this, "사진 가져오기에 실패하였습니다.");
                    FirebaseCrash.log("사진 가져오기 에러: " + e.getLocalizedMessage());
                    FirebaseCrash.log("사진 가져오기 에러: " + e.getMessage());

                    e.printStackTrace();
                } finally {
                    try {
                        if (inputStream != null) inputStream.close();
                        if (out != null) out.close();
                    } catch (IOException e) {
                        U.getInstance().log("2" + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //사진 가져와서 저장
    public void saveExternalImg(Uri u) {
        try {
            String[] fileArray = u.getPath().split("/");

            String filename = fileArray[fileArray.length - 1];

            U.getInstance().log("uri: " + filename);

            String date = filename.split("_")[1];
            date += filename.split("_")[2].substring(0, 6);
            String tm = filename.split("_")[2].substring(0, 2);
            SimpleDateFormat old = new SimpleDateFormat("yyyyMMddHHmmss");
            SimpleDateFormat new_date = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date d = old.parse(date);

            String date_new = new_date.format(d);

            U.getInstance().log("date: " + date);

            String day = U.getInstance().getDateDay(filename.split("_")[1]);
            U.getInstance().log("day: " + day);


            int time = Integer.parseInt(tm) - 8;
            U.getInstance().log("시간: " + time);

            getSubject(day, time, date_new, u);

        } catch (Exception e) {
            U.getInstance().log("saveExternalImg" + e.getLocalizedMessage());
            U.getInstance().toast(this, "사진 가져오기에 실패하였습니다.");
            FirebaseCrash.log("사진 가져오기 에러(saveExternalImg): " + e.getLocalizedMessage());
            FirebaseCrash.log("사진 가져오기 에러(saveExternalImg): " + e.getMessage());

            e.printStackTrace();
        }

    }

    //과목명 추출
    public void getSubject(String day, int time, String date_new, Uri u) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

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

            //사진 해당 과목에 저장 기타 폴더에 저장 후 -> 사진 정보 추출 과목 구한 후 -> 해당 과목으로 이동

            U.getInstance().moveFile(u.getPath(), mediaStorageDir.getPath(), "IMG_" + date_new + ".jpg");

            //firestorage에 저장
            uploadStorage(mediaStorageDir.getPath() + "/" + "IMG_" + date_new + ".jpg", "IMG_" + date_new + ".jpg", result);


        }).addOnFailureListener(e -> {
            U.getInstance().toast(StartActivity.this, "" + e.getMessage());

        });
    }

    //firestorage에 저장
    public void uploadStorage(String filePath, String fileName, String subject) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

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
            U.getInstance().log("firestorage 업로드 실패 " + e.getMessage());
        }).addOnSuccessListener(taskSnapshot -> {

            //여기서firestore에 다운로드 url저장
            if (taskSnapshot.getMetadata() != null && taskSnapshot.getMetadata().getReference() != null) {
                insertFirestore(subject, taskSnapshot.getMetadata().getReference().getDownloadUrl().toString(), fileName);
                //Log.d("TTT",""+taskSnapshot.getUploadSessionUri().getPath());
                U.getInstance().toast(getApplicationContext(), "업로드 성공");
            }

        });

    }

    //firestore 입력
    public void insertFirestore(String subject, String url, String fileName) {
        DownloadUrl d = new DownloadUrl(url, fileName);

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

}
