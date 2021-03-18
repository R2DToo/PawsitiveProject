package com.example.pawsitiveproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

    private ArrayList<PictureItem> image_queue;
    private SwipeAdapter swipeAdapter;
    private SwipeFlingAdapterView flingContainer;
    private SharedPreferences sharedPreferences;
    private Toolbar toolbar;

    private FirebaseUser user;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        signIn();

        mReqQueue = Volley.newRequestQueue(this);

        image_queue = new ArrayList<PictureItem>();
        PictureItem init_item = new PictureItem(
            "duYK7C91Q",
            "https://cdn2.thedogapi.com/images/duYK7C91Q.jpg",
            "Job",
            "Job Category",
            "Lifespan",
            "Name",
            "Temperament"
        );
        image_queue.add(init_item);
        getRandomPicture();

        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.swipe_picture_frame);
        swipeAdapter = new SwipeAdapter(MainActivity.this, R.layout.picture_item, image_queue);
        flingContainer.setAdapter(swipeAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                //Log.d("bsr", "removeFirstObject");
                image_queue.remove(0);
                swipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object o) {
                Log.d("bsr", "<---swipe left--- == downvote");
                PictureItem item = (PictureItem)o;
                postNewVote(0, item.getId());
            }

            @Override
            public void onRightCardExit(Object o) {
                Log.d("bsr", "---swipe right---> == upvote");
                PictureItem item = (PictureItem)o;
                postNewVote(1, item.getId());
            }

            @Override
            public void onAdapterAboutToEmpty(int i) {
                //Log.d("bsr", "AdapterAboutToEmpty");
                getRandomPicture();
                swipeAdapter.notifyDataSetChanged();

            }

            @Override
            public void onScroll(float v) {
                //Log.d("bsr", "onScroll");
            }
        });
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

    private void getRandomPicture() {
        String url = "https://api.thedogapi.com/v1/images/search?mime_types=jpg,png";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
            response -> {
                //Iterates through elements in response array and displays in TextView
                //Log.d("bsr", response.toString());
                for (int i = 0; i < response.length(); i++){
                    try {
                        //Gets jsonObject at index i
                        JSONObject jsonObject = response.getJSONObject(i);
                        //gets date from jsonObject by key name
                        Log.d("bsr", jsonObject.toString());
                        String id = jsonObject.getString("id");
                        String pictureUrl = jsonObject.getString("url");
                        JSONArray breeds = jsonObject.getJSONArray("breeds");
                        String job = breeds.getJSONObject(0).getString("bred_for");
                        String job_category = breeds.getJSONObject(0).getString("breed_group");
                        String lifespan = breeds.getJSONObject(0).getString("life_span");
                        String name = breeds.getJSONObject(0).getString("name");
                        String temperament = breeds.getJSONObject(0).getString("temperament");
                        PictureItem newItem = new PictureItem(id, pictureUrl, job, job_category, lifespan, name, temperament);
                        image_queue.add(newItem);
                    } catch (JSONException je) {
                        Log.d("bsr", "JSON ERROR: " + je);
                    }
                }
            }, error -> {
                // On error in parsing logs the error
                Log.d("bsr", "RESPONSE ERROR: " + error.toString());
            }

        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-api-key", API_KEY);
                //Log.d("bsr", "getHeaders");
                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("sub_id", userId);
                params.put("include_vote", "1");
                params.put("include_favourite", "1");
                return params;
            }
        };

        jsonArrayRequest.setTag(tag);
        mReqQueue.add(jsonArrayRequest);
    }

    private void postNewVote(int vote, String pic_id) {
        String url = "https://api.thedogapi.com/v1/votes";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("image_id", pic_id);
            jsonObject.put("sub_id", userId);
            jsonObject.put("value", vote);
        } catch (JSONException je) {
            Log.d("bsr", "POST BODY ERROR: " + je);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    Log.d("bsr", response.toString());
                }, error -> {
                    Log.d("bsr", "RESPONSE ERROR: " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-api-key", API_KEY);
                params.put("Content-Type", "application/json");
                //Log.d("bsr", "getHeaders");
                return params;
            }
        };
        jsonObjectRequest.setTag(tag);
        mReqQueue.add(jsonObjectRequest);
    }

    private void getAllBreeds() {
        String url = "https://api.thedogapi.com/v1/breeds?api_key=" + API_KEY;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                response -> {
                    //Iterates through elements in response array and displays in TextView
                    for (int i = 0; i < response.length(); i++){
                        try {
                            //Gets jsonObject at index i
                            JSONObject jsonObject = response.getJSONObject(i);

                            //gets date from jsonObject by key name
                            String id = jsonObject.getString("id");
                            String name = jsonObject.getString("name");
                        } catch (JSONException je) {
                            Log.d("bsr", "JSON ERROR: " + je);
                        }
                    }
                }, error -> {
            // On error in parsing logs the error
            Log.d("bsr", "RESPONSE ERROR: " + error.toString());
        }
        );

        jsonArrayRequest.setTag(tag);
        mReqQueue.add(jsonArrayRequest);
    }

    private class SwipeAdapter extends ArrayAdapter<PictureItem> {
        private ArrayList<PictureItem> picture_items;

        public SwipeAdapter(Context context, int textViewResourceId, ArrayList<PictureItem> picture_items) {
            super(context, textViewResourceId, picture_items);
            this.picture_items = picture_items;
        }

        @Override
        public int getCount() {
            return picture_items.size();
        }

        @Nullable
        @Override
        public PictureItem getItem(int position) {
            return picture_items.get(position);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater viewInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = viewInflater.inflate(R.layout.picture_item, parent, false);
            }
            PictureItem displayItem = getItem(position);
            if (displayItem != null) {
                ImageView image_view = view.findViewById(R.id.swipe_picture);
                Glide.with(MainActivity.this)
                        .load(displayItem.getUrl())
                        .centerCrop()
                        .placeholder(R.drawable.ic_baseline_cached_24)
                        .into(image_view);
                TextView name = view.findViewById(R.id.swipe_name);
                TextView job = view.findViewById(R.id.swipe_job);
                TextView job_category = view.findViewById(R.id.swipe_job_category);
                TextView temperament = view.findViewById(R.id.swipe_temperament);
                TextView lifespan = view.findViewById(R.id.swipe_lifespan);
                name.setText(displayItem.getName());
                job.setText(displayItem.getJob());
                job_category.setText(displayItem.getJobCategory());
                temperament.setText(displayItem.getTemperament());
                lifespan.setText(displayItem.getLifespan());
            }
            return view;
        }
    }
}