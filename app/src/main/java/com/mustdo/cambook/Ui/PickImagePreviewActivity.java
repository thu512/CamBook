package com.mustdo.cambook.Ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mustdo.cambook.Dialog.ImgMoveDialog;
import com.mustdo.cambook.Model.DownloadUrl;
import com.mustdo.cambook.R;
import com.mustdo.cambook.Util.U;
import com.shizhefei.view.largeimage.LargeImageView;
import com.shizhefei.view.largeimage.factory.FileBitmapDecoderFactory;
import com.werb.pickphotoview.PickPhotoPreviewActivity;
import com.werb.pickphotoview.model.PickData;
import com.werb.pickphotoview.model.PickHolder;
import com.werb.pickphotoview.util.PickConfig;
import com.werb.pickphotoview.widget.MyToolbar;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by jieun on 2017-09-19.
 */

public class PickImagePreviewActivity extends PickPhotoPreviewActivity {

    private ArrayList<String> allImagePath;
    private ArrayList<String> selectImagePath;
    private String path;
    private String[] name;
    private String imgDate;
    private String imgTime;
    private ViewPager viewPager;
    private ArrayList<LargeImageView> imageViews;
    private MyToolbar myToolbar;
    private boolean mIsHidden, misSelect;
    private PickData pickData;

    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private FirebaseUser user;

    private ArrayList<String> subjects;
    private ImgMoveDialog imgMoveDialog;

    View popupView;
    PopupWindow popupWindow;
    Spinner spinner;
    ArrayAdapter<String> adapter;

    String select;
    PopupMenu popup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(com.werb.pickphotoview.R.layout.pick_activty_preview_photo);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();
        db=FirebaseFirestore.getInstance();

        pickData = (PickData) getIntent().getSerializableExtra(PickConfig.INTENT_PICK_DATA);
        path = getIntent().getStringExtra(PickConfig.INTENT_IMG_PATH);
        name = getIntent().getStringExtra(PickConfig.INTENT_DIR_NAME).split("_");   //IMG_2017_09_19_20_39_42.jpg
        imgDate = name[1] + "-" + name[2] + "-" + name[3] + " ";
        imgTime = name[4] + ":" + name[5];


