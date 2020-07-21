package com.example.converse.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.net.UriCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.converse.Activities.HomeActivity;
import com.example.converse.HelperClasses.UserInformation;
import com.example.converse.R;
import com.example.converse.Utility.Constants;
import com.example.converse.Utility.SharedPref;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static android.app.Activity.RESULT_OK;

public class NewUserFragment extends Fragment {


    public static final int REQUEST_PERMISSION_RESULT=10;

    Context context;
    private static final String TAG = "NewUserFragment";
    TextInputLayout userNameLayout;
    TextInputEditText userNameEditText;
    MaterialButton getStartedButton;
    ImageButton selectImageButton;
    ImageView userProfileImageView;

    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseStorage firebaseStorage;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDatabaseReference;
    StorageReference storageReference;

    Uri profileImageUri;
    String userPhoneNumber;
    String userName;
    ProgressDialog progressDialog;

    Boolean imageUploaded=false, userUpdated=false, addedUserToDatabase=false;

    public NewUserFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: called");

        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        return inflater.inflate(R.layout.fragment_new_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        storageReference = firebaseStorage.getReference("userProfileImages").child(firebaseUser.getUid() + "_profileImage.jpg");
        usersDatabaseReference = firebaseDatabase.getReference("users");

        userNameLayout = view.findViewById(R.id.user_name_layout);
        userNameEditText = view.findViewById(R.id.user_name_edit_text);
        userProfileImageView = view.findViewById(R.id.user_profile_image);
        selectImageButton = view.findViewById(R.id.select_image_button);
        getStartedButton = view.findViewById(R.id.login_screen_get_started_button);

        assert getArguments() != null;
        userPhoneNumber=getArguments().getString("phoneNumber");
        Log.d(TAG, "onViewCreated: called for user id and phone number "+firebaseUser.getUid() + userPhoneNumber);

        progressDialog=new ProgressDialog(context);

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions();
            }
        });

        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateUserName() && profileImageUri!=null)
                {
                    userName=userNameEditText.getText().toString().trim();
                    if(!imageUploaded) //if image is not uploaded then nothing is done
                        uploadImage();
                    else if(!userUpdated)   //this means that image is uploaded but user profile is not updated
                        uploadUserDetails();
                    else if(!addedUserToDatabase)  //this means only adding user to database remains
                        addUserToDatabase();
                }
                else
                {
                    Toast.makeText(context,"Please add Image and Name", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void uploadImage()
    {
        if(!imageUploaded) {
            Log.d(TAG, "uploadImage: uploading user image");
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Uploading image...");
            progressDialog.show();

            UploadTask uploadTask = (UploadTask) storageReference.putFile(profileImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG, "onSuccess: Image upload successful " + taskSnapshot.toString());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: image upload failed " + e.getMessage());
                            progressDialog.dismiss();
                            Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show();
                        }
                    });


            final Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        profileImageUri = task.getResult();
                        Log.e(TAG, "onComplete: got uploaded image url" + profileImageUri.toString());
                        imageUploaded = true;
                        uploadUserDetails();
                    } else {
                        progressDialog.dismiss();
                        //Toast.makeText(context, "Error getting image url", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "onComplete: error getting uploaded image url");
                    }
                }
            });
        }
    }

    private void uploadUserDetails()
    {
        if(imageUploaded && !userUpdated)
        {
            progressDialog.setMessage("Updating user details...");
            Log.d(TAG, "uploadUserDetails: updating user details image url and name= " +profileImageUri+" "+userName);

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(userName)
                    .setPhotoUri(profileImageUri)
                    .build();

            firebaseUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User profile updated. "+task.getResult());
                                userUpdated=true;
                                addUserToDatabase();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(context,"Please try again", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: user could not be updated "+e.getMessage());
                }
            });
        }

    }

    private void addUserToDatabase()
    {
        if(!addedUserToDatabase && imageUploaded && userUpdated)
        {
            progressDialog.setMessage("Adding user to database...");
            Log.d(TAG, "addUserToDatabase: adding user to database");
            UserInformation userInformation=new UserInformation(userPhoneNumber,userName,profileImageUri.toString());
            usersDatabaseReference.child(firebaseUser.getUid())
                    .setValue(userInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "onComplete: user added to database");
                            SharedPref.getSharedPrefEditor(context).putString(Constants.KEY_USER_NAME,userName).apply();
                            SharedPref.getSharedPrefEditor(context).putString(Constants.KEY_USER_PROFILE_IMAGE_URL,profileImageUri.toString()).apply();
                            progressDialog.dismiss();
                            addedUserToDatabase=true;
                            Toast.makeText(context,"Welcome", Toast.LENGTH_SHORT).show();
                            context.startActivity(new Intent(context, HomeActivity.class));
                            ((Activity)context).finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(context,"Please try again", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "onFailure: user could not be added to database "+e.getMessage() );
                        }
                    });
        }
    }

    private boolean validateUserName()
    {
        String name=userNameEditText.getText().toString();
        if(name.equals(""))
        {
            userNameLayout.setBoxStrokeErrorColor(getResources().getColorStateList(R.color.colorPrimaryDark,null));
            userNameLayout.setError("Enter name");
            return false;
        }
        return true;
    }

    public void checkPermissions()
    {
        if(ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION_RESULT);
        }
        else
        {
            selectImageFromGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_PERMISSION_RESULT && grantResults[0]==PackageManager.PERMISSION_DENIED)
        {
            Toast.makeText(context,"Permission is required to function", Toast.LENGTH_SHORT).show();
        }
        else
        {
            selectImageFromGallery();
        }
    }


    private void selectImageFromGallery()
    {
        Log.d(TAG, "selectImageFromGallery: called");
        CropImage.activity().setCropShape(CropImageView.CropShape.OVAL)
                .setActivityTitle("Converse")
                .setFixAspectRatio(true)
                .setAspectRatio(1,1)
                .start(getContext(), this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context=context;
        Log.d(TAG, "onAttach: called");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                profileImageUri = result.getUri();
                Picasso.get().load(profileImageUri).into(userProfileImageView);
                Log.d(TAG, "onActivityResult: uri of file "+ profileImageUri.toString());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, "onActivityResult: error thrown "+error.getMessage());
            }
        }
    }
}