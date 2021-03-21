package com.example.pawsitiveproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    private final String API_KEY = "2fc6b7da-314a-4327-9888-0fd52d813f7f";
    private final String TAG = "api-req";

    private ArrayList<PictureItem> image_queue;
    private SwipeAdapter swipeAdapter;
    private SwipeFlingAdapterView flingContainer;
    private SharedPreferences sharedPreferences;
    private View view;

    private Boolean firstPicture = true;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        image_queue = new ArrayList<PictureItem>();
        getRandomPicture();
        getRandomPicture();
        getRandomPicture();

        flingContainer = (SwipeFlingAdapterView) view.findViewById(R.id.swipe_picture_frame);
        swipeAdapter = new SwipeAdapter(this.getContext(), R.layout.picture_item, image_queue);
        flingContainer.setAdapter(swipeAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                image_queue.remove(0);
                swipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object o) {
                PictureItem item = (PictureItem)o;
                postNewVote(0, item.getId());
            }

            @Override
            public void onRightCardExit(Object o) {
                PictureItem item = (PictureItem)o;
                postNewVote(1, item.getId());
            }

            @Override
            public void onAdapterAboutToEmpty(int i) {
                getRandomPicture();
            }

            @Override
            public void onScroll(float v) {}
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void getRandomPicture() {
        String url = "https://api.thedogapi.com/v1/images/search?mime_types=jpg,png&sub_id=" + currentUser.getUid();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
            response -> {
                Gson gson = new Gson();
                JSONObject jsonObject = null;
                String string = null;
                try {
                    jsonObject = response.getJSONObject(0);
                    string = jsonObject.toString();
                } catch (JSONException je) {
                    Log.e("bsr", "JSON ERROR: " + je);
                }
                RandomImageResponse imageResponse = gson.fromJson(string, RandomImageResponse.class);

                final String placeholder = "N/A";
                String bred_for = placeholder;
                String breed_group = placeholder;
                String name  = placeholder;
                String lifespan = placeholder;
                String temperament = placeholder;

                if (imageResponse.getBreeds().size() > 0) {
                    bred_for = imageResponse.getBreeds().get(0).getBred_for();
                    breed_group = imageResponse.getBreeds().get(0).getBreed_group();
                    lifespan = imageResponse.getBreeds().get(0).getLifespan();
                    name = imageResponse.getBreeds().get(0).getName();
                    temperament = imageResponse.getBreeds().get(0).getTemperament();
                }

                PictureItem newItem = new PictureItem(
                            imageResponse.getId(),
                            imageResponse.getUrl(),
                            bred_for,
                            breed_group,
                            lifespan,
                            name,
                            temperament
                );
                image_queue.add(newItem);
                swipeAdapter.notifyDataSetChanged();
            },
            error -> {
                Log.d("bsr", "RESPONSE ERROR: " + error.toString());
            }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-api-key", API_KEY);
                return params;
            }
        };

        jsonArrayRequest.setTag(TAG);
        SharedRequestQueue.getInstance(this.getContext()).addToRequestQueue(jsonArrayRequest);
    }

    private void postNewVote(int vote, String pic_id) {
        String url = "https://api.thedogapi.com/v1/votes";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("image_id", pic_id);
            jsonObject.put("sub_id", currentUser.getUid());
            jsonObject.put("value", vote);
        } catch (JSONException je) {
            Log.d("bsr", "POST BODY ERROR: " + je);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    //Log.d("bsr", response.toString());
                }, error -> {
                    Log.d("bsr", "RESPONSE ERROR: " + error.toString());
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-api-key", API_KEY);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        jsonObjectRequest.setTag(TAG);
        SharedRequestQueue.getInstance(this.getContext()).addToRequestQueue(jsonObjectRequest);
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
                LayoutInflater viewInflater = (LayoutInflater)getLayoutInflater();
                view = viewInflater.inflate(R.layout.picture_item, parent, false);
            }
            PictureItem displayItem = getItem(position);
            if (displayItem != null) {
                ImageView image_view = view.findViewById(R.id.swipe_picture);
                Glide.with(this.getContext())
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