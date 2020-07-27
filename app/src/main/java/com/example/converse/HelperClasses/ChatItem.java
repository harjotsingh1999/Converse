package com.example.converse.HelperClasses;

public class ChatItem {

    String chatId, displayImage, displayName, lastMessage;

    public ChatItem(String chatId, String displayImage, String displayName, String lastMessage) {
        this.chatId = chatId;
        this.displayImage = displayImage;
        this.displayName = displayName;
        this.lastMessage = lastMessage;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getDisplayImage() {
        return displayImage;
    }

    public void setDisplayImage(String displayImage) {
        this.displayImage = displayImage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
