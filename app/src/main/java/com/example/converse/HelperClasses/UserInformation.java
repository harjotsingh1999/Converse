package com.example.converse.HelperClasses;

import android.provider.Contacts;

public class UserInformation {

    public String phoneNumber, userName, profileImageUrl, userId;

    public UserInformation(String phoneNumber, String userName, String profileImageUrl) {
        this.phoneNumber = phoneNumber;
        this.userName = userName;
        this.profileImageUrl = profileImageUrl;
    }

    public UserInformation()
    {

    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
