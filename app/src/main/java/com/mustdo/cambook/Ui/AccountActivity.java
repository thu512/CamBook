package com.mustdo.cambook.Ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mustdo.cambook.Model.User;
import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.Util.U;
import com.mustdo.cambook.databinding.ActivityAccountBinding;


public class AccountActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener {

    ActivityAccountBinding binding;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account);
        initGoogleLoginInit();

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        if (user.isAnonymous()) {
            binding.email.setVisibility(View.VISIBLE);
            binding.idbox.setText("비회원 입니다.");
        } else {
            DocumentReference userRef = db.collection("users").document(user.getUid());
            userRef.get().addOnSuccessListener(documentSnapshots -> {
                User user = documentSnapshots.toObject(User.class);
                binding.idbox.setText("" + user.getName());
            }).addOnFailureListener(e -> {
                U.getInstance().toast(AccountActivity.this, "" + e.getMessage());
            });

        }


        binding.email.setOnClickListener(view -> {
                    Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
                    intent.putExtra("Anony", true);
                    startActivity(intent);
                }
        );


        binding.logout.setOnClickListener(view -> {


            if(user.isAnonymous()){
                U.getInstance().showPopup3(AccountActivity.this,
                        "경고", "비회원 로그아웃 시 데이터가 사라집니다.",
                        "확인",(sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            logOut();
                        }),
                        "취소", sweetAlertDialog -> sweetAlertDialog.dismissWithAnimation());
            }else {
                logOut();
            }

        });
    }

    public void logOut(){
        LoginManager.getInstance().logOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                status -> U.getInstance().log("구글 로그 아웃"));
        signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    private GoogleApiClient mGoogleApiClient;   // 구글 로그인 담당 객체(Api 담당 객체)

    // google
    // 초기화
    public void initGoogleLoginInit() {
        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
