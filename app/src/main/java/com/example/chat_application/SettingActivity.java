package com.example.chat_application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userPofileImage;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference RootRef;
    String currentUserID;
    private Toolbar toolbar;
    private ProgressDialog progressDialog,loadingBar;
    private static final int gallerypict=1;

    private StorageReference userProfileImagesReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        toolbar = findViewById(R.id.setting_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.show();

        loadingBar = new ProgressDialog(this);


            updateAccountSettings = findViewById(R.id.updateSetitngButton);
            userName =  findViewById(R.id.set_user_name);
            userStatus =  findViewById(R.id.set_user_status);
            userPofileImage =  findViewById(R.id.profile_image);


            firebaseAuth= FirebaseAuth.getInstance();

            currentUserID = firebaseAuth.getCurrentUser().getUid().toString();

        userProfileImagesReference = FirebaseStorage.getInstance().getReference("Profile Images");

            RootRef = FirebaseDatabase.getInstance().getReference();



            updateAccountSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        updateSetting();
                }
            });


            retrieveUserInformation();

            userPofileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent,gallerypict);
                }
            });
    }

    private void retrieveUserInformation() {

        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image"))
                        {
                            progressDialog.dismiss();
                            String Name = dataSnapshot.child("name").getValue().toString();
                            String Status = dataSnapshot.child("status").getValue().toString();
                            userName.setVisibility(View.INVISIBLE);
                                userName.setText(Name);
                                userStatus.setText(Status);
                            String image = dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(image).into(userPofileImage);
                        }
                        else if(dataSnapshot.exists() && dataSnapshot.hasChild("name"))
                        {
                            userName.setVisibility(View.INVISIBLE);
                            progressDialog.dismiss();
                            String Name = dataSnapshot.child("name").getValue().toString();
                            String Status = dataSnapshot.child("status").getValue().toString();

                            userName.setText(Name);
                            userStatus.setText(Status);


                        }
                        else
                        {
                            progressDialog.dismiss();
                            Toast.makeText(SettingActivity.this, "Please set & update profile", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                            progressDialog.dismiss();
                    }
                });
    }

    private void updateSetting() {
        String UserName = userName.getText().toString();
        String Status = userStatus.getText().toString();
        if(UserName.isEmpty())
        {
            userName.setError("Please Enter Name");
            return;
        }
        if(Status.isEmpty())
        {
            userStatus.setError("Please enter status");
            return;
        }
        else {
            HashMap<String,Object> parameter = new HashMap<>();
            parameter.put("uid",currentUserID);
            parameter.put("name",UserName);
            parameter.put("status",Status);

            RootRef.child("Users").child(currentUserID).updateChildren(parameter).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(SettingActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SettingActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        String message = task.getException().toString();
                        Toast.makeText(SettingActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode==gallerypict &&  resultCode==RESULT_OK && data!=null)
        {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode== RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Updating");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final Uri resutlUri = result.getUri();

                StorageReference filePath = userProfileImagesReference.child(currentUserID + ".jpg");
                filePath.putFile(resutlUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {
                                String uril = o.toString();

                               RootRef.child("Users").child(currentUserID).child("image").setValue(uril).addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if(task.isSuccessful())
                                       {
                                           loadingBar.dismiss();
                                           Toast.makeText(SettingActivity.this, "image has been uploaded", Toast.LENGTH_SHORT).show();
                                       }
                                       else
                                       {
                                           loadingBar.dismiss();
                                           Toast.makeText(SettingActivity.this, "Error in image uploading", Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               });
                            }
                        });
                    }
                });
            }
        }


    }
}
