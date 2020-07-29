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
import androidx.recyclerview.widget.RecyclerView;

import com.example.converse.HelperClasses.MessageItem;
import com.example.converse.R;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MessagesRecyclerAdapter extends RecyclerView.Adapter<MessagesRecyclerAdapter.MessagesViewHolder> {

    private static final String TAG = "MessagesRecyclerAdapter";

    public static final int VIEW_TYPE_SENDER=1;
    public static final int VIEW_TYPE_RECEIVER=2;

    Context context;
    ArrayList<MessageItem> messageItemArrayList;
    String currentUserId;

    public MessagesRecyclerAdapter(Context context, ArrayList<MessageItem> messageItemArrayList, String currentUserId) {
        this.context = context;
        this.messageItemArrayList = messageItemArrayList;
        this.currentUserId=currentUserId;
    }


    @Override
    public int getItemViewType(int position) {
        if(messageItemArrayList.get(position).getMessageSenderId().equals(currentUserId))
        {
            return VIEW_TYPE_SENDER;
        }
        return VIEW_TYPE_RECEIVER;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==VIEW_TYPE_SENDER)
        {
            Log.d(TAG, "onCreateViewHolder: called view type sender");
            View view= LayoutInflater.from(context).inflate(R.layout.layout_text_item_sender,parent, false);
            return new MessagesViewHolder(view, VIEW_TYPE_SENDER);
        }
        Log.d(TAG, "onCreateViewHolder: called view type receiver");
        View view= LayoutInflater.from(context).inflate(R.layout.layout_text_item_receiver,parent, false);
        return new MessagesViewHolder(view, VIEW_TYPE_RECEIVER);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
        if(holder.mViewType==VIEW_TYPE_SENDER)
        {
            Log.d(TAG, "onBindViewHolder: called viewType sender "+position);
            holder.senderText.setText(messageItemArrayList.get(position).getMessageText());

            if(messageItemArrayList.get(position).getMessageText().equals(" "))
                holder.senderText.setVisibility(View.GONE);

            DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getDefault());
            String text = formatter.format(new Date(Long.parseLong(messageItemArrayList.get(position).getMessageTimestamp())));

            holder.senderMessageTime.setText(text);

            if(messageItemArrayList.get(position).getMediaUri()!=null && !messageItemArrayList.get(position).getMediaUri().equals(""))
            {
                Picasso.get().load(Uri.parse(messageItemArrayList.get(position).getMediaUri())).into(holder.mediaImage);
                holder.mediaImage.setVisibility(View.VISIBLE);
            }

        }
        else
        {
            Log.d(TAG, "onBindViewHolder: called viewType receiver "+position);
            holder.receiverText.setText(messageItemArrayList.get(position).getMessageText());

            if(messageItemArrayList.get(position).getMessageText().equals(" "))
                holder.receiverText.setVisibility(View.GONE);

            DateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getDefault());
            String text = formatter.format(new Date(Long.parseLong(messageItemArrayList.get(position).getMessageTimestamp())));
            holder.receiverMessageTime.setText(text);

            if(messageItemArrayList.get(position).getMediaUri()!=null && !messageItemArrayList.get(position).getMediaUri().equals(""))
            {
                Picasso.get().load(Uri.parse(messageItemArrayList.get(position).getMediaUri())).into(holder.mediaImage);
                holder.mediaImage.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageItemArrayList.size();
    }

    static class MessagesViewHolder extends RecyclerView.ViewHolder{

        TextView senderText, senderMessageTime, receiverText, receiverMessageTime, senderNameTextView;
        ImageView mediaImage;
        int mViewType;
        public MessagesViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);

            this.mViewType=viewType;
            if(viewType==VIEW_TYPE_SENDER) {
                senderText = itemView.findViewById(R.id.item_sender_message_text);
                senderMessageTime = itemView.findViewById(R.id.item_sender_message_time);
                senderMessageTime=itemView.findViewById(R.id.sender_name_text_view);
                mediaImage=itemView.findViewById(R.id.media_image);
            }
            else {
                receiverText = itemView.findViewById(R.id.item_receiver_message_text);
                receiverMessageTime = itemView.findViewById(R.id.item_receiver_message_time);
                senderMessageTime=itemView.findViewById(R.id.sender_name_text_view);
                mediaImage=itemView.findViewById(R.id.media_image);
            }
        }
    }
}
