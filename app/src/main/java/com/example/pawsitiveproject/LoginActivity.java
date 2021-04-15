package com.example.pawsitiveproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the firebase login activity
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText et_email, et_password;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_email = findViewById(R.id.login_email);
        et_password = findViewById(R.id.login_password);

        mAuth = FirebaseAuth.getInstance();

        Button btn_goto_signup = findViewById(R.id.btn_goto_signup);
        btn_goto_signup.setOnClickListener(this::onClick);
        Button btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this::onClick);
    }

    @Override
    public void onStart() {
        super.onStart();
        //Checks if the user was previously logged in. If they were, then start the main activity.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_goto_signup:
                startActivity(new Intent(this, SignupActivity.class));
                break;
            case R.id.btn_login:
                login();
                break;
        }
    }

    /**
     * This login method will validate all user inputs, then sign in with the provided credentials.
     */
    public void login() {
        String email = et_email.getText().toString();
        String password = et_password.getText().toString();

        if (TextUtils.isEmpty(email)) {
            et_email.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            et_password.setError("Password is required");
            return;
        }

        Pattern spacePattern = Pattern.compile(" ", Pattern.CASE_INSENSITIVE);

        Matcher passwordMatcher = spacePattern.matcher(password);
        boolean passwordContainsSpaces = passwordMatcher.find();
        if (passwordContainsSpaces) {
            et_password.setError("No spaces allowed");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et_email.setError("Please enter a valid email");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }
            } else {
                // Sign in fails, display a message to the user.
                String errorMessage = task.getException().getMessage();
                et_password.setError(errorMessage);
            }
        });
    }
}