package com.example.converse;

import android.app.Application;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.example.converse.HelperClasses.UserInformation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ConverseApplication extends Application {

    private static final String TAG = "ConverseApplication";
    public static HashMap<String, String> contactsMap=new HashMap<>();



    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: called");
        super.onCreate();

        getUserContacts();

    }

    private void getUserContacts() {
        Log.d(TAG, "getUserContacts: called");
        Cursor contact = getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null);
        assert contact != null;
        while (contact.moveToNext()) {
            String name = contact.getString(contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNum = contact.getString(contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneNum = phoneNum.replace(" ", "");
            phoneNum = phoneNum.replace("-", "");
            phoneNum = phoneNum.replace("(", "");
            phoneNum = phoneNum.replace(")", "");
            if (phoneNum.length() > 10)
                phoneNum = phoneNum.substring(phoneNum.length() - 10);

            if (phoneNum.length() == 10) {
               contactsMap.put(phoneNum,name);
            }
        }
        contact.close();
    }
}