        allImagePath = (ArrayList<String>) getIntent().getSerializableExtra(PickConfig.INTENT_IMG_LIST);
        imageViews = new ArrayList<>();
        selectImagePath = PickHolder.getStringPaths();
        for (int i = 0; i < 4; i++) {
            LargeImageView imageView = new LargeImageView(this);
            imageView.setEnabled(true);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideOrShowToolbar();
                }
            });
            imageViews.add(imageView);
        }
        initView();
    }

    private void initView() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(pickData.getStatusBarColor());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (pickData.isLightStatusBar()) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        myToolbar = (MyToolbar) findViewById(com.werb.pickphotoview.R.id.toolbar);

        myToolbar.setPhotoDirName(imgDate + imgTime);
        myToolbar.setBackgroundColor(pickData.getToolbarColor());
        myToolbar.setIconColor(pickData.getToolbarIconColor());
        myToolbar.setSelectColor(pickData.getSelectIconColor());
        myToolbar.setLeftIcon(com.werb.pickphotoview.R.mipmap.pick_ic_back);
        myToolbar.setLeftLayoutOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishForResult();
            }
        });


        myToolbar.setRightIcon(com.mustdo.cambook.R.mipmap.ic_menu);
        myToolbar.setRightLayoutOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup = new PopupMenu(PickImagePreviewActivity.this, view);
                MenuInflater inflater = popup.getMenuInflater();
                Menu menu = popup.getMenu();
                inflater.inflate(R.menu.menu_imagepreview, menu);

                popup.setOnMenuItemClickListener(menuItem -> {
                    switch (menuItem.getItemId()) {

                        case R.id.menu_delete:
                            U.getInstance().showPopup3(PickImagePreviewActivity.this,
                                    "경고", "삭제한 데이터는 다시 복구할 수 없습니다.",
                                    "확인", (sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        imageDelete();//디렉 사진 삭제, firebase 파일 삭제
                                    }),
                                    "취소", sweetAlertDialog -> sweetAlertDialog.dismissWithAnimation());
                            return true;

                        case R.id.menu_move:
                            imgMoveDialog = new ImgMoveDialog(PickImagePreviewActivity.this,
                                    view1 -> {
                                    },
                                    view1 -> {

                                        imageMove(imgMoveDialog.getSelect());
                                        imgMoveDialog.dismiss();

                                    });
                            imgMoveDialog.show();
                            return true;
                    }
                    return true;
                });
                popup.show();
            }

        });


        viewPager = (ViewPager) findViewById(com.werb.pickphotoview.R.id.image_vp);
        int indexOf = allImagePath.indexOf(path);
        //judgeSelect(allImagePath.get(indexOf));
        viewPager.setAdapter(new listPageAdapter());
        viewPager.setCurrentItem(indexOf);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                path = allImagePath.get(position);
                changePage(path);
                //judgeSelect(path);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    private class listPageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return allImagePath.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override

        public Object instantiateItem(ViewGroup container, final int position) {
            int i = position % 4;
            final LargeImageView pic = imageViews.get(i);
            ImageView gif = new ImageView(container.getContext());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            String path = allImagePath.get(position);
            if (path.endsWith(".gif")) {
                container.addView(gif, params);
                Glide.with(PickImagePreviewActivity.this).asGif().load(new File(path)).into(gif);
                return gif;
            } else {
                container.addView(pic, params);
                pic.setImage(new FileBitmapDecoderFactory(new File(path)));
                return pic;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            int i = position % 4;
            final LargeImageView imageView = imageViews.get(i);
            container.removeView(imageView);
        }
    }


    @Override
    public void finish() {
        super.finish();
        viewPager = null;
        overridePendingTransition(0, com.werb.pickphotoview.R.anim.pick_finish_slide_out_left);
    }

    //固定 toolbar
    private void hideOrShowToolbar() {
        myToolbar.animate()
                .translationY(mIsHidden ? 0 : -myToolbar.getHeight())
                .setInterpolator(new DecelerateInterpolator(2))
                .start();

        mIsHidden = !mIsHidden;

    }

    private void judgeSelect(final String path) {
        int indexOf = selectImagePath.indexOf(path);
        if (indexOf != -1) {
            myToolbar.setRightIconDefault(com.werb.pickphotoview.R.mipmap.pick_ic_select);
            misSelect = true;
        } else {
            myToolbar.setRightIcon(com.werb.pickphotoview.R.mipmap.pick_ic_un_select_black);
            misSelect = false;
        }

        myToolbar.setRightLayoutOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (misSelect) {
                    myToolbar.setRightIcon(com.werb.pickphotoview.R.mipmap.pick_ic_un_select_black);
                    selectImagePath.remove(path);
                    PickHolder.setStringPaths(selectImagePath);
                    misSelect = false;
                } else {
                    if (selectImagePath.size() < pickData.getPickPhotoSize()) {
                        myToolbar.setRightIconDefault(com.werb.pickphotoview.R.mipmap.pick_ic_select);
                        selectImagePath.add(path);
                        PickHolder.setStringPaths(selectImagePath);
                        misSelect = true;
                    } else {
                        Toast.makeText(PickImagePreviewActivity.this, String.format(v.getContext().getString(com.werb.pickphotoview.R.string.pick_photo_size_limit), String.valueOf(pickData.getPickPhotoSize())), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finishForResult();
    }

    private void finishForResult() {
        Intent intent = new Intent();
        intent.setClass(PickImagePreviewActivity.this, PickPhotoActivity.class);
        intent.putExtra(PickConfig.INTENT_IMG_LIST_SELECT, selectImagePath);
        setResult(PickConfig.PREVIEW_PHOTO_DATA, intent);
        finish();
    }

    private void changePage(String path) {
        String[] nameArr = path.split("/");
        String[] name = nameArr[nameArr.length - 1].split("_");
        imgDate = name[1] + "-" + name[2] + "-" + name[3] + " ";
        imgTime = name[4] + ":" + name[5];

        myToolbar.setPhotoDirName(imgDate + imgTime);
    }


    @Override
    public void openOptionsMenu() {

        Configuration config = getResources().getConfiguration();

        if ((config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                > Configuration.SCREENLAYOUT_SIZE_LARGE) {

            int originalScreenLayout = config.screenLayout;
            config.screenLayout = Configuration.SCREENLAYOUT_SIZE_LARGE;
            super.openOptionsMenu();
            config.screenLayout = originalScreenLayout;

        } else {
            super.openOptionsMenu();
        }
    }


    private void imageDelete() {
        String[] subjects = path.split("/");
        String subname = subjects[subjects.length - 2];

        String filename = getIntent().getStringExtra(PickConfig.INTENT_DIR_NAME);


        File f = new File(path);
        f.delete();//디렉에 있는 해당 파일 삭제
        deletePhotoUrl(user.getUid(), subname, filename);
        //deleteStorage(subname, filename);//storage에 있는 해당 파일 삭제

        Toast.makeText(PickImagePreviewActivity.this, "삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
        finish();
        Intent intent = new Intent(PickImagePreviewActivity.this, PickPhotoActivity.class);

        //intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        //intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);


        startActivity(intent);
    }


    public void deletePhotoUrl(String uid, String subject, String filename) {
        CollectionReference colRef = db.collection("photos")
                .document(user.getUid())
                .collection(subject);
        colRef.get().addOnSuccessListener(documentSnapshots -> {
            for (DocumentSnapshot doc : documentSnapshots.getDocuments()) {

                DownloadUrl du = doc.toObject(DownloadUrl.class);
                if (du.getFileName().equals(filename)) {
                    deleteStorage(du.getUrl());
                    db.collection("photos")
                            .document(user.getUid())
                            .collection(subject)
                            .document(doc.getId())
                            .delete().addOnSuccessListener(aVoid -> {
                        //삭제 성공
                    }).addOnFailureListener(e -> {
                        //삭제 실패
                        U.getInstance().toast(getApplicationContext(), "" + e.getMessage());
                    });
                    break;
                }
            }
        });
    }

    private void deleteStorage(String url) {


        //StorageReference storageRef = firebaseStorage.getReferenceFromUrl("gs://cambook-31402.appspot.com/");
        StorageReference storageRef2 = firebaseStorage.getReferenceFromUrl(url);

        //StorageReference desertRef = storageRef.child(user.getUid()).child(subname).child(filename);

        //Log.d("deleteSubject", subname);
        //Log.d("deleteFilename", filename);

        storageRef2.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(PickImagePreviewActivity.this, "삭제 완료", Toast.LENGTH_SHORT).show();
                Log.d("deleteStorage", "Successed");
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PickImagePreviewActivity.this, "삭제 실패", Toast.LENGTH_SHORT).show();
                Log.d("deleteStorage", "Failed");
            }
        });

    }

    private void imageMove(String subname) {

        /*
            firebase 과목 리스트 불러와서 스피너로 선택
       */
        String subject = subname;     //이동할 과목 앨범
        String filename = getIntent().getStringExtra(PickConfig.INTENT_DIR_NAME); //IMG_2017_11_25_12_35_08.jpg
        String[] subjects = path.split("/");

        String subnm = subjects[subjects.length - 2]; //기존 과목
        Log.d("TTT",subnm+" / "+subject+" / "+filename);
        //firestore에 파일 삭제 후 새로운 path로 다시 저장
        movePhotoUrl(subnm,subject,filename);

        // 과목별 사진 저장소 path
        File mediaStorageDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_DCIM), subject);

        //사진을 해당 과목 으로 이동
        U.getInstance().moveFile(path, mediaStorageDir.getPath(), filename);




        Toast.makeText(PickImagePreviewActivity.this, "사진을 이동하였습니다.", Toast.LENGTH_SHORT).show();
        finish();
        Intent intent = new Intent(PickImagePreviewActivity.this, PickPhotoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);

    }

    public void movePhotoUrl(String subject, String newSubject, String filename) {
        CollectionReference colRef = db.collection("photos")
                .document(user.getUid())
                .collection(subject);
        colRef.get().addOnSuccessListener(documentSnapshots -> {
            for (DocumentSnapshot doc : documentSnapshots.getDocuments()) {

                DownloadUrl du = doc.toObject(DownloadUrl.class);
                if (du.getFileName().equals(filename)) {
                    db.collection("photos")
                            .document(user.getUid())
                            .collection(subject)
                            .document(doc.getId())
                            .delete().addOnSuccessListener(aVoid -> {

                        db.collection("photos").document(user.getUid()).collection(newSubject)
                                .add(du)
                                .addOnSuccessListener(aVoid1 -> {

                                    //U.getInstance().toast(getApplicationContext(), "사진이 저장되었습니다.");

                                })
                                .addOnFailureListener(e -> {

                                    //U.getInstance().toast(getApplicationContext(), "" + e.getMessage());
                                });

                    }).addOnFailureListener(e -> {
                        //삭제 실패
                        U.getInstance().toast(getApplicationContext(), "" + e.getMessage());
                    });
                    break;
                }
            }
        });
    }

}