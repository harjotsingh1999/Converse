package com.example.converse.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.converse.Activities.ChatActivity;
import com.example.converse.Activities.HomeActivity;
import com.example.converse.HelperClasses.UserInformation;
import com.example.converse.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ContactsRecyclerAdapter extends RecyclerView.Adapter<ContactsRecyclerAdapter.ContactsViewHolder> {

    private static final String TAG = "ContactsRecyclerAdapter";
    private ArrayList<UserInformation> appUserContacts;
    Context context;
    UserInformation currentUser;

    public ContactsRecyclerAdapter(ArrayList<UserInformation> appUserContacts, Context context, UserInformation currentUser) {
        this.appUserContacts = appUserContacts;
        this.context = context;
        this.currentUser=currentUser;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.layout_contact_item,parent,false);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: position "+position);
        UserInformation thisUser=appUserContacts.get(position);
        String imageUrl=thisUser.getProfileImageUrl();
        if(imageUrl!=null || !imageUrl.equals(""))
            Picasso.get().load(Uri.parse(imageUrl)).placeholder(R.drawable.user_profile_image).into(holder.contactImageView);
        holder.contactUserName.setText(thisUser.getUserName());
        holder.contactPhoneNumber.setText(thisUser.getPhoneNumber());


        holder.contactCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (HomeActivity.userChats != null && HomeActivity.userChats.containsKey(appUserContacts.get(position).getUserId())) {
                    Log.e(TAG, "onClick: user is already chatting with this person");
                    Intent chatIntent = new Intent(context, ChatActivity.class);
                    chatIntent.putExtra("chatId", HomeActivity.userChats.get(appUserContacts.get(position).getUserId()));
                    chatIntent.putExtra("chatName", appUserContacts.get(position).userName);
                    context.startActivity(chatIntent);
                } else {

                    Log.e(TAG, "onClick: new chat has to be created" );
                    String key = FirebaseDatabase.getInstance().getReference().child("chats").push().getKey();

                    if (FirebaseAuth.getInstance().getCurrentUser() != null && key != null) {
                        FirebaseDatabase.getInstance().getReference()
                                .child("users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child("chats")
                                .child(key)
                                .setValue(appUserContacts.get(position)).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure: " + e.getMessage());
                            }
                        });
                        // The above adds the chat id to the chats of the party who clicks the contact
                        FirebaseDatabase.getInstance().getReference()
                                .child("users")
                                .child(appUserContacts.get(position).getUserId())
                                .child("chats")
                                .child(key)
                                .setValue(currentUser).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure: " + e.getMessage());
                            }
                        });
                        //The above adds the chat id to the chats of the party whose contact has been clicked

                        Intent chatIntent = new Intent(context, ChatActivity.class);
                        chatIntent.putExtra("chatId", key);
                        chatIntent.putExtra("chatName", appUserContacts.get(position).userName);
                        context.startActivity(chatIntent);
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return appUserContacts.size();
    }



    static class ContactsViewHolder extends RecyclerView.ViewHolder{

        ImageView contactImageView;
        TextView contactUserName, contactPhoneNumber;
        LinearLayout contactCard;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            contactImageView=itemView.findViewById(R.id.contact_item_image_view);
            contactUserName=itemView.findViewById(R.id.contact_item_name_text_view);
            contactPhoneNumber=itemView.findViewById(R.id.contact_item_phone_text_view);
            contactCard=itemView.findViewById(R.id.contact_item_card_view);
        }
    }
}
