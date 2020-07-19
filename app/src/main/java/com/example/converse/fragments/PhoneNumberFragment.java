package com.example.converse.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.converse.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;


public class PhoneNumberFragment extends Fragment {


    Context context;
    CountryCodePicker countryCodePicker;
    TextInputLayout phoneLayout;
    MaterialButton verifyButton;
    TextInputEditText textInputEditText;
    private static final String TAG = "PhoneNumberFragment";

    public PhoneNumberFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: called");
        return inflater.inflate(R.layout.fragment_phone_number, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated: called");
        countryCodePicker=view.findViewById(R.id.login_screen_country_code_picker);
        phoneLayout=view.findViewById(R.id.phone_number_layout);
        verifyButton=view.findViewById(R.id.login_screen_verify_button);
        textInputEditText=view.findViewById(R.id.phone_number_edit_text);

        textInputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phoneLayout.isErrorEnabled())
                    phoneLayout.setError(null);
            }
        });

        phoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phoneLayout.isErrorEnabled())
                    phoneLayout.setError(null);
            }
        });


        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validatePhoneNumber())
                {
                    Log.d(TAG, "onClick: phone number correct");
                    String phoneNum=countryCodePicker.getSelectedCountryCodeWithPlus()+textInputEditText.getText().toString();
                    FragmentManager fragmentManager=getFragmentManager();
                    assert fragmentManager != null;
                    FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                    PhoneVerificationFragment phoneVerificationFragment=new PhoneVerificationFragment();
                    Bundle bundle=new Bundle();
                    bundle.putString("phoneNumber", phoneNum);
                    phoneVerificationFragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.login_activity_container,phoneVerificationFragment).disallowAddToBackStack().commit();
                }
            }
        });
    }

    private boolean validatePhoneNumber()
    {
        Log.d(TAG, "validatePhoneNumber: called");
        String phoneNumber=textInputEditText.getText().toString();
        if(phoneNumber.equals("")|| phoneNumber.length()!=10)
        {
            phoneLayout.setBoxStrokeErrorColor(getResources().getColorStateList(R.color.black,null));
            phoneLayout.setError("Enter Mobile Number");
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach: called");
        super.onAttach(context);
        this.context=context;
    }
}