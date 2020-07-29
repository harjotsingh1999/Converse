package com.example.converse.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.mbms.MbmsErrors;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.converse.Adapters.MessagesRecyclerAdapter;
import com.example.converse.HelperClasses.MessageItem;
import com.example.converse.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";
    public static final int GALLERY_INTENT_REQUEST_CODE=2;

    String chatId, chatName;

    String imageUri;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference currentChatDatabaseReference;
    FirebaseStorage firebaseStorage;
    StorageReference currentChatStorageReference;

    MaterialToolbar chatActivityToolbar;
    TextView titleTextView;
    TextInputEditText messageEditText;
    ImageView mediaPreview;
    ImageButton sendMessageButton, addMediaButton;
    ProgressDialog progressDialog;
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
        firebaseStorage=FirebaseStorage.getInstance();
        currentChatStorageReference=firebaseStorage.getReference("chats").child(chatId);


        messageEditText = findViewById(R.id.message_edit_text);
        sendMessageButton = findViewById(R.id.message_text_send_button);
        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        mediaPreview=findViewById(R.id.media_preview);
        addMediaButton=findViewById(R.id.attach_media_button);
        progressDialog=new ProgressDialog(this);
        layoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        progressBar = findViewById(R.id.chat_activity_progress_bar);
        emptyLayout = findViewById(R.id.empty_chat_layout);
        messageItemArrayList = new ArrayList<>();

        progressBar.setVisibility(View.VISIBLE);
        messagesRecyclerView.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
        mediaPreview.setVisibility(View.GONE);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((messageEditText.getText() != null && !messageEditText.getText().toString().isEmpty()) || imageUri!=null) {
                    //send the message
                    sendMessage();
                    messageEditText.getText().clear();
                    mediaPreview.setVisibility(View.GONE);
                    imageUri=null;
                }
            }
        });

        addMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLERY_INTENT_REQUEST_CODE);
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

                    MessageItem messageItem;
                    if(snapshot.hasChild("mediaUri") && snapshot.child("mediaUri").getValue()!=null)
                    {
                        String mediaUri=snapshot.child("mediaUri").getValue().toString();
                        messageItem = new MessageItem(messageId, messageText, messageSenderId, messageTimestamp,mediaUri);
                    }
                    else
                    {
                        messageItem = new MessageItem(messageId, messageText, messageSenderId, messageTimestamp,null);
                    }

                    messageItemArrayList.add(messageItem);
                    if(messagesRecyclerAdapter!=null)
                    {
                        messagesRecyclerAdapter.notifyDataSetChanged();
                        messagesRecyclerView.smoothScrollToPosition(messagesRecyclerAdapter.getItemCount()-1);
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
            messagesRecyclerView.smoothScrollToPosition(messagesRecyclerAdapter.getItemCount()-1);
        }
    }

    private void sendMessage() {
        final String messageId = currentChatDatabaseReference.push().getKey();
        final String messageText;
        if(messageEditText.getText()!=null) {
            messageText = messageEditText.getText().toString().trim();
        }
        else
        {
            messageText=" ";
        }
        final String messageSenderId = firebaseUser.getUid();
        final String messageTimestamp = String.valueOf(System.currentTimeMillis());
        final MessageItem[] messageItem = new MessageItem[1];
        if(imageUri!=null)
        {
            //an image has to be uploaded
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Sending media");
            progressDialog.show();
            final String[] uploadedImageUrl = new String[1];
            final StorageReference currentImageStorageReference=currentChatStorageReference.child(messageId);
            UploadTask uploadTask=currentImageStorageReference.putFile(Uri.parse(imageUri));
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    currentImageStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d(TAG, "onSuccess: uploaded image url= "+uri.toString());
                            messageItem[0] =new MessageItem(messageText,messageSenderId,messageTimestamp,uri.toString());
                            currentChatDatabaseReference.child("messages").child(messageId).setValue(messageItem[0]).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "onFailure: send failed " + e.getMessage());
                                }
                            });

                            HashMap<String, String> map = new HashMap<>();
                            if (messageText.equals(" ")) {
                                map.put("lastMessage", "media");
                            } else {
                                map.put("lastMessage", messageText);
                            }
                            map.put("lastMessageTimestamp", messageTimestamp);
                            currentChatDatabaseReference.child("chatInfo").setValue(map).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "onFailure: update failed " + e.getMessage());
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.e(TAG, "onFailure: "+e.getMessage() );
                            Toast.makeText(getApplicationContext(),"Image Sending failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Log.e(TAG, "onFailure: "+e.getMessage() );
                    Toast.makeText(getApplicationContext(),"Image sending failed", Toast.LENGTH_SHORT).show();
                }
            });
            progressDialog.dismiss();
        }
        else
        {
            messageItem[0] = new MessageItem(messageText, messageSenderId, messageTimestamp);
            currentChatDatabaseReference.child("messages").child(messageId).setValue(messageItem[0]).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: send failed " + e.getMessage());
                }
            });

            HashMap<String, String> map = new HashMap<>();
            if (messageText.equals(" ")) {
                map.put("lastMessage", "media");
            } else {
                map.put("lastMessage", messageText);
            }
            map.put("lastMessageTimestamp", messageTimestamp);
            currentChatDatabaseReference.child("chatInfo").setValue(map).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "onFailure: update failed " + e.getMessage());
                }
            });
        }


        if (messagesRecyclerView != null && messagesRecyclerAdapter != null && messageItemArrayList != null)
            messagesRecyclerView.smoothScrollToPosition(messagesRecyclerAdapter.getItemCount()-1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK)
        {
            if(requestCode==GALLERY_INTENT_REQUEST_CODE && data!=null && data.getData()!=null)
            {
                imageUri=data.getData().toString();
                Picasso.get().load(Uri.parse(imageUri)).into(mediaPreview);
                mediaPreview.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}