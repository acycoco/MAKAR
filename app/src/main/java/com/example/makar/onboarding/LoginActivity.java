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

import com.example.makar.data.ActivityUtil;
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
    public static String userUId;
    private String email;
    private String password;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFirebaseAuth();
        
        setActivityUtil();
        setButtonListener();
    }

    // MARK: setActivityUtil()
    private void setActivityUtil() {
        ActivityUtil.setHideKeyboard(binding.getRoot());
    }

    // MARK: setButtonListener()
    private void setButtonListener() {
        //로그인 버튼 리스너
        binding.loginBtn.setOnClickListener(view -> {
            email = binding.emailEditText.getText().toString();
            password = binding.passwordEditText.getText().toString();

            if (email.equals("")) {
                Toast.makeText(LoginActivity.this, R.string.email_empty_toast, Toast.LENGTH_SHORT).show();
            } else if (password.equals("")) {
                Toast.makeText(LoginActivity.this, R.string.password_empty_toast, Toast.LENGTH_SHORT).show();
            } else {
                signIn(email, password);
            }
        });

        //회원가입 버튼 리스너
        binding.signupBtn.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        userUId = FirebaseAuth.getInstance().getUid();
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

    // MARK: 기존 사용자 로그인
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("MAKAR_LOGIN", "SignInWithEmail:success");
                            Toast.makeText(LoginActivity.this, R.string.login_success_toast, Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            userUId = FirebaseAuth.getInstance().getUid();
                            updateUI(user);
                        } else {
                            Log.w("MAKAR_LOGIN", "SignInWithEmail:failure", task.getException());
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
