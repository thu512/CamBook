package com.mustdo.cambook.Ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.mustdo.cambook.BuildConfig;
import com.mustdo.cambook.R;
import com.mustdo.cambook.Util.U;
import com.mustdo.cambook.databinding.ActivitySettingBinding;

import java.util.concurrent.TimeUnit;

public class SettingActivity extends AppCompatActivity {
    private ActivitySettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);


        binding.back.setOnClickListener(view -> finish());
        //스위치 -> Sp연동
        if (U.getInstance().getBoolean(this, "autosave")) {
            binding.autoSaveSwt.setChecked(true);
        } else {
            binding.autoSaveSwt.setChecked(false);
        }
        if (U.getInstance().getBoolean(this, "startphoto")) {
            binding.luchSetSwt.setChecked(true);
        } else {
            binding.luchSetSwt.setChecked(false);
        }

        binding.luchSetSwt.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                U.getInstance().setBoolean(this, "startphoto", true);
                binding.luchSetSwt.setChecked(true);
            } else {
                U.getInstance().setBoolean(this, "startphoto", false);
                binding.luchSetSwt.setChecked(false);
            }
        });

        binding.privacy.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse("https://blog.naver.com/thu512/221147322047/");
            intent.setData(uri);
            startActivity(intent);

        });

        binding.autoSaveSwt.setChecked(true);
        binding.autoSaveSwt.setOnClickListener(view -> {
            U.getInstance().showPopup1(this, "죄송합니다.", "추후 업데이트 될 예정입니다.\n\n현재 자동저장 실행 중 입니다.", "확인",
                    sweetAlertDialog -> sweetAlertDialog.dismissWithAnimation());
            binding.autoSaveSwt.setChecked(true);
        });

        binding.getphoto.setOnClickListener(view -> {
            U.getInstance().showPopup2(this, "알림", "로그아웃 하신 후\n다시 로그인 하시면 자동으로 \n데이터가 복구 됩니다.",
                    "확인", sweetAlertDialog -> sweetAlertDialog.dismissWithAnimation());
        });


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
        long minimumFetchInvervalInSeconds = 0L;
        if (!BuildConfig.DEBUG) {
            minimumFetchInvervalInSeconds = TimeUnit.HOURS.toSeconds(12);
        }
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(minimumFetchInvervalInSeconds)
                .build();
        config.setConfigSettingsAsync(configSettings);
        //3.패치
        //디버깅 모드에서는 0, 상용에서는 3600
        long cacheExpiration = 3600; // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (config.getInfo().getConfigSettings().getMinimumFetchIntervalInSeconds() == 0L) {
            cacheExpiration = 0;
        }
        config.fetch(cacheExpiration).addOnCompleteListener(task -> {
                    //4.성공 했을 경우에만 실제 fetch진행
                    if (task.isSuccessful()) {
                        //실제패치
                        config.fetchAndActivate();

                        //5.획득
                        String ver = config.getString("VERSION");

                        //버전체크
                        info(ver);

                    } else {

                    }
                }
        ).addOnFailureListener(e -> U.getInstance().toast(getApplicationContext(), "" + e.getMessage()));
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
