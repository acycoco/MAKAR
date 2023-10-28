package com.example.makar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.makar.databinding.ActivitySignupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    String signupEmail;
    String signupPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    ActivitySignupBinding signupBinding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(signupBinding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        signupBinding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupEmail = signupBinding.signupEmail.getText().toString();
                signupPassword = signupBinding.signupPassword.getText().toString();

                if(signupEmail.equals("")||signupPassword.equals("")){
                    Toast.makeText(SignupActivity.this, "빈 칸을 채워주세요", Toast.LENGTH_SHORT).show();
                }
                else{
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

                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                        } else {
                            Log.w("signup", "Signup:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "회원가입에 실패했습니다",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}