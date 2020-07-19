package com.example.converse.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toolbar;

import com.example.converse.Adapters.HomeFragmentsPagerAdapter;
import com.example.converse.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    MaterialToolbar toolbar;
    ViewPager homeViewPager;
    TabLayout homeTabLayout;
    HomeFragmentsPagerAdapter fragmentsPagerAdapter;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
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
    }


    @Override
    protected void onStart() {
        super.onStart();

        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();

        if(firebaseUser==null)
        {
            startActivity(new Intent(HomeActivity.this,LoginActivity.class));
            finish();
        }
    }
}