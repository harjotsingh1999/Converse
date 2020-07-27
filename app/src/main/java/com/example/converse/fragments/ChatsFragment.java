package com.example.converse.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.converse.Adapters.ChatsRecyclerAdapter;
import com.example.converse.Adapters.ContactsRecyclerAdapter;
import com.example.converse.ConverseApplication;
import com.example.converse.HelperClasses.ChatItem;
import com.example.converse.HelperClasses.UserInformation;
import com.example.converse.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.security.interfaces.DSAKey;
import java.util.ArrayList;

import static android.view.View.GONE;

public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragment";
    Context context;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference chatsDatabaseReference;
    DatabaseReference currentUserChatsDatabaseReference;

    LinearLayout emptyView;
    RecyclerView recyclerView;
    ProgressBar progressBar;

    ChatsRecyclerAdapter chatsRecyclerAdapter;
    RecyclerView.LayoutManager layoutManager;

    ArrayList<ChatItem> chatItemArrayList;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: called");

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        chatsDatabaseReference = firebaseDatabase.getReference("chats");
        currentUserChatsDatabaseReference = firebaseDatabase.getReference("users").child(firebaseUser.getUid()).child("chats");

        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: called");
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.chats_fragment_progress_bar);
        emptyView = view.findViewById(R.id.chats_fragment_empty_layout);
        recyclerView = view.findViewById(R.id.chats_fragment_recycler_view);

        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(GONE);
        recyclerView.setVisibility(GONE);

        chatItemArrayList = new ArrayList<>();

        layoutManager = new LinearLayoutManager(context);

        getUserChatsFromDatabase();
    }

    private void getUserChatsFromDatabase() {

        Log.d(TAG, "getUserChatsFromDatabase: called");
        currentUserChatsDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot chatDataSnapshot : snapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: get user chats from database " + chatDataSnapshot);
                        String chatId = chatDataSnapshot.getKey();
                        assert chatId != null;
                        String chatDisplayName;
                        String profileImageUrl=chatDataSnapshot.child("profileImageUrl").getValue().toString();
                        String phone = chatDataSnapshot.child("phoneNumber").getValue().toString();
                        if (ConverseApplication.contactsMap.containsKey(phone)) {
                            chatDisplayName = chatDataSnapshot.child("userName").getValue().toString();
                        } else if (ConverseApplication.contactsMap.containsKey(phone.substring(3))) {
                            chatDisplayName = chatDataSnapshot.child("userName").getValue().toString();
                        } else {
                            chatDisplayName = phone;
                        }
                        ChatItem chatItem = new ChatItem(chatId, profileImageUrl, chatDisplayName, " ");
                        chatItemArrayList.add(chatItem);
                    }

                    if (chatsRecyclerAdapter != null) {
                        chatsRecyclerAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                        setUpRecyclerView();
                    }
                }
                else
                {
                    setUpRecyclerView();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setUpRecyclerView() {
        Log.d(TAG, "setUpRecyclerView: called");
        progressBar.setVisibility(GONE);
        if (chatItemArrayList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(GONE);
        } else {
            emptyView.setVisibility(GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(layoutManager);
            chatsRecyclerAdapter = new ChatsRecyclerAdapter(context, chatItemArrayList);
            recyclerView.setAdapter(chatsRecyclerAdapter);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach: called");
        super.onAttach(context);
        this.context = context;
    }
}