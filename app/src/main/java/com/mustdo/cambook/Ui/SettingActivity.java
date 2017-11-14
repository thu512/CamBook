package com.mustdo.cambook.Ui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.mustdo.cambook.BuildConfig;
import com.mustdo.cambook.R;
import com.mustdo.cambook.Util.U;
import com.mustdo.cambook.databinding.ActivitySettingBinding;

public class SettingActivity extends AppCompatActivity {
    ActivitySettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);


        binding.back.setOnClickListener(view -> finish());


    }

    @Override
    protected void onResume() {
        super.onResume();
        getVersion();
    }

    //firebase  원격구성으로 버전 가져오기
    public void getVersion() {
        //firebase 원격구성 사용
        final FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        //2.설정
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        config.setConfigSettings(configSettings);
        //3.패치
        //디버깅 모드에서는 0, 상용에서는 3600
        long cacheExpiration = 3600; // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (config.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        config.fetch(cacheExpiration).addOnCompleteListener(task -> {
                    //4.성공 했을 경우에만 실제 fetch진행
                    if (task.isSuccessful()) {
                        //실제패치
                        config.activateFetched();

                        //5.획득
                        String ver = config.getString("VERSION");

                        //버전체크
                        info(ver);

                    } else {

                    }
                }
        ).addOnFailureListener(e -> U.getInstance().toast(getApplicationContext(),""+e.getMessage()));
    }


    //버전 정보 체크
    public void info(String version) {
        final PackageInfo pakageInfo;
        try {
            pakageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            binding.appVersion.setText(pakageInfo.versionName);
            if (!version.equals(pakageInfo.versionName)) {
                binding.versionState.setText("최신 버전이 아닙니다.");
                binding.versionState.setTextColor(Color.parseColor("#c33b4d"));
            } else {
                binding.versionState.setText("최신 버전입니다.");
                binding.versionState.setTextColor(Color.parseColor("#3b4aaa"));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}