package com.example.makar.onboarding;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.makar.main.MainActivity;
import com.example.makar.R;
import com.example.makar.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private String email;
    private String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFirebaseAuth();

        View rootView = findViewById(android.R.id.content);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 터치 이벤트가 발생시 키보드를 숨기기
                hideKeyboard();
                return false;
            }
        });

        //로그인 버튼 리스너
        binding.loginBtn.setOnClickListener(view -> {
                email = binding.email.getText().toString();
                password = binding.password.getText().toString();

                if (email.equals("")) {
                    Toast.makeText(LoginActivity.this, R.string.email_empty_toast, Toast.LENGTH_SHORT).show();
                } else if (password.equals("")) {
                    Toast.makeText(LoginActivity.this, R.string.password_empty_toast, Toast.LENGTH_SHORT).show();
                }else {
                    signIn(email, password);
                }
        });

        //회원가입 버튼 리스너
        binding.signupBtn.setOnClickListener(view -> {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                //회원가입 뷰로 넘어감
                finish();
        });
    }

    private void initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            //자동 로그인
            updateUI(currentUser);
        }
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
                            updateUI(user);
                        } else {
                            Log.w("login", "SignInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.login_failure_toast,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("uid", user.getUid());
            startActivity(intent);
            finish();
        }
        }
    }
