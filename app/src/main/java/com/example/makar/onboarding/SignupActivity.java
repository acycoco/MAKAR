package com.example.makar.onboarding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.makar.data.ActivityUtil;
import com.example.makar.databinding.ActivitySignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    String signupEmail;
    String signupPassword;
    String signupPasswordCheck;
    String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";
    ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setActivityUtil();
        setButtonListener();

        initFirebaseAuth();
    }

    // MARK: setActivityUtil()
    private void setActivityUtil() {
        ActivityUtil.setActionBar(this, binding.toolbarSignUp.getRoot());
        ActivityUtil.setToolbar(binding.toolbarSignUp, "회원가입");
        ActivityUtil.setHideKeyboard(binding.getRoot());
    }

    // MARK: setButtonListener()
    private void setButtonListener() {
        //email Listener
        binding.signupEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //이메일 유효성 검사
                signupEmail = binding.signupEmailEditText.getText().toString();
                if (isValidEmail(signupEmail, emailPattern)) {
                    //오류 해제
                    binding.signupEmailEditText.setError(null);
                } else {
                    //format error 발생
                    binding.signupEmailEditText.setError("올바른 이메일 주소를 입력하세요.");
                }
            }
        });

        //Password Listener
        binding.signupPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                signupPassword = binding.signupPasswordEditText.getText().toString();
                if (signupPassword.length() < 8) {
                    //최소 글자 수
                    binding.signupPasswordEditText.setError("8자 이상 입력해주세요");
                }
            }
        });

        //PasswordCheck Listener
        binding.signupPasswordCheckEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                signupPassword = binding.signupPasswordEditText.getText().toString();
                signupPasswordCheck = binding.signupPasswordCheckEditText.getText().toString();

                if (signupPasswordCheck.length() < 8) {
                    //최소 글자 수
                    binding.signupPasswordCheckEditText.setError("8자 이상 입력해주세요");
                } else if (!(signupPassword.equals(signupPasswordCheck))) {
                    //비밀번호 확인 실패
                    binding.signupPasswordCheckEditText.setError("비밀번호 확인 실패");
                } else if (signupPassword.equals(signupPasswordCheck)) {
                    //비밀번호 확인 성공
                    //error 해제
                    binding.signupPasswordCheckEditText.setError(null);
                }
            }
        });

        //signupBtn Listener
        binding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupEmail = binding.signupEmailEditText.getText().toString();
                signupPassword = binding.signupPasswordEditText.getText().toString();
                signupPasswordCheck = binding.signupPasswordCheckEditText.getText().toString();

                if (signupEmail.equals("") || !isValidEmail(signupEmail, emailPattern)) {
                    //이메일 입력 공란
                    Toast.makeText(SignupActivity.this, "올바른 이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                } else if (signupPassword.equals("") || signupPassword.length() < 8) {
                    //비밀번호 입력 공란 || 텍스트 글자 수 8자 미만
                    Toast.makeText(SignupActivity.this, "8자 이상 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                } else if (signupPasswordCheck.equals("") || signupPasswordCheck.length() < 8) {
                    //비밀번호 확인 공란 || 텍스트 글자 수 8자 미만
                    Toast.makeText(SignupActivity.this, "비밀번호 확인란을 채워주세요", Toast.LENGTH_SHORT).show();
                } else if (!signupPassword.equals(signupPassword)) {
                    //비밀번호 확인 실패
                    Toast.makeText(SignupActivity.this, "비밀번호 확인에 실패했습니다", Toast.LENGTH_SHORT).show();
                } else {
                    //회원가입
                    createAccount(signupEmail, signupPassword);
                }
            }
        });
    }

    private void initFirebaseAuth() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    //신규 사용자 가입
    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d("signup", "Signup:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(SignupActivity.this, "회원가입에 성공했습니다", Toast.LENGTH_SHORT).show();

                    mAuth.signOut();
                    //회원가입 성공 후 로그인뷰로 이동
                    updateUI(user);
                } else {
                    //회원가입 실패
                    //이미 존재하는 이메일 주소를 기입했을 경우
                    if (task.getException() instanceof FirebaseAuthException) {
                        FirebaseAuthException e = (FirebaseAuthException) task.getException();
                        Log.e("signup", "errorcode : " + e.getErrorCode());
                        if ("ERROR_EMAIL_ALREADY_IN_USE".equals(e.getErrorCode())) {
                            Log.d("signup", "existing email address");
                            Toast.makeText(SignupActivity.this, "이미 가입된 이메일 주소입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w("signup", "Signup:failure", task.getException());
                        Toast.makeText(SignupActivity.this, "회원가입에 실패했습니다", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    //이메일 유효성 검사
    private boolean isValidEmail(String email, String emailPattern) {
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // MARK: toolbar
    public boolean onOptionsItemSelected(MenuItem item) {
        return ActivityUtil.handleOptionsItemSelected(item, this);
    }
}