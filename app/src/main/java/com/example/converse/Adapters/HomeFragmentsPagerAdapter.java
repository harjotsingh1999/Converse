package com.example.converse.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.converse.fragments.ContactsFragment;
import com.example.converse.fragments.ChatsFragment;
import com.example.converse.fragments.StatusFragment;

public class HomeFragmentsPagerAdapter extends FragmentPagerAdapter {

    public HomeFragmentsPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0:
                return new ChatsFragment();
            case 1:
                return new StatusFragment();
            case 2:
                return new ContactsFragment();
            default:
                return new ChatsFragment();
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
                return "Chats";
            case 1:
                return "Status";
            case 2:
                return "Contacts";
            default:
                return null;
        }
    }



    @Override
    public int getCount() {
        return 3;
    }
}
