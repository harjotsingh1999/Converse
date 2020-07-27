package com.example.converse.HelperClasses;

public class MessageItem {

    String messageId, messageText, messageSenderId, messageTimestamp;

    public MessageItem() {
    }

    public MessageItem(String messageText, String messageSenderId, String messageTimestamp) {
        this.messageText = messageText;
        this.messageSenderId = messageSenderId;
        this.messageTimestamp = messageTimestamp;
    }

    public MessageItem(String messageId, String messageText, String messageSenderId, String messageTimestamp) {
        this.messageId = messageId;
        this.messageText = messageText;
        this.messageSenderId = messageSenderId;
        this.messageTimestamp = messageTimestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageSenderId() {
        return messageSenderId;
    }

    public void setMessageSenderId(String messageSenderId) {
        this.messageSenderId = messageSenderId;
    }

    public String getMessageTimestamp() {
        return messageTimestamp;
    }

    public void setMessageTimestamp(String messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }
}
