package com.example.pawsitiveproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private final String tag = "api-req";
    private RequestQueue mReqQueue;
    private final String API_KEY = "2fc6b7da-314a-4327-9888-0fd52d813f7f";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mReqQueue = Volley.newRequestQueue(this);
        getRandomImage();
    }

    private void getRandomImage() {
        String url = "https://api.thedogapi.com/v1/images/search?format=jpg&api_key=" + API_KEY;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Iterates through elements in response array and displays in TextView
                        for (int i = 0; i < response.length(); i++){
                            try {
                                //Gets jsonObject at index i
                                JSONObject jsonObject = response.getJSONObject(i);

                                //gets date from jsonObject by key name
                                String id = jsonObject.getString("id");
                                String pictureUrl = jsonObject.getString("url");
                                Log.d("bsr", "ID: " + id);
                                Log.d("bsr", "Url: " + pictureUrl);
                            } catch (JSONException je) {
                                Log.d("bsr", "JSON ERROR: " + je);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // On error in parsing logs the error
                Log.d("bsr", "RESPONSE ERROR: " + error.toString());
            }
        }
        );

        jsonArrayRequest.setTag(tag);
        mReqQueue.add(jsonArrayRequest);
    }

    private void getAllBreeds() {
        String url = "https://api.thedogapi.com/v1/breeds?api_key=" + API_KEY;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    //Iterates through elements in response array and displays in TextView
                    for (int i = 0; i < response.length(); i++){
                        try {
                            //Gets jsonObject at index i
                            JSONObject jsonObject = response.getJSONObject(i);

                            //gets date from jsonObject by key name
                            String id = jsonObject.getString("id");
                            String name = jsonObject.getString("name");
                            Log.d("bsr", "ID: " + id);
                            Log.d("bsr", "Name: " + name);
                        } catch (JSONException je) {
                            Log.d("bsr", "JSON ERROR: " + je);
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // On error in parsing logs the error
                    Log.d("bsr", "RESPONSE ERROR: " + error.toString());
                }
            }
        );

        jsonArrayRequest.setTag(tag);
        mReqQueue.add(jsonArrayRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // if queue is not empty cancels all the requests with given tag
        if (mReqQueue != null){
            mReqQueue.cancelAll(tag);
        }
    }
}