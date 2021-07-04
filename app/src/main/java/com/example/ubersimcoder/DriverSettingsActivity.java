package com.example.ubersimcoder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.util.HashMap;
import java.util.Map;

public class DriverSettingsActivity extends AppCompatActivity
{
    private EditText mNameField, mPhoneField, mCarField;

    private Button mBack, mConfirm;

    private ImageView mProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;

    private String userID;

    private String mName;
    private String mPhone;
    private String mCar;
    private String mService;
    private String mProfileImageUrl;

    private Uri resultUri;

    private RadioGroup mRadioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_settings);

        mNameField = findViewById(R.id.name);
        mPhoneField = findViewById(R.id.phone);
        mCarField = findViewById(R.id.car);

        mProfileImage = findViewById(R.id.profileImage);

        mRadioGroup = findViewById(R.id.radioGroup);

        mBack = findViewById(R.id.back);
        mConfirm = findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users")
                .child("Drivers").child(userID);

        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);


            }
        });


        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                saveUserInformation();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                finish();
                return;

            }
        });


    }


    private void getUserInfo()
    {
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if(map.get("name")!=null)
                    {
                        mName = map.get("name").toString();
                        mNameField.setText(mName);

                    }

                    if(map.get("phone") !=null)
                    {
                        mPhone = map.get("phone").toString();
                        mPhoneField.setText(mPhone);
                    }

                    if(map.get("car") !=null)
                    {
                        mCar = map.get("car").toString();
                        mCarField.setText(mCar);
                    }

                    if(map.get("service") !=null)
                    {
                        mService = map.get("service").toString();

                        switch (mService)
                        {
                            case "UberX" :
                                mRadioGroup.check(R.id.UberX);
                                break;

                            case "UberBlack" :
                                mRadioGroup.check(R.id.UberBlack);
                                break;

                            case "UberXl" :
                                mRadioGroup.check(R.id.UberXl);
                                break;
                        }

                    }

                    if(map.get("profileImageUrl") !=null)
                    {
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(mProfileImage);

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }





    private void saveUserInformation()
    {
        mName = mNameField.getText().toString();
        mPhone = mPhoneField.getText().toString();
        mCar = mCarField.getText().toString();

        int selectedId = mRadioGroup.getCheckedRadioButtonId();

        final RadioButton radioButton = findViewById(selectedId);

        if(radioButton.getText() == null)
        {
            return;
        }

        mService = radioButton.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name",mName);
        userInfo.put("phone",mPhone);
        userInfo.put("car",mCar);
        userInfo.put("service",mService);

        mDriverDatabase.updateChildren(userInfo);

        if(resultUri !=null)
        {
            final StorageReference ref = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
//
//            ref.putFile(resultUri)
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
//                        {
//
//
//                        }
//                    });

            ref.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUrl = task.getResult();

                        Map newImage = new HashMap();
                        newImage.put("profileImageUrl",downloadUrl.toString());

                        mDriverDatabase.updateChildren(newImage);

                        finish();
                        return;


                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });


        }
        else
        {
            finish();

        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==1 && resultCode == Activity.RESULT_OK)
        {
            final Uri imageUri = data.getData();

            resultUri = imageUri;

            mProfileImage.setImageURI(resultUri);





        }



    }
}
