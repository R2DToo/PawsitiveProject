package com.example.pawsitiveproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FriendsFragment extends Fragment {
    private Button btn_friends_list, btn_received_list, btn_sent_list;
    private ImageButton start_friend_search;
    private ListView friends_list_view;

    private ArrayList<User> user_list;
    private FriendAdapter friendAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn_friends_list = view.findViewById(R.id.btn_friends_list);
        btn_received_list = view.findViewById(R.id.btn_received_list);
        btn_sent_list = view.findViewById(R.id.btn_sent_list);
        start_friend_search = view.findViewById(R.id.start_friend_search);
        friends_list_view = view.findViewById(R.id.friends_list_view);

        btn_friends_list.setEnabled(false);

        user_list = new ArrayList<User>();
        friendAdapter = new FriendAdapter(this.getContext(), R.layout.friend_search_item, user_list);
        friends_list_view.setAdapter(friendAdapter);

        getFriends();

        start_friend_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent friendSearchIntent = new Intent(getContext(), FriendSearchActivity.class);
                startActivity(friendSearchIntent);
            }
        });
        btn_friends_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_friends_list.setEnabled(false);
                btn_received_list.setEnabled(true);
                btn_sent_list.setEnabled(true);
                getFriends();
            }
        });
        btn_received_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_friends_list.setEnabled(true);
                btn_sent_list.setEnabled(true);
                btn_received_list.setEnabled(false);
                mDatabase.child("friend_requests").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            user_list.clear();
                            Log.d("bsr", "Received List cleared. Size == " + user_list.size());
                            friendAdapter.notifyDataSetChanged();
                            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                                String friendUid = snapshot.getKey();
                                String friendStatus = snapshot.getValue().toString();
                                if(friendStatus.equals("received")) {
                                    //Log.d("bsr", "key: " + friendUid + " - value: " + friendStatus);
                                    getSpecificUser(friendUid, friendStatus);
                                }
                            }
                        } else {
                            Log.d("bsr", "DB ERROR: " + task.getException());
                        }
                    }
                });
            }
        });
        btn_sent_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_friends_list.setEnabled(true);
                btn_sent_list.setEnabled(false);
                btn_received_list.setEnabled(true);
                mDatabase.child("friend_requests").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            user_list.clear();
                            Log.d("bsr", "Sent List cleared. Size == " + user_list.size());
                            friendAdapter.notifyDataSetChanged();
                            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                                String friendUid = snapshot.getKey();
                                String friendStatus = snapshot.getValue().toString();
                                if(friendStatus.equals("sent")) {
                                    //Log.d("bsr", "key: " + friendUid + " - value: " + friendStatus);
                                    getSpecificUser(friendUid, friendStatus);
                                }
                            }
                        } else {
                            Log.d("bsr", "DB ERROR: " + task.getException());
                        }
                    }
                });
            }
        });
    }

    private void getFriends() {
        mDatabase.child("friends").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    user_list.clear();
                    friendAdapter.notifyDataSetChanged();
                    for (DataSnapshot snapshot : task.getResult().getChildren()) {
                        String friendUid = snapshot.getKey();
                        String friendStatus = snapshot.getValue().toString();
                        if(friendStatus.equals("true")) {
                            //Log.d("bsr", "key: " + friendUid + " - value: " + friendStatus);
                            getSpecificUser(friendUid, friendStatus);
                        }
                    }
                } else {
                    Log.d("bsr", "DB ERROR: " + task.getException());
                }
            }
        });
    }

    private void getSpecificUser(String uid, String status) {
        mDatabase.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    Gson gson = new Gson();
                    for (DataSnapshot userSnapshot: snapshot.getChildren()) {
                        User specificUser = new User(userSnapshot.getValue().toString());
                        specificUser.setUid(uid);
                        specificUser.setStatus(status);
                        Log.d("bsr", specificUser.toString());
                        user_list.add(specificUser);
                        friendAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError de) { Log.d("bsr", "DB ERROR: " + de); }
        });
    }

    private class FriendAdapter extends ArrayAdapter<User> {
        private ArrayList<User> users;

        public FriendAdapter(Context context, int textViewResourceId, ArrayList<User> users) {
            super(context, textViewResourceId, users);
            this.users = users;
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Nullable
        @Override
        public User getItem(int position) {
            return users.get(position);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                LayoutInflater viewInflater = (LayoutInflater)getLayoutInflater();
                view = viewInflater.inflate(R.layout.friend_search_item, null);
            }
            User user = getItem(position);
            if (user != null) {
                TextView friend_email = view.findViewById(R.id.friend_email);
                Button friend_btn = view.findViewById(R.id.friend_btn);
                if (user.getStatus().equals("sent")) {
                    friend_btn.setEnabled(false);
                    friend_btn.setText("Pending..");
                    friend_email.setText(user.getEmail());
                } else if (user.getStatus().equals("received")) {
                    friend_btn.setEnabled(true);
                    friend_btn.setText("Approve");
                    friend_email.setText(user.getEmail());
                    friend_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mDatabase.child("friends").child(currentUser.getUid()).child(user.getUid()).setValue(true);
                            mDatabase.child("friends").child(user.getUid()).child(currentUser.getUid()).setValue(true);
                            mDatabase.child("friend_requests").child(currentUser.getUid()).child(user.getUid()).removeValue();
                            mDatabase.child("friend_requests").child(user.getUid()).child(currentUser.getUid()).removeValue();
                        }
                    });
                } else if (user.getStatus().equals("true")) {
                    friend_btn.setEnabled(false);
                    friend_btn.setText("Chat");
                    friend_email.setText(user.getEmail());
                }
            }
            return view;
        }
    }
}