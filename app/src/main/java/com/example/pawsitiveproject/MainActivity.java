package com.example.pawsitiveproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final String tag = "api-req";
    private Boolean firstPicture = true;
    private RequestQueue mReqQueue;
    private final String API_KEY = "2fc6b7da-314a-4327-9888-0fd52d813f7f";

    private ArrayList<PictureItem> image_queue;
    private SwipeAdapter swipeAdapter;
    private SwipeFlingAdapterView flingContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mReqQueue = Volley.newRequestQueue(this);
        image_queue = new ArrayList<PictureItem>();
        PictureItem init_item = new PictureItem("iNX25KsSX", "https://cdn2.thedogapi.com/images/iNX25KsSX.jpg");
        image_queue.add(init_item);
        //getRandomPicture();

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
                Log.d("bsr", "<---swipe left---");
            }

            @Override
            public void onRightCardExit(Object o) {
                Log.d("bsr", "---swipe right--->");
            }

            @Override
            public void onAdapterAboutToEmpty(int i) {
                Log.d("bsr", "AdapterAboutToEmpty");
                getRandomPicture();
                swipeAdapter.notifyDataSetChanged();

            }

            @Override
            public void onScroll(float v) {
                //Log.d("bsr", "onScroll");
            }
        });
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
        String url = "https://api.thedogapi.com/v1/images/search?mime_types=jpg,png&api_key=" + API_KEY;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                response -> {
                    //Iterates through elements in response array and displays in TextView
                    //Log.d("bsr", response.toString());
                    for (int i = 0; i < response.length(); i++){
                        try {
                            //Gets jsonObject at index i
                            JSONObject jsonObject = response.getJSONObject(i);
                            //gets date from jsonObject by key name
                            String id = jsonObject.getString("id");
                            String pictureUrl = jsonObject.getString("url");
                            Log.d("bsr", "url: " + pictureUrl);
                            PictureItem newItem = new PictureItem(id, pictureUrl);
                            image_queue.add(newItem);
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
                        .placeholder(R.drawable.ic_baseline_cached_24)
                        .into(image_view);
            }
            return view;
        }
    }
}