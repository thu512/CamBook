package com.mustdo.cambook.Ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mustdo.cambook.R;
import com.mustdo.cambook.databinding.ActivityCameraBinding;

public class CameraActivity extends AppCompatActivity {
    ActivityCameraBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera);


    }



}
