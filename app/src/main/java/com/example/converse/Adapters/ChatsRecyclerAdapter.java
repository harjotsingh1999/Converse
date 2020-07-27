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
import com.example.converse.HelperClasses.ChatItem;
import com.example.converse.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChatsRecyclerAdapter extends RecyclerView.Adapter<ChatsRecyclerAdapter.ChatViewHolder> {

    private static final String TAG = "ChatsRecyclerAdapter";
    Context context;
    ArrayList<ChatItem> chatItemArrayList;

    public ChatsRecyclerAdapter(Context context, ArrayList<ChatItem> chatItemArrayList) {
        this.context = context;
        this.chatItemArrayList = chatItemArrayList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.layout_chat_item,parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, final int position) {

        Log.d(TAG, "onBindViewHolder: "+position);
        Picasso.get().load(Uri.parse(chatItemArrayList.get(position).getDisplayImage())).placeholder(R.drawable.user_profile_image).into(holder.profileImage);
        holder.userName.setText(chatItemArrayList.get(position).getDisplayName());
        holder.lastMessage.setText(" ");


        holder.chatItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, ChatActivity.class);
                intent.putExtra("chatId", chatItemArrayList.get(position).getChatId());
                intent.putExtra("chatName", chatItemArrayList.get(position).getDisplayName());
                context.startActivity(intent);
            }
        });

        Log.d(TAG, "onBindViewHolder: "+position);
    }

    @Override
    public int getItemCount() {
        return chatItemArrayList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{

        ImageView profileImage;
        TextView userName, lastMessage;
        LinearLayout chatItem;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage=itemView.findViewById(R.id.chat_item_image_view);
            userName=itemView.findViewById(R.id.chat_item_name_text_view);
            lastMessage=itemView.findViewById(R.id.chat_item_last_message_text_view);
            chatItem=itemView.findViewById(R.id.chat_item_layout);
        }
    }
}
