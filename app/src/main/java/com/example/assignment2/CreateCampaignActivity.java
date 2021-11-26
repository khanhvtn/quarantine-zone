package com.example.assignment2;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.FileProvider;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;
import java.util.Calendar;

public class CreateCampaignActivity extends AppCompatActivity
        implements CaptureImageDialogFragment.NoticeDialogListener {
    private static final String ACTIVITY_TAG = "CreateCampaign";
    private static final String CAPTURE_IMAGE_DIALOG_TAG = "CAPTURE_IMAGE_DIALOG_TAG";
    private static final int REQUEST_ID_READ_WRITE_PERMISSION = 99;
    private static final int REQUEST_ID_IMAGE_CAPTURE = 100;
    private static final int REQUEST_ID_VIDEO_CAPTURE = 101;
    private AppCompatButton crCamBtnBack, crCamBtnCreate, crCamBtnChangeImage, crCamBtnRemoveImage;
    private AppCompatImageButton crCamBtnPickDate;
    private AppCompatEditText crCamEdtCampaignName, crCamEdtOrganization, crCamEdtStartDate,
            crCamEdtDescription;
    private AppCompatImageView crCamImageView;
    private int lastSelectedYear, lastSelectedMonth, lastSelectedDayOfMonth;
    private ActivityResultLauncher captureImageLauncher, pickImageFromPhoto;
    private Uri imageUri;
    private CaptureImageDialogFragment captureImageDialogFragment;
    private AlertDialog loadingProgress;
    private String campaignName, organization, startDate, description;
    private FirebaseFirestore db;
    private FirebaseStorage storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_campaign);
        //Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        //generate progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.loading_progress).setCancelable(false);
        loadingProgress = builder.create();

        //create temporary image
        File newFile = new File(this.getFilesDir(), "default_image.jpg");
        imageUri = FileProvider
                .getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", newFile);

        //set activity result launcher
        captureImageLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        Log.i(ACTIVITY_TAG, "Capture Image");
                        if (result) {
                            Glide.with(CreateCampaignActivity.this).load(imageUri)
                                    .into(crCamImageView);
                            UpdateUIPickImage("image");
                        }
                    }
                });
        pickImageFromPhoto = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {

                            Glide.with(CreateCampaignActivity.this).load(result)
                                    .into(crCamImageView);
                            imageUri = result;
                            UpdateUIPickImage("image");
                        }
                    }
                });

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        lastSelectedYear = c.get(Calendar.YEAR);
        lastSelectedMonth = c.get(Calendar.MONTH);
        lastSelectedDayOfMonth = c.get(Calendar.DAY_OF_MONTH);


        //get intent
        Intent intent = getIntent();
        Double latitude = intent.getDoubleExtra("latitude", -1);
        Double longitude = intent.getDoubleExtra("longitude", -1);

        //declare field
        crCamBtnPickDate = findViewById(R.id.crCam_btnPickDate);
        crCamBtnBack = findViewById(R.id.crCam_btnBack);
        crCamBtnCreate = findViewById(R.id.crCam_btnCreate);
        crCamBtnChangeImage = findViewById(R.id.crCam_btnChangeImage);
        crCamBtnRemoveImage = findViewById(R.id.crCam_btnRemoveImage);
        crCamEdtCampaignName = findViewById(R.id.crCam_edtCampaignName);
        crCamEdtOrganization = findViewById(R.id.crCam_edtOrganization);
        crCamEdtStartDate = findViewById(R.id.crCam_edtStartDate);
        crCamEdtDescription = findViewById(R.id.crCam_edtDescription);
        crCamImageView = findViewById(R.id.crCam_image);

        //set current for start date field.
        crCamEdtStartDate
                .setText(lastSelectedDayOfMonth + "/" + (lastSelectedMonth + 1) + "/" +
                        lastSelectedYear);

        //set listener
        crCamBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        crCamBtnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener dateSetListener =
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month,
                                                  int dayOfMonth) {
                                crCamEdtStartDate
                                        .setText(dayOfMonth + "/" + (month + 1) + "/" + year);

                            }
                        };

                DatePickerDialog datePickerDialog = null;
                datePickerDialog =
                        new DatePickerDialog(CreateCampaignActivity.this, dateSetListener,
                                lastSelectedYear,
                                lastSelectedMonth, lastSelectedDayOfMonth);
                datePickerDialog.show();
            }
        });

        crCamBtnChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImageDialogFragment = new CaptureImageDialogFragment();
                captureImageDialogFragment
                        .show(getSupportFragmentManager(), CAPTURE_IMAGE_DIALOG_TAG);

            }
        });

        crCamBtnRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crCamImageView.setImageDrawable(getResources().getDrawable(R.drawable.app_logo));
                UpdateUIPickImage("default");
            }
        });

        crCamBtnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgress.show();
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String validateResult = validateUserInput();
                if (validateResult != null) {
                    Toast.makeText(CreateCampaignActivity.this, validateResult, Toast.LENGTH_SHORT)
                            .show();
                    loadingProgress.dismiss();
                } else {
                    Campaign newCampaign = new Campaign(longitude, latitude, campaignName,
                            organization, startDate, description,
                            currentUserId);
                    db.collection("campaigns").add(newCampaign).addOnSuccessListener(
                            new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    String imageFileName = documentReference.getId() + ".jpg";
                                    UploadTask uploadTask =
                                            storage.getReference().child("images/" + imageFileName)
                                                    .putFile(imageUri);
                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            db.collection("events")
                                                    .document(documentReference.getId()).delete();
                                            Toast.makeText(CreateCampaignActivity.this,
                                                    "Something went wrong with uploading image. Please try again!!!",
                                                    Toast.LENGTH_SHORT)
                                                    .show();
                                            loadingProgress.dismiss();
                                        }
                                    });
                                    uploadTask.addOnSuccessListener(
                                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(
                                                        UploadTask.TaskSnapshot taskSnapshot) {
                                                    newCampaign.setImageFileName(imageFileName);
                                                    documentReference
                                                            .set(newCampaign, SetOptions.merge())
                                                            .addOnSuccessListener(
                                                                    new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(
                                                                                Void unused) {
                                                                            Toast.makeText(
                                                                                    CreateCampaignActivity.this,
                                                                                    "Create Successfully!!!",
                                                                                    Toast.LENGTH_SHORT)
                                                                                    .show();
                                                                            loadingProgress
                                                                                    .dismiss();
                                                                            finish();
                                                                        }
                                                                    }).addOnFailureListener(
                                                            new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(
                                                                        @NonNull Exception e) {
                                                                    Toast.makeText(
                                                                            CreateCampaignActivity.this,
                                                                            "Something went wrong. Please try again!!!",
                                                                            Toast.LENGTH_SHORT)
                                                                            .show();
                                                                    loadingProgress.dismiss();
                                                                }
                                                            });
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateCampaignActivity.this, "Something went wrong!!!",
                                    Toast.LENGTH_SHORT)
                                    .show();
                            loadingProgress.dismiss();
                        }
                    });
                }
            }
        });

    }

    private void UpdateUIPickImage(String tag) {
        crCamImageView.setTag(tag);
        if (crCamImageView.getTag().equals("default")) {
            crCamBtnChangeImage.setVisibility(View.VISIBLE);
            crCamBtnRemoveImage.setVisibility(View.GONE);
        } else {
            crCamBtnChangeImage.setVisibility(View.GONE);
            crCamBtnRemoveImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPickingOption(int optionIndex) {
        switch (optionIndex) {
            case 0:
                captureImageLauncher.launch(imageUri);
                break;
            case 1:
                pickImageFromPhoto.launch("image/*");
                break;
        }
    }

    private String validateUserInput() {
        campaignName = crCamEdtCampaignName.getText().toString().trim();
        organization = crCamEdtOrganization.getText().toString().trim();
        startDate = crCamEdtStartDate.getText().toString().trim();
        description = crCamEdtDescription.getText().toString().trim();
        if (campaignName.isEmpty()) {
            return "Campaign Name can not be blanked.";
        } else if (organization.isEmpty()) {
            return "Organization can not be blanked.";
        } else if (startDate.isEmpty()) {
            return "Start Date can not be blanked.";
        } else if (description.isEmpty()) {
            return "Description can not be blanked.";
        }else if (crCamImageView.getTag().equals("default")) {
            return "Please upload image.";
        } else {
            return null;
        }
    }
}