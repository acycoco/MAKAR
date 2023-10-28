package com.example.makar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.makar.databinding.ActivitySignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    String signupEmail;
    String signupPassword;
    String signupPasswordCheck;
    String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySignupBinding signupBinding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(signupBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();


        //email Listener
        signupBinding.signupEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                //이메일 유효성 검사
                signupEmail = signupBinding.signupEmail.getText().toString();
                if(isValidEmail(signupEmail, emailPattern)){
                    //오류 해제
                    signupBinding.signupEmail.setError(null);
                }
                else{
                    //format error 발생
                    signupBinding.signupEmail.setError("올바른 이메일 주소를 입력하세요.");
                }
            }
        });


        //Password Listener
        signupBinding.signupPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                signupPassword = signupBinding.signupPassword.getText().toString();
                if(signupPassword.length() < 8){
                    //최소 글자 수
                    signupBinding.signupPassword.setError("8자 이상 입력해주세요");
                }
            }
        });



        //PasswordCheck Listener
        signupBinding.signupPasswordCheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                signupPassword = signupBinding.signupPassword.getText().toString();
                signupPasswordCheck = signupBinding.signupPasswordCheck.getText().toString();

                if (signupPasswordCheck.length() < 8) {
                    //최소 글자 수
                    signupBinding.signupPasswordCheck.setError("8자 이상 입력해주세요");
                } else if (!(signupPassword.equals(signupPasswordCheck))) {
                    //비밀번호 확인 실패
                    signupBinding.signupPasswordCheck.setError("비밀번호 확인 실패");
                } else if (signupPassword.equals(signupPasswordCheck)) {
                    //비밀번호 확인 성공
                    //error 해제
                    signupBinding.signupPasswordCheck.setError(null);
                }
            }
        });




        //signupBtn Listener
        signupBinding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupEmail = signupBinding.signupEmail.getText().toString();
                signupPassword = signupBinding.signupPassword.getText().toString();
                signupPasswordCheck = signupBinding.signupPasswordCheck.getText().toString();

                if(signupEmail.equals("") || !isValidEmail(signupEmail, emailPattern)){
                    //이메일 입력 공란
                    Toast.makeText(SignupActivity.this, "올바른 이메일을 입력해주세요", Toast.LENGTH_SHORT).show();

                }
                else if(signupPassword.equals("") || signupPassword.length() < 8){
                    //비밀번호 입력 공란 || 텍스트 글자 수 8자 미만
                    Toast.makeText(SignupActivity.this, "8자 이상 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();

                }
                else if(signupPasswordCheck.equals("") || signupPasswordCheck.length() < 8){
                    //비밀번호 확인 공란 || 텍스트 글자 수 8자 미만
                    Toast.makeText(SignupActivity.this, "비밀번호 확인란을 채워주세요", Toast.LENGTH_SHORT).show();

                }
                else if(!signupPassword.equals(signupPassword)){
                    //비밀번호 확인 실패
                    Toast.makeText(SignupActivity.this, "비밀번호 확인에 실패했습니다", Toast.LENGTH_SHORT).show();
                }
                else{
                    //회원가입
                    createAccount(signupEmail, signupPassword);

                }
            }
        });

    }



    //신규 사용자 가입
    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("signup", "Signup:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignupActivity.this, "회원가입에 성공했습니다", Toast.LENGTH_SHORT).show();

                            //회원가입 성공 후 로그인뷰로 이동
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                        } else {
                            Log.w("signup", "Signup:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "회원가입에 실패했습니다",
                                    Toast.LENGTH_SHORT).show();
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


}