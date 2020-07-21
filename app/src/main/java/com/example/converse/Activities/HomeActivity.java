package com.example.converse.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.converse.Adapters.HomeFragmentsPagerAdapter;
import com.example.converse.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    public static final int REQUEST_CONTACTS_PERMISSION_CODE=10;
    MaterialToolbar toolbar;
    ViewPager homeViewPager;
    TabLayout homeTabLayout;
    HomeFragmentsPagerAdapter fragmentsPagerAdapter;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseAuth.AuthStateListener authStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar=findViewById(R.id.home_activity_toolbar);
        setSupportActionBar(toolbar);

        homeViewPager=findViewById(R.id.home_activity_viewpager);
        homeTabLayout=findViewById(R.id.home_Activity_tab_layout);

        fragmentsPagerAdapter=new HomeFragmentsPagerAdapter(getSupportFragmentManager());
        homeViewPager.setAdapter(fragmentsPagerAdapter);
        homeTabLayout.setupWithViewPager(homeViewPager);
        homeViewPager.setOffscreenPageLimit(2);

        authStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                if(user==null)
                {
                    mAuth.signOut();
                    Log.d(TAG, "onAuthStateChanged: successfully signed out");
                    sendUserToLoginActivity();
                }
                else
                {
                    Log.d(TAG, "onAuthStateChanged: user signed in with used id "+user.getUid());
                }
            }
        };

        checkPermissions();
    }


    @Override
    protected void onStart() {
        super.onStart();

        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();

        if(firebaseUser==null)
        {
            sendUserToLoginActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.options_menu,menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        int id=item.getItemId();
        switch (id)
        {
            case R.id.menu_settings:
                sendUserToSettingsActivity();
                return true;
            case R.id.menu_sign_out:
                mAuth.signOut();
                sendUserToLoginActivity();
                return true;
            default:
                return false;
        }
    }

    public void sendUserToLoginActivity()
    {
        Log.d(TAG, "sendUserToLoginActivity: called");
        startActivity(new Intent(HomeActivity.this,LoginActivity.class));
        finish();
    }
    public void sendUserToSettingsActivity()
    {
        Log.d(TAG, "sendUserToSettingsActivity: called");
        startActivity(new Intent(HomeActivity.this,SettingsActivity.class));
    }

    private void checkPermissions()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)== PackageManager.PERMISSION_DENIED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS},REQUEST_CONTACTS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}