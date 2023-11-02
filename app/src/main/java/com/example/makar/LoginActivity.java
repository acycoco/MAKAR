package com.example.makar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.makar.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private String email;
    private String password;
    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //자동로그인
        if(currentUser != null){
            String uid = currentUser.getUid();
            preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            email = preferences.getString("email", null);
            password = preferences.getString("password", null);
            if(email != null & password!=null) {
                //email, password를 preference에서 얻어와서 로그인
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // 자동 로그인 성공
                                //NonRouteMainActivity로 넘어감
                                Log.d("login", "Auto-Login:Success");
                                successLogin();
                            }
                        });
            }
        }


        //로그인 버튼 리스너
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = binding.email.getText().toString();
                password = binding.password.getText().toString();

                if (email.equals("")) {
                    Toast.makeText(LoginActivity.this, R.string.email_empty_toast, Toast.LENGTH_SHORT).show();
                } else if (password.equals("")) {
                    Toast.makeText(LoginActivity.this, R.string.password_empty_toast, Toast.LENGTH_SHORT).show();
                }else {
                    signIn(email, password);
                }
            }
        });

        //회원가입 버튼 리스너
        binding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                //회원가입 뷰로 넘어감
            }
        });
    }


    //기존 사용자 로그인
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("login", "SignInWithEmail:success");
                            Toast.makeText(LoginActivity.this, R.string.login_success_toast, Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid();

                            // UID, email, password를 SharedPreferences에 저장
                            preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("uid", uid);
                            editor.putString("email", email);
                            editor.putString("password", password);
                            editor.apply();

                            successLogin();
                        } else {
                            Log.w("login", "SignInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.login_failure_toast,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void successLogin(){
        //경로 설정 유무에 따라 이동할 Activity 분류해야 함
        Intent intent = new Intent(LoginActivity.this, NonRouteMainActivity.class);
        startActivity(intent);
    }


}