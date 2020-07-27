package com.example.converse.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.telephony.mbms.MbmsErrors;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.converse.Adapters.MessagesRecyclerAdapter;
import com.example.converse.HelperClasses.MessageItem;
import com.example.converse.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    String chatId, chatName;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference currentChatDatabaseReference;

    MaterialToolbar chatActivityToolbar;
    TextView titleTextView;
    TextInputEditText messageEditText;
    ImageButton sendMessageButton;
    RecyclerView messagesRecyclerView;
    RecyclerView.LayoutManager layoutManager;
    ProgressBar progressBar;
    LinearLayout emptyLayout;
    MessagesRecyclerAdapter messagesRecyclerAdapter;

    ArrayList<MessageItem> messageItemArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getStringExtra("chatId");
        chatName = getIntent().getStringExtra("chatName");
        Log.d(TAG, "onCreate: called with chatId and name " + chatId + " " + chatName);

        chatActivityToolbar = findViewById(R.id.chat_activity_toolbar);
        titleTextView = findViewById(R.id.chat_toolbar_title);
        titleTextView.setText(chatName);
        setSupportActionBar(chatActivityToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        currentChatDatabaseReference = firebaseDatabase.getReference().child("chats").child(chatId);


        messageEditText = findViewById(R.id.message_edit_text);
        sendMessageButton = findViewById(R.id.message_text_send_button);
        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        progressBar = findViewById(R.id.chat_activity_progress_bar);
        emptyLayout = findViewById(R.id.empty_chat_layout);
        messageItemArrayList = new ArrayList<>();

        progressBar.setVisibility(View.VISIBLE);
        messagesRecyclerView.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messageEditText.getText() != null && !messageEditText.getText().toString().isEmpty()) {
                    //send the message
                    sendMessage();
                    messageEditText.getText().clear();
                }
            }
        });

        getMessagesFromDatabase();
    }

    private void getMessagesFromDatabase() {
        Log.d(TAG, "getMessagesFromDatabase: called");
//        currentChatDatabaseReference.child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    Log.e(TAG, "onDataChange: "+snapshot);
//                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                        Log.d(TAG, "onDataChange: " + dataSnapshot);
//                        String messageId = dataSnapshot.getKey();
//                        String messageText = dataSnapshot.child("messageText").getValue().toString();
//                        String messageSenderId = dataSnapshot.child("messageSenderId").getValue().toString();
//                        String messageTimestamp = dataSnapshot.child("messageTimestamp").getValue().toString();
//
//                        MessageItem messageItem = new MessageItem(messageId, messageText, messageSenderId, messageTimestamp);
//                        messageItemArrayList.add(messageItem);
//                    }
//                }
//                setUpRecyclerView();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });


        currentChatDatabaseReference.child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //Log.d(TAG, "onChildAdded: " + snapshot);
                if (snapshot.exists()) {
                    Log.d(TAG, "onChildAdded: " + snapshot);
                    String messageId = snapshot.getKey();
                    String messageText = snapshot.child("messageText").getValue().toString();
                    String messageSenderId = snapshot.child("messageSenderId").getValue().toString();
                    String messageTimestamp = snapshot.child("messageTimestamp").getValue().toString();

                    MessageItem messageItem = new MessageItem(messageId, messageText, messageSenderId, messageTimestamp);
                    messageItemArrayList.add(messageItem);
                    if(messagesRecyclerAdapter!=null)
                    {
                        messagesRecyclerAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                        setUpRecyclerView();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Log.d(TAG, "onChildChanged: " + snapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                Log.d(TAG, "onChildChanged: " + snapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Log.d(TAG, "onChildChanged: " + snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {

//
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    private void setUpRecyclerView() {
        progressBar.setVisibility(View.GONE);
        if (messageItemArrayList.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            messagesRecyclerView.setVisibility(View.GONE);
        } else {
            messagesRecyclerView.setLayoutManager(layoutManager);
            emptyLayout.setVisibility(View.GONE);
            messagesRecyclerAdapter = new MessagesRecyclerAdapter(this, messageItemArrayList, firebaseUser.getUid());
            messagesRecyclerView.setAdapter(messagesRecyclerAdapter);
            messagesRecyclerView.setVisibility(View.VISIBLE);
            messagesRecyclerView.smoothScrollToPosition(messageItemArrayList.size() - 1);
        }
    }

    private void sendMessage() {
        String messageId = currentChatDatabaseReference.push().getKey();
        String messageText = messageEditText.getText().toString();
        String messageSenderId = firebaseUser.getUid();
        String messageTimestamp = String.valueOf(System.currentTimeMillis());

        MessageItem messageItem = new MessageItem(messageText, messageSenderId, messageTimestamp);
        currentChatDatabaseReference.child("messages").child(messageId).setValue(messageItem).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: send failed " + e.getMessage());
            }
        });

        HashMap<String, String> map = new HashMap<>();
        map.put("lastMessage", messageText);
        map.put("lastMessageTimestamp", messageTimestamp);
        currentChatDatabaseReference.child("chatInfo").setValue(map).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: update failed " + e.getMessage());
            }
        });

        if (messagesRecyclerView != null && messagesRecyclerAdapter != null && messageItemArrayList != null)
            messagesRecyclerView.smoothScrollToPosition(messageItemArrayList.size() - 1);
    }
}