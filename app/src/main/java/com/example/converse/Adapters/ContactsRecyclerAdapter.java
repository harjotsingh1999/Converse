package com.example.converse.Adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.converse.HelperClasses.UserInformation;
import com.example.converse.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ContactsRecyclerAdapter extends RecyclerView.Adapter<ContactsRecyclerAdapter.ContactsViewHolder> {

    private static final String TAG = "ContactsRecyclerAdapter";
    private ArrayList<UserInformation> appUserContacts;
    Context context;

    public ContactsRecyclerAdapter(ArrayList<UserInformation> appUserContacts, Context context) {
        this.appUserContacts = appUserContacts;
        this.context = context;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.layout_contact_item,parent,false);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: position "+position);
        UserInformation thisUser=appUserContacts.get(position);
        String imageUrl=thisUser.getProfileImageUrl();
        if(imageUrl!=null || !imageUrl.equals(""))
            Picasso.get().load(Uri.parse(imageUrl)).placeholder(R.drawable.user_profile_image).into(holder.contactImageView);
        holder.contactUserName.setText(thisUser.getUserName());
        holder.contactPhoneNumber.setText(thisUser.getPhoneNumber());
    }

    @Override
    public int getItemCount() {
        return appUserContacts.size();
    }



    static class ContactsViewHolder extends RecyclerView.ViewHolder{

        ImageView contactImageView;
        TextView contactUserName, contactPhoneNumber;
        CardView contactCard;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            contactImageView=itemView.findViewById(R.id.contact_item_image_view);
            contactUserName=itemView.findViewById(R.id.contact_item_name_text_view);
            contactPhoneNumber=itemView.findViewById(R.id.contact_item_phone_text_view);
            contactCard=itemView.findViewById(R.id.contact_item_card_view);
        }
    }
}
