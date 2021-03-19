package com.example.pawsitiveproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private final String ALLOWED_CHARACTERS ="0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-";
    private final String tag = "api-req";
    private Boolean firstPicture = true;
    private RequestQueue mReqQueue;
    private final String API_KEY = "2fc6b7da-314a-4327-9888-0fd52d813f7f";
    private static final int RC_SIGN_IN = 123;
    private String userId;

    private Toolbar toolbar;
    private NavController navController;

    private FirebaseUser user;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottom_nav = findViewById(R.id.bottom_nav);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_controller);
        navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottom_nav, navController);
        NavigationUI.setupActionBarWithNavController(this, navController);

        signIn();


    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d("bsr", "onSupportNavigateUp");
        return navController.navigateUp();
    }

    private void signIn() {
        // Initialize Firebase Auth and check if the user is signed in
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                //new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
                //new AuthUI.IdpConfig.FacebookBuilder().build(),
                //new AuthUI.IdpConfig.TwitterBuilder().build()
        );

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().getCurrentUser();
                database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference();

                myRef.child("users").child(user.getUid()).child("userID").get().addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Random random=new Random();
                        StringBuilder sb=new StringBuilder(10);
                        for(int i=0;i<10;++i)
                            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
                        String uniqueId = sb.toString();
                        userId = uniqueId;
                        Log.d("bsr", "Setting: " + userId);
                        myRef.child("users").child(user.getUid()).child("userID").setValue(userId);
                    }
                    else {
                        Log.d("bsr", "DB UserId  == " + String.valueOf(task.getResult().getValue()));
                        userId = String.valueOf(task.getResult().getValue());
                    }
                });
                Log.d("bsr", user.getEmail());
                myRef.child("users").child(user.getUid()).child("email").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.d("bsr", "Setting: " + user.getEmail());
                            myRef.child("users").child(user.getUid()).child("email").setValue(user.getEmail());
                        }
                        else {
                            Log.d("bsr", "DB Email == " + String.valueOf(task.getResult().getValue()));
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Error while trying to login", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.tb_logout) {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        user = null;
                        signIn();
                    }
                });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // if queue is not empty cancels all the requests with given tag
        if (mReqQueue != null){
            mReqQueue.cancelAll(tag);
        }
    }

    public String getUserId() {
        return this.userId;
    }
}