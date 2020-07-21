package com.example.converse.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.example.converse.Activities.HomeActivity;
import com.example.converse.Activities.LoginActivity;
import com.example.converse.R;
import com.example.converse.Utility.Constants;
import com.example.converse.Utility.SharedPref;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;


public class PhoneVerificationFragment extends Fragment {


    Context context;
    PinView pinView;
    TextView enterOTPTextView;
    MaterialButton goButton;
    ProgressBar progressBar;
    PhoneAuthProvider phoneAuthProvider;
    private static final String TAG = "PhoneVerificationFragme";
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    FirebaseAuth mAuth;
    String userPhoneNumber;

    public PhoneVerificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.e(TAG, "onCreateView: called" );
        phoneAuthProvider=PhoneAuthProvider.getInstance();
        return inflater.inflate(R.layout.fragment_phone_verification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.e(TAG, "onViewCreated: called" );
        pinView=view.findViewById(R.id.login_screen_pin_view);
        goButton=view.findViewById(R.id.login_screen_go_button);
        enterOTPTextView=view.findViewById(R.id.enter_otp_text_view);
        progressBar=view.findViewById(R.id.login_screen_progress_bar);
        mAuth=FirebaseAuth.getInstance();

        assert getArguments() != null;
        userPhoneNumber=getArguments().getString("phoneNumber", "");

        String string=enterOTPTextView.getText().toString()+" "+userPhoneNumber;
        enterOTPTextView.setText(string);

        Log.e(TAG, "onViewCreated: called phone Number= "+userPhoneNumber );

        progressBar.setVisibility(View.VISIBLE);
        goButton.setEnabled(false);
        pinView.setEnabled(false);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                userPhoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                (Activity)context,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }


    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:" + credential);

            progressBar.setVisibility(View.GONE);
            goButton.setEnabled(true);
            pinView.setEnabled(true);

            String code=credential.getSmsCode();
            pinView.setText(code);

            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.e(TAG, "onVerificationFailed", e);

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.e(TAG, "onVerificationFailed: invalid credentials "+e.getMessage() );
                Toast.makeText(context,"Invalid Phone Number", Toast.LENGTH_SHORT).show();
                FragmentManager fragmentManager=getFragmentManager();
                assert fragmentManager != null;
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                PhoneNumberFragment phoneNumberFragment=new PhoneNumberFragment();
                fragmentTransaction.replace(R.id.login_activity_container,phoneNumberFragment).disallowAddToBackStack().commit();
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.e(TAG, "onVerificationFailed: SMS quota exceeded "+e.getMessage() );
                Toast.makeText(context,"Please try later", Toast.LENGTH_SHORT).show();
                FragmentManager fragmentManager=getFragmentManager();
                assert fragmentManager != null;
                FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                PhoneNumberFragment phoneNumberFragment=new PhoneNumberFragment();
                fragmentTransaction.replace(R.id.login_activity_container,phoneNumberFragment).disallowAddToBackStack().commit();
            }

            // Show a message and update the UI
            else {
                Toast.makeText(context, "Please try again later", Toast.LENGTH_SHORT).show();
                FragmentManager fragmentManager = getFragmentManager();
                assert fragmentManager != null;
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                PhoneNumberFragment phoneNumberFragment = new PhoneNumberFragment();
                fragmentTransaction.replace(R.id.login_activity_container, phoneNumberFragment).disallowAddToBackStack().commit();
            }
        }

        @Override
        public void onCodeSent(@NonNull final String verificationId,
                @NonNull PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            progressBar.setVisibility(View.GONE);
            goButton.setEnabled(true);
            pinView.setEnabled(true);
            Log.d(TAG, "onCodeSent: verification id and token" + verificationId+" "+token);

            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;

            goButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String userEnteredPin=pinView.getText().toString();
                    if(userEnteredPin.length()!=6)
                    {
                        Toast.makeText(context,"Enter OTP",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(mVerificationId,userEnteredPin);
                        signInWithPhoneAuthCredential(credential);
                    }
                }
            });

        }
    };

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {

        Log.d(TAG, "signInWithPhoneAuthCredential: called with credential "+credential);
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context,"Sign In Successful", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onSuccess: sign in successful "+authResult.toString());


                FirebaseUser firebaseUser=mAuth.getCurrentUser();
                Log.e(TAG, "onSuccess: firebase user id "+firebaseUser.getUid()+ " "+firebaseUser.getDisplayName()+" "+firebaseUser.getPhotoUrl());
                if(firebaseUser.getDisplayName()==null || firebaseUser.getPhotoUrl()==null)
                {
                    Log.e(TAG, "onSuccess: new user signIn sending to new user fragment" );
                    SharedPref.getSharedPrefEditor(context).putString(Constants.KEY_USER_MOBILE_NUMBER,userPhoneNumber).apply();
                    FragmentManager fragmentManager=getFragmentManager();
                    assert fragmentManager != null;
                    FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
                    NewUserFragment newUserFragment=new NewUserFragment();
                    Bundle bundle=new Bundle();
                    bundle.putString("phoneNumber", userPhoneNumber);
                    newUserFragment.setArguments(bundle);
                    fragmentTransaction.replace(R.id.login_activity_container,newUserFragment).disallowAddToBackStack().commit();
                }
                else
                {
                    Toast.makeText(context,"Welcome", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onSuccess: old user sending to Home Activity" );
                    context.startActivity(new Intent(context, HomeActivity.class));
                    ((Activity)context).finish();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context,"Incorrect OTP", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: sign in failed "+e.getMessage());
            }
        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach: called" );
        super.onAttach(context);
        this.context=context;
    }
}