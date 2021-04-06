package com.example.pawsitiveproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class FriendSearchActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText txt_search_friend;
    private ListView potential_friends_list;

    private ArrayList<User> potential_friends;
    private FriendSearchAdapter friendSearchAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_search);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txt_search_friend = findViewById(R.id.txt_search_friend);
        potential_friends_list = findViewById(R.id.potential_friends_list);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        potential_friends = new ArrayList<User>();
        friendSearchAdapter = new FriendSearchAdapter(this, R.layout.friend_search_item, potential_friends);
        potential_friends_list.setAdapter(friendSearchAdapter);

        txt_search_friend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                potential_friends.clear();
                String input = editable.toString();

                mDatabase.child("users").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.getValue() != null) {
                            Gson gson = new Gson();
                            for (DataSnapshot userSnapshot: snapshot.getChildren()) {
                                String uid = userSnapshot.getKey();
                                User newUser = gson.fromJson(userSnapshot.getValue().toString(), User.class);
                                newUser.setUid(uid);
                                if (newUser.getEmail().contains(input) && !newUser.getUid().equals(currentUser.getUid())) {
                                    potential_friends.add(newUser);
                                    friendSearchAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError de) {
                        Log.d("bsr", "DB ERROR: " + de);
                    }
                });
            }
        });
    }

    private class FriendSearchAdapter extends ArrayAdapter<User> {
        private ArrayList<User> users;

        public FriendSearchAdapter(Context context, int textViewResourceId, ArrayList<User> users) {
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
                TextView display_email = view.findViewById(R.id.friend_email);
                display_email.setText(user.getEmail());
                Button btn_send_request = view.findViewById(R.id.friend_btn);
                btn_send_request.setText("Send Request");
                btn_send_request.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("bsr", "Send to " + user.getUid() + " - " + user.getEmail());
                        mDatabase.child("friend_requests").child(currentUser.getUid())
                                .child(user.getUid()).setValue("sent");
                        mDatabase.child("friend_requests").child(user.getUid())
                                .child(currentUser.getUid()).setValue("received");
                        btn_send_request.setEnabled(false);
                    }
                });
            }
            return view;
        }
    }
}