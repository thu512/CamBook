package com.mustdo.cambook.Ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mustdo.cambook.Model.User;
import com.mustdo.cambook.R;
import com.mustdo.cambook.SuperActivity.Activity;
import com.mustdo.cambook.Util.U;
import com.mustdo.cambook.databinding.ActivityLoginBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    //구글 페북 파베
    private FirebaseAuth.AuthStateListener mAuthListener;

    private static Context context;

    private static U U;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        context = getApplicationContext();
        U = U.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();


        //google버튼모양
        binding.signInButton.setSize(SignInButton.SIZE_WIDE);

        binding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPd();
                onGoogleLogin();
            }
        });

        //로그인버튼 클릭
        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onLogin();
            }

        });

        //회원가입버튼 클릭
        binding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, JoinActivity.class));
            }
        });

//        //비밀번호 찾기
//        binding.btn3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(LoginActivity.this,FindPwdActivity.class));
//            }
//        });

        //비회원 로그인
        binding.signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                anonymouslySignUp();
            }
        });

        binding.fbLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPd();
            }
        });
        //facebook
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = binding.fbLoginBtn;
        loginButton.setReadPermissions("email", "public_profile", "user_birthday");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i("TTT", "FB=> 엑세스토큰: " + loginResult.getAccessToken().getToken());
                onFaceBookInfoWithAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                stopPd();
            }

            @Override
            public void onError(FacebookException error) {
                stopPd();
            }
        });

        // google
        initGoogleLoginInit();


        //구글 페북
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("TTT", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("TTT", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

    }


    public void anonymouslySignUp() {
        showPd();

        //익명 로그인을 진행하고 그 결과를 비동기적으로 받아서 결과를 리스너 객체를 통해 전달한다.
        firebaseAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //성공 => 회원정보 획득
                    FirebaseUser user = getUser(); //firebaseAuth.getCurrentUser();
                    if (user != null) {
                        U.log(user.getUid());
                        U.log("" + user.isAnonymous());
                        U.toast(LoginActivity.this, "익명계정 생성 성공.");
                        User u = new User(user.getUid(), null, null, null);

                        //firestore 입력
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users")
                                .document(user.getUid())
                                .set(u)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        U.toast(context, "firestone ok");
                                        stopPd();
                                        startActivity(new Intent(context, MainActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        U.toast(context, "firestone fail" + e.getMessage());
                                        stopPd();
                                    }
                                });
                    }
                } else {
                    //실패
                    U.toast(LoginActivity.this, "익명계정 생성 실패.");
                    stopPd();
                }

            }
        });

    } //익명가입 / 로그인

    public void onLogin() {

        if (!isVaild()) {
            return;
        }
        showPd();
        String email = binding.id.getText().toString();
        String password = binding.pw.getText().toString();
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = getUser();
                    //연결이 성공되었으니, 허가된 서버로 이동
                    U.log("로그인완료");
                    U.log(user.getUid());
                    U.log(user.getEmail());
                    U.toast(LoginActivity.this, "이메일로그인 성공.");
                    startActivity(new Intent(context, MainActivity.class));
                    finish();

                } else {
                    //실패 사유 보여주기
                    U.toast(LoginActivity.this, "이메일로그인 실패.\n" + task.getException().getMessage());
                }
                stopPd();
            }
        });
    } //이메일 로그인


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

    // =============================================================================================
    // google
    private static final int RC_SIGN_IN = 9001; // 구글 로그인 요청코드 (값은 변경 가능함)
    private FirebaseAuth mAuth;                 // fb 인증 객체
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

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
    }

    // google
    public void onGoogleLogin() {
        signIn();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("T", "연결 실패 => 재시도 같은 시나리오 필요:" + connectionResult);
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
        Log.d("T", "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        //showProgressDialog();
        // [END_EXCLUDE]
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.i("T", "" + user.getDisplayName());
                            Log.i("T", "" + user.getEmail());
                            Log.i("T", "" + user.getUid());
                            Log.i("T", "" + user.getPhotoUrl().toString());

                            User u = new User(user.getUid(), null, user.getEmail(), user.getDisplayName());

                            //firestore 입력
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users")
                                    .document(user.getUid())
                                    .set(u)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            U.toast(context, "firestone ok");
                                            stopPd();
                                            startActivity(new Intent(context, MainActivity.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            stopPd();
                                            U.toast(context, "firestone fail" + e.getMessage());

                                        }
                                    });

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            stopPd();
                        }

                    }
                });
    }

    //====================================================================================
    // facebook
    CallbackManager callbackManager;


    public void onFaceBookInfoWithAccessToken(final AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.i("TTT", "signInWithCredential", task.getException());
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.i("TTT", "파이어베이스 페북 로그인 완료 => 개인정보 획득");
                            if (Profile.getCurrentProfile() != null) {
                                Log.i("TTT", " " + Profile.getCurrentProfile().getName());
                                Log.i("TTT", " " + Profile.getCurrentProfile().getLinkUri());
                                Log.i("TTT", " " + Profile.getCurrentProfile().getProfilePictureUri(100, 100));
                                Log.i("TTT", " " + Profile.getCurrentProfile().getId());
                            }

                            //개인정보 획득을위한 요청을 만듬
                            GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    try {
                                        Log.i("TTT", " " + object.getString("email"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Log.i("TTT", "" + object.toString());

                                }
                            });

                            Bundle param = new Bundle();
                            param.putString("fields", "email,id,name,gender,birthday");
                            //요청 수행
                            request.setParameters(param);
                            request.executeAsync();
                        }
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        //google & facebook
        mAuth.addAuthStateListener(mAuthListener);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Log.i("TTT", "" + user.getDisplayName());
            Log.i("TTT", "" + user.getEmail());
            Log.i("TTT", "" + user.getUid());
            Log.i("TTT", "" + user.getPhotoUrl().toString());
            startActivity(new Intent(context, MainActivity.class));
            finish();
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
                U.log("google 로그인 실패\n");
                stopPd();
            }
        } else {

            //facebook
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
