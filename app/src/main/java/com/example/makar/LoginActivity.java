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
            // UID를 사용하여 Firebase에 다시 로그인
            mAuth.signInWithEmailAndPassword(uid, password)
                     .addOnCompleteListener(task -> {
                         if (task.isSuccessful()) {
                             // 자동 로그인 성공
                             //NonRouteMainActivity로 넘어감
                            successLogin();
                         } else {
                             // 자동 로그인 실패
                             // 오류 처리
                         }
                     });
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
                            Log.d("login", "signInWithEmail:success");
                            Toast.makeText(LoginActivity.this, R.string.login_success_toast, Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid();

                            // UID를 SharedPreferences에 저장
                            SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("uid", uid);
                            editor.apply();

                            successLogin();
                        } else {
                            Log.w("login", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.login_failure_toast,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void successLogin(){
        Intent intent = new Intent(LoginActivity.this, NonRouteMainActivity.class);
        startActivity(intent);
    }


}