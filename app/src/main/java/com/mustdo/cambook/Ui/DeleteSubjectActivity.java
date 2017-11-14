package com.mustdo.cambook.Ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.databinding.ActivityDeleteSubjectBinding;


public class DeleteSubjectActivity extends Activity {
    ActivityDeleteSubjectBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_delete_subject);
        binding.back.setOnClickListener((view -> finish()));
    }
}
