package com.example.converse.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.converse.Adapters.ContactsRecyclerAdapter;
import com.example.converse.HelperClasses.UserInformation;
import com.example.converse.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


public class ContactsFragment extends Fragment {

    private static final String TAG = "ContactsFragment";
    public static final int REQUEST_CONTACTS_PERMISSION_CODE = 10;
    Context context;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDatabaseReference;
    DatabaseReference currentUserDatabaseReference;

    ArrayList<UserInformation> userContacts, appUserContacts;
    LinearLayout emptyView, noPermissionView;
    RecyclerView recyclerView;
    TextView givePermissionText;
    ProgressBar progressBar;

    ContactsRecyclerAdapter contactsRecyclerAdapter;
    RecyclerView.LayoutManager layoutManager;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");
        // Inflate the layout for this fragment
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDatabaseReference = firebaseDatabase.getReference("users");
        currentUserDatabaseReference = firebaseDatabase.getReference("users").child(firebaseUser.getUid());

        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: called");

        emptyView = view.findViewById(R.id.contacts_fragment_empty_layout);
        noPermissionView = view.findViewById(R.id.contacts_fragment_no_permission_layout);
        recyclerView = view.findViewById(R.id.contacts_fragment_recycler_view);
        progressBar = view.findViewById(R.id.contacts_fragment_progress_bar);
        givePermissionText = view.findViewById(R.id.contacts_fragment_give_permission_text_view);

        userContacts = new ArrayList<>();
        appUserContacts = new ArrayList<>();

        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        noPermissionView.setVisibility(View.GONE);

        checkPermissions();

        givePermissionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, REQUEST_CONTACTS_PERMISSION_CODE);
            }
        });
    }


    private void getUserContacts() {
        Log.d(TAG, "getUserContacts: called");
        Cursor contact = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
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
                UserInformation userInformation = new UserInformation();
                userInformation.setUserName(name);
                userInformation.setPhoneNumber(phoneNum);
                userContacts.add(userInformation);
                checkIfThisUserIsInDatabase(phoneNum, name);
            }
        }

        if (appUserContacts.isEmpty()) {
            Log.d(TAG, "getUserContacts: no app user found");
            emptyView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            setUpRecyclerView();
        }
    }

    private void checkIfThisUserIsInDatabase(final String phone, final String name) {
        Log.d(TAG, "checkIfThisUserIsInDatabase: with phone " + phone);

        usersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    //Log.d(TAG, "onDataChange: datasnapshot "+dataSnapshot.toString());
                    if (dataSnapshot.child("phoneNumber").getValue().toString().contains(phone)) {
                        Log.d(TAG, "onDataChange: this person uses app and is in contacts " + dataSnapshot.toString());
                        UserInformation userInformation = new UserInformation(dataSnapshot.child("phoneNumber").getValue().toString(),
                                name,
                                dataSnapshot.child("profileImageUrl").getValue().toString());
                        appUserContacts.add(userInformation);
                    }
                }
                setUpRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: database event listener cancelled " + error);
            }
        });
    }

    private void setUpRecyclerView() {
        Log.d(TAG, "setUpRecyclerView: called");
        contactsRecyclerAdapter = new ContactsRecyclerAdapter(appUserContacts, context);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(contactsRecyclerAdapter);
        recyclerView.setVisibility(View.VISIBLE);
        noPermissionView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        Log.d(TAG, "onAttach: called");
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            Log.d(TAG, "checkPermissions: permission not received");
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, REQUEST_CONTACTS_PERMISSION_CODE);
        } else {
            Log.d(TAG, "checkPermissions: permission accepted");
            progressBar.setVisibility(View.VISIBLE);
            getUserContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CONTACTS_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: permission given");
            getUserContacts();
        } else {
            Log.d(TAG, "checkPermissions: permission not given");
            noPermissionView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();
        appUserContacts.clear();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        Log.d(TAG, "onAttachFragment: called");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: called");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: called");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: called");
    }

    @Override
    public void onInflate(@NonNull Context context, @NonNull AttributeSet attrs, @Nullable Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        Log.d(TAG, "onInflate: called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called");
    }
}