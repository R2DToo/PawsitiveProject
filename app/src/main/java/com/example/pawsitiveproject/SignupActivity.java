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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the firebase sign-up activity
 */
public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
    EditText et_email, et_password, et_confirm_password;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        et_email = findViewById(R.id.signup_email);
        et_password = findViewById(R.id.signup_password);
        et_confirm_password = findViewById(R.id.signup_confirm_password);

        mAuth = FirebaseAuth.getInstance();
        Button btn_goto_login = findViewById(R.id.btn_goto_login);
        btn_goto_login.setOnClickListener(this::onClick);
        Button btn_signup = findViewById(R.id.btn_signup);
        btn_signup.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_goto_login:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.btn_signup:
                registerNewUser();
                break;
        }
    }

    private void registerNewUser() {
        String email = et_email.getText().toString();
        String password = et_password.getText().toString();
        String confirm_password = et_confirm_password.getText().toString();

        if (TextUtils.isEmpty(email)) {
            et_email.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            et_password.setError("Password is required");
            return;
        }

        if (TextUtils.isEmpty(confirm_password)) {
            et_confirm_password.setError("Confirm password is required");
            return;
        }

        Pattern spacePattern = Pattern.compile(" ", Pattern.CASE_INSENSITIVE);
        Matcher passwordMatcher = spacePattern.matcher(password);
        boolean passwordContainsSpaces = passwordMatcher.find();
        if (passwordContainsSpaces) {
            et_password.setError("No spaces allowed");
            return;
        }

        Matcher confirmPasswordMatcher = spacePattern.matcher(password);
        boolean confirmPasswordContainsSpaces = confirmPasswordMatcher.find();
        if (confirmPasswordContainsSpaces) {
            et_confirm_password.setError("No spaces allowed");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            et_email.setError("Please enter a valid email");
            return;
        }

        if (!password.equals(confirm_password)) {
            et_confirm_password.setError("Passwords do not match");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d("bsr", "createUserWithEmail:success");
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null){
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("users").child(user.getUid()).child("email").setValue(email);
                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                }
            } else {
                // If sign in fails, display a message to the user.
                String errorMessage = task.getException().getMessage();
                errorMessage = errorMessage.substring(errorMessage.indexOf("[") + 1);
                errorMessage = errorMessage.substring(0, errorMessage.indexOf("]"));
                errorMessage = errorMessage.trim();
                et_password.setError(errorMessage);
            }
        });
    }
}