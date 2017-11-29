package com.mustdo.cambook.Ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mustdo.cambook.Model.User;
import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.Util.U;
import com.mustdo.cambook.databinding.ActivityLoginBinding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static Context context;
    private static U U;

    // google
    private static final int RC_SIGN_IN = 9001; // 구글 로그인 요청코드 (값은 변경 가능함)
    private FirebaseAuth mAuth;                 // fb 인증 객체
    private GoogleApiClient mGoogleApiClient;   // 구글 로그인 담당 객체(Api 담당 객체)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        context = getApplicationContext();
        U = U.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();

        //getHash();
        // google
        initGoogleLoginInit();

        //google버튼모양
        binding.signInButton.setSize(SignInButton.SIZE_WIDE);

        //구글 로그인
        binding.signInButton.setOnClickListener((view -> {
            showPd();
            onGoogleLogin();
        }));

        //로그인버튼 클릭
        binding.login.setOnClickListener((view -> onLogin()));

        //회원가입버튼 클릭
        binding.signupBtn.setOnClickListener((view -> startActivity(new Intent(LoginActivity.this, JoinActivity.class))));

        //비밀번호 찾기
        binding.findpw.setOnClickListener((view -> startActivity(new Intent(LoginActivity.this, FindPwdActivity.class))));

        //비회원 로그인
        binding.signin.setOnClickListener((view -> anonymouslySignUp()));







        //구글
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    U.log("onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    U.log("onAuthStateChanged:signed_out");
                }
            }
        };




    }


    //익명가입 / 로그인
    public void anonymouslySignUp() {
        showPd();

        //익명 로그인을 진행하고 그 결과를 비동기적으로 받아서 결과를 리스너 객체를 통해 전달한다.
        firebaseAuth.signInAnonymously().addOnCompleteListener(this, (task -> {
            if (task.isSuccessful()) {
                //성공 => 회원정보 획득
                FirebaseUser user = getUser(); //firebaseAuth.getCurrentUser();
                if (user != null) {
                    U.log(user.getUid());
                    U.log("" + user.isAnonymous());
                    //U.toast(LoginActivity.this, "익명계정 생성 성공.");
                    User u = new User(user.getUid(), null, null, null);

                    //firestore 입력
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users")
                            .document(user.getUid())
                            .set(u)
                            .addOnSuccessListener((aVoid -> {
                                //U.toast(context, "firestore ok");
                                stopPd();
                                startActivity(new Intent(context, MainActivity.class));
                                finish();
                            }))
                            .addOnFailureListener(e -> {
                                stopPd();
                                U.toast(context, "firestore fail" + e.getMessage());
                            });
                }
            } else {
                //실패
                U.toast(LoginActivity.this, "익명계정 생성 실패.");
                stopPd();
            }
        }));
    }


    //이메일 로그인
    public void onLogin() {

        if (!isVaild()) {
            return;
        }
        showPd();
        String email = binding.id.getText().toString();
        String password = binding.pw.getText().toString();
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener((task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = getUser();
                //연결이 성공되었으니, 허가된 서버로 이동
                U.log("로그인완료");
                U.log(user.getUid());
                U.log(user.getEmail());
                //U.toast(LoginActivity.this, "이메일로그인 성공.");
                if (user.isEmailVerified()) {
                    startActivity(new Intent(context, MainActivity.class));
                    finish();
                } else {
                    U.showPopup2(LoginActivity.this,
                            "이메일 인증",
                            "이메일 인증 후 사용해 주세요.",
                            "확인",
                            sweetAlertDialog -> {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    U.toast(getApplicationContext(), "인증 메일이 전송되었습니다.");
                                                }
                                            }
                                        });
                                sweetAlertDialog.dismissWithAnimation();
                            }
                    );
                }


            } else {
                //실패 사유 보여주기
                U.toast(LoginActivity.this, "이메일로그인 실패.\n" + task.getException().getMessage());
            }
            stopPd();
        }));
    }


    //공백 검증
    public boolean isVaild() {
        String email = binding.id.getText().toString();
        String password = binding.pw.getText().toString();

        if (TextUtils.isEmpty(email)) {
            binding.id.setError("이메일을 입력하세요.");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            binding.pw.setError("비밀번호를 입력하세요.");
            return false;
        }

        return true;
    }

    // google
    // 초기화
    public void initGoogleLoginInit() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        U.log("구글 로그인 실패 메세지: " + connectionResult.getErrorMessage());
                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

    }


    // google
    public void onGoogleLogin() {
        signIn();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        U.log("연결 실패 => 재시도 같은 시나리오 필요:" + connectionResult);
    }

    // google
    // 구글 로그인 시작점
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        // 다른 액티비티를 구동해서 결과를 돌려 받을려면 이렇게 액티비티를 구동해야 한다.
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // google
    // 로그인 성공후 호출 코드
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        U.log("firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, (task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        U.log("" + user.getDisplayName());
                        U.log("" + user.getEmail());
                        U.log("" + user.getUid());
                        U.log("" + user.getPhotoUrl().toString());

                        User u = new User(user.getUid(), null, user.getEmail(), user.getDisplayName());

                        //firestore 입력
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users")
                                .document(user.getUid())
                                .set(u)
                                .addOnSuccessListener((aVoid -> {
                                    //U.toast(context, "firestore ok");
                                    stopPd();
                                    startActivity(new Intent(context, MainActivity.class));
                                    finish();
                                }))
                                .addOnFailureListener((e -> {
                                    stopPd();
                                    U.toast(context, "firestore fail" + e.getMessage());
                                }));

                    } else {
                        // If sign in fails, display a message to the user.
                        U.toast(context, "" + task.getException().getMessage());
                        stopPd();
                    }
                }));
    }





    @Override
    public void onStart() {
        super.onStart();
        //google & facebook
        mAuth.addAuthStateListener(mAuthListener);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            U.log("" + user.getDisplayName());
            U.log("" + user.getEmail());
            U.log("" + user.getUid());
            //U.log("" + user.getPhotoUrl().toString());
            if (user.isEmailVerified()) {
                Log.d("TTT", "onStart");
                startActivity(new Intent(context, MainActivity.class));
                finish();
            }

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        //google & facebook
        //fb인증관련 리스너 해제
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // google
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {

                U.log("google 로그인 실패\n" + result.getStatus().getStatusMessage());
                U.log("" + result.getStatus().toString());
                stopPd();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


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


    private void getHash(){
        try{
            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_SIGNATURES);
            for(android.content.pm.Signature signature : info.signatures){
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("TTT", Base64.encodeToString(md.digest(), Base64.DEFAULT)); }
        }catch (PackageManager.NameNotFoundException e){
        }catch ( NoSuchAlgorithmException e){
        }
    }
}
