package com.example.pawsitiveproject;

import android.content.Context;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class VotesFragment extends Fragment {
    private final String API_KEY = "2fc6b7da-314a-4327-9888-0fd52d813f7f";
    private final String TAG = "api-req";

    private ArrayList<Vote> all_votes;
    private VoteAdapter voteAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        return inflater.inflate(R.layout.fragment_votes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        all_votes = new ArrayList<Vote>();
        voteAdapter = new VoteAdapter(this.getContext(), R.layout.vote_list_item, all_votes);
        ListView vote_list = view.findViewById(R.id.vote_list);
        vote_list.setAdapter(voteAdapter);
        getAllVotes();
    }

    private void getAllVotes() {
        String url = "https://api.thedogapi.com/v1/votes?sub_id="+ currentUser.getUid();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
            response -> {
                Log.d("bsr", String.valueOf(response.length()));
                for (int i = 0; i < response.length(); i++) {
                    Gson gson = new Gson();
                    JSONObject jsonObject = null;
                    String string = null;
                    try {
                        jsonObject = response.getJSONObject(i);
                        string = jsonObject.toString();
                    } catch (JSONException je) {
                        Log.e("bsr", "JSON ERROR: " + je);
                    }
                    Vote newVote = gson.fromJson(string, Vote.class);
                    Log.d("bsr", i + ": vote == " + newVote.toString());
                    if (i == response.length() - 1) {
                        getImageURL(newVote, true);
                    } else {
                        getImageURL(newVote, false);
                    }
                }
                Log.d("bsr", "Vote Loop Over");
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

    private void getImageURL(Vote newVote, boolean lastVote) {
        String url = "https://api.thedogapi.com/v1/images/" + newVote.getImage_id();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, (Response.Listener<JSONObject>) response -> {
            Gson gson = new Gson();
            String string = response.toString();
            RandomImageResponse imageResponse = gson.fromJson(string, RandomImageResponse.class);
            newVote.setImage_url(imageResponse.getUrl());
            all_votes.add(newVote);
            Log.d("bsr", "Image URL == " + newVote.getImage_url());
            if (lastVote == true) {
                sortVotes();
                voteAdapter.notifyDataSetChanged();
            }
        }, error -> {
            Log.d("bsr", "RESPONSE ERROR: " + error.toString());
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-api-key", API_KEY);
                return params;
            }
        };

        jsonObjectRequest.setTag(TAG);
        SharedRequestQueue.getInstance(this.getContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void sortVotes() {
        Collections.sort(all_votes, new Comparator<Vote>() {
            @Override
            public int compare(Vote vote, Vote v1) {
                return vote.getCreated_at().compareTo(v1.getCreated_at());
            }
        });
        Collections.reverse(all_votes);
    }

    private class VoteAdapter extends ArrayAdapter<Vote> {
        private ArrayList<Vote> votes;

        public VoteAdapter(Context context, int textViewResourceId, ArrayList<Vote> votes) {
            super(context, textViewResourceId, votes);
            this.votes = votes;
        }

        @Override
        public int getCount() { return votes.size(); }

        @Nullable
        @Override
        public Vote getItem(int position) { return votes.get(position); }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                LayoutInflater viewInflater = (LayoutInflater)getLayoutInflater();
                view = viewInflater.inflate(R.layout.vote_list_item, null);
            }
            Vote thisVote = getItem(position);
            if (thisVote != null) {
                LinearLayout vote_layout = view.findViewById(R.id.vote_layout);
                if (thisVote.getValue() == 1) {
                    vote_layout.setBackgroundColor(Color.parseColor("#7ff0a6"));
                } else {
                    vote_layout.setBackgroundColor(Color.parseColor("#e37f7f"));
                }
                ImageView img_vote_dog = view.findViewById(R.id.img_vote_dog);
                Glide.with(this.getContext())
                        .load(thisVote.getImage_url())
                        .centerCrop()
                        .placeholder(R.drawable.ic_baseline_cached_24)
                        .into(img_vote_dog);
                TextView vote_id = view.findViewById(R.id.vote_id);
                vote_id.setText(String.valueOf(thisVote.getId()));
                TextView created_at = view.findViewById(R.id.vote_created_at);
                created_at.setText(thisVote.getCreated_at().toString());
                TextView vote_image_id = view.findViewById(R.id.vote_image_id);
                vote_image_id.setText(thisVote.getImage_id());
            }
            return view;
        }
    }
}