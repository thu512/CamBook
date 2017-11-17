package com.mustdo.cambook.Ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mustdo.cambook.R;
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
    private boolean mIsHidden,misSelect;
    private PickData pickData;


    ActionBar actionBar;
    TextView textviewTitle;
    ImageButton buttonDelete;
    View popupView;
    PopupWindow popupWindow;
    Spinner spinner;
    ArrayAdapter<String> adapter;
    ArrayList<String> subject=new ArrayList<>();
    String select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.werb.pickphotoview.R.layout.pick_activty_preview_photo);
        pickData = (PickData) getIntent().getSerializableExtra(PickConfig.INTENT_PICK_DATA);
        path = getIntent().getStringExtra(PickConfig.INTENT_IMG_PATH);
        name = getIntent().getStringExtra(PickConfig.INTENT_DIR_NAME).split("_");   //IMG_20170919_203942.jpg
        imgDate = name[1].substring(0,4)+"-"+name[1].substring(4,6)+"-"+name[1].substring(6,8)+" ";
        imgTime = name[2].substring(0,2)+":"+name[2].substring(2,4);

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
        Log.d("image size", allImagePath.size() + "");
    }

    private void initView() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(pickData.getStatusBarColor());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(pickData.isLightStatusBar()) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        myToolbar = (MyToolbar) findViewById(com.werb.pickphotoview.R.id.toolbar);

        myToolbar.setPhotoDirName(imgDate+imgTime);
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

        //옵션 메뉴 클릭
        myToolbar.setRightIcon(com.mustdo.cambook.R.mipmap.ic_menu);
        myToolbar.setRightLayoutOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    PickImagePreviewActivity.this.openOptionsMenu();

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
            if(path.endsWith(".gif")) {
                container.addView(gif,params);
                Glide.with(PickImagePreviewActivity.this).asGif().load(new File(path)).into(gif);
                return gif;
            }else {
                container.addView(pic,params);
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

    private void judgeSelect(final String path){
        int indexOf = selectImagePath.indexOf(path);
        if(indexOf != -1){
            myToolbar.setRightIconDefault(com.werb.pickphotoview.R.mipmap.pick_ic_select);
            misSelect = true;
        }else {
            myToolbar.setRightIcon(com.werb.pickphotoview.R.mipmap.pick_ic_un_select_black);
            misSelect = false;
        }

        myToolbar.setRightLayoutOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(misSelect){
                    myToolbar.setRightIcon(com.werb.pickphotoview.R.mipmap.pick_ic_un_select_black);
                    selectImagePath.remove(path);
                    PickHolder.setStringPaths(selectImagePath);
                    misSelect = false;
                }else {
                    if(selectImagePath.size() < pickData.getPickPhotoSize()) {
                        myToolbar.setRightIconDefault(com.werb.pickphotoview.R.mipmap.pick_ic_select);
                        selectImagePath.add(path);
                        PickHolder.setStringPaths(selectImagePath);
                        misSelect = true;
                    }else {
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

    private void finishForResult(){
        Intent intent = new Intent();
        intent.setClass(PickImagePreviewActivity.this, PickPhotoActivity.class);
        intent.putExtra(PickConfig.INTENT_IMG_LIST_SELECT, selectImagePath);
        setResult(PickConfig.PREVIEW_PHOTO_DATA,intent);
        finish();
    }

    private void changePage(String path){
        String[] nameArr = path.split("/");
        String[] name = nameArr[nameArr.length-1].split("_");
        imgDate = name[1].substring(0,4)+"-"+name[1].substring(4,6)+"-"+name[1].substring(6,8)+" ";
        imgTime = name[1].substring(0,2)+":"+name[2].substring(2,4);

        myToolbar.setPhotoDirName(imgDate+imgTime);

    }

    //액션바 설정
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_imagepreview,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_edit:
                return true;
            case R.id.menu_move:
                imageChange(R.id.menu_move);
                return true;
            case R.id.menu_copy:
                return true;
            case R.id.menu_delete:
                imageDelete();

                return true;
        }

        return false;
    }
    private void imageDelete(){
        File f = new File(path);
        f.delete();
        Toast.makeText(PickImagePreviewActivity.this, "삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
        finish();
        Intent intent = new Intent(PickImagePreviewActivity.this, PickPhotoActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
    private void imageSave(){


    }
    private void imageLoad(){

    }
    private void imageChange(final int id){
        popupView = getLayoutInflater().inflate(R.layout.activity_submenu,null);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        Button cancel = (Button) popupView.findViewById(R.id.popupCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });

        Button submit = (Button) popupView.findViewById(R.id.popupSubmit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(select.equals("앨범 선택") || select.equals("------------------------")){
                    Toast.makeText(PickImagePreviewActivity.this,"앨범을 선택해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                 switch (id){
                     case R.id.menu_move:
                         //imageMove();
                         break;
                     case R.id.menu_copy:

                         break;

                 }
                //Toast.makeText(, "Clicked Submit...!", Toast.LENGTH_SHORT).show();
            }
        });

        spinner = (Spinner)findViewById(R.id.spinnerSub);
        String[] files = this.getExternalFilesDir(Environment.DIRECTORY_DCIM).list();

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,files);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //adapter.add("앨범 선택");
        /*
        for(int i=0; i<files.length; i++){
            adapter.add(files[i]);
        }*/
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                select=parent.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

}
