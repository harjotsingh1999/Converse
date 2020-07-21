package com.example.converse.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.converse.HelperClasses.UserInformation;
import com.example.converse.R;
import com.example.converse.Utility.Constants;
import com.example.converse.Utility.SharedPref;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
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

import java.util.Objects;
import java.util.concurrent.CompletionService;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    public static final int REQUEST_PERMISSION_RESULT=20;

    MaterialToolbar settingsToolbar;
    ImageView userImageView;
    ImageButton selectImageButton;
    MaterialButton updateSettingsButton;
    TextInputLayout userNameLayout;
    TextInputEditText userNameEditText;
    TextView userPhoneTextView;
    ProgressDialog progressDialog;
    String userPhone,userName, userImageUrl;
    boolean isNameChanged=false, isImageChanged=false;


    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    Boolean isImageUploaded=false, isUserUpdated=false, isUserUpdatedInDatabase=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsToolbar=findViewById(R.id.settings_activity_toolbar);
        settingsToolbar.setTitle("Settings");
        setSupportActionBar(settingsToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        userImageView=findViewById(R.id.user_profile_image);
        selectImageButton=findViewById(R.id.settings_select_image_button);
        userNameLayout=findViewById(R.id.settings_activity_user_name_layout);
        userNameEditText=findViewById(R.id.settings_user_name_edit_text);
        userPhoneTextView=findViewById(R.id.settings_phone_number_text_view);
        updateSettingsButton=findViewById(R.id.settings_activity_update_button);
        progressDialog=new ProgressDialog(this);

        userImageUrl=SharedPref.getSharedPref(this).getString(Constants.KEY_USER_PROFILE_IMAGE_URL,"");
        Picasso.get().load(Uri.parse(userImageUrl)).placeholder(getDrawable(R.drawable.user_profile_image)).into(userImageView);
        userName=SharedPref.getSharedPref(this).getString(Constants.KEY_USER_NAME, "");
        userNameEditText.setText(userName);
        userPhone=SharedPref.getSharedPref(this).getString(Constants.KEY_USER_MOBILE_NUMBER,"");
        userPhoneTextView.setText(userPhone);

        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference().child("userProfileImages").child(firebaseUser.getUid()+"_profileImage_"+System.currentTimeMillis());
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("users");


        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions();
            }
        });

        updateSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateUserName()) {
                    isNameChanged = !userName.equals(userNameEditText.getText().toString().trim());
                    if (isImageChanged || isNameChanged) {
                        //either image and name changed update storage, authentication, and database
                        Log.e(TAG, "onClick: either or both image and name changed");
                        userName=userNameEditText.getText().toString().trim();
                        if(isImageChanged) {
                            if (!isImageUploaded) //if image is not uploaded then nothing is done
                                uploadImage();
                            else if (!isUserUpdated)   //this means that image is uploaded but user profile is not updated
                                uploadUserDetails();
                            else if (!isUserUpdatedInDatabase)  //this means only adding user to database remains
                                addUserToDatabase();
                        }
                        else
                        {
                            isImageUploaded=true;
                            if (!isUserUpdated)   //this means that image is uploaded but user profile is not updated
                                uploadUserDetails();
                            else if (!isUserUpdatedInDatabase)  //this means only adding user to database remains
                                addUserToDatabase();
                        }
                    }
                    else
                     {
                         Log.e(TAG, "onClick: nothing changed");
                        Toast.makeText(SettingsActivity.this, "No changes made", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                        finish();
                     }
                }
            }
        });
    }
    private void uploadImage()
    {
        if(!isImageUploaded) {
            Log.d(TAG, "uploadImage: uploading user image");
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            UploadTask uploadTask = (UploadTask) storageReference.putFile(Uri.parse(userImageUrl))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG, "onSuccess: Image upload successful " + taskSnapshot);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: image upload failed " + e.getMessage());
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
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
                        userImageUrl = task.getResult().toString();
                        Log.e(TAG, "onComplete: got uploaded image url " + userImageUrl);
                        isImageUploaded = true;
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
        if(isImageUploaded && !isUserUpdated)
        {
            if(!progressDialog.isShowing())
            {
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Please wait...");
                progressDialog.show();
            }
            Log.d(TAG, "uploadUserDetails: updating user details image url and name= " +userImageUrl+" "+userName);

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(userName)
                    .setPhotoUri(Uri.parse(userImageUrl))
                    .build();

            firebaseUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User profile updated. "+task.getResult());
                                isUserUpdated=true;
                                addUserToDatabase();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this,"Please try again", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: user could not be updated "+e.getMessage());
                }
            });
        }

    }

    private void addUserToDatabase()
    {
        if(!isUserUpdatedInDatabase && isImageUploaded && isUserUpdated)
        {
            Log.d(TAG, "addUserToDatabase: adding user to database");
            UserInformation userInformation=new UserInformation(userPhone,userName,userImageUrl);
            Log.d(TAG, "addUserToDatabase: adding user to database items="+userInformation.getProfileImageUrl()+" "+userInformation.getUserName());
            databaseReference.child(firebaseUser.getUid())
                    .setValue(userInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG, "onComplete: user added to database task= "+task.getResult());
                    SharedPref.getSharedPrefEditor(SettingsActivity.this).putString(Constants.KEY_USER_NAME,userName).apply();
                    SharedPref.getSharedPrefEditor(SettingsActivity.this).putString(Constants.KEY_USER_PROFILE_IMAGE_URL,userImageUrl).apply();
                    progressDialog.dismiss();
                    isUserUpdatedInDatabase=true;
                    Toast.makeText(SettingsActivity.this,"Updated", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this,"Please try again", Toast.LENGTH_SHORT).show();
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
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
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
            Toast.makeText(this,"Permission is required to function", Toast.LENGTH_SHORT).show();
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
                .start(this);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                userImageUrl = result.getUri().toString();
                isImageChanged = true;
                Picasso.get().load(Uri.parse(userImageUrl)).into(userImageView);
                Log.d(TAG, "onActivityResult: uri of file " + userImageUrl);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, "onActivityResult: error thrown " + error.getMessage());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onBackPressed();
        return super.onOptionsItemSelected(item);

    }
}