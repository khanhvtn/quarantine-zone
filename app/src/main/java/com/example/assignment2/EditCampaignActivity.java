package com.example.assignment2;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.FileProvider;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.assignment2.models.Campaign;
import com.example.assignment2.models.CaptureImageDialogFragment;
import com.example.assignment2.models.Notification;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Calendar;

public class EditCampaignActivity extends AppCompatActivity
        implements CaptureImageDialogFragment.NoticeDialogListener {
    private static final String ACTIVITY_TAG = "EditCampaign";
    private static final String CAPTURE_IMAGE_DIALOG_TAG = "CAPTURE_IMAGE_DIALOG_TAG";
    private static final String EDIT_CAMPAIGN_TAG = "EditCampaignActivity";
    private AppCompatButton edtCamBtnBack, edtCamBtnEdit, edtCamBtnChangeImage,
            edtCamBtnRemoveImage;
    private AppCompatImageButton edtCamBtnPickDate;
    private AppCompatEditText edtCamEdtCampaignName, edtCamEdtOrganization, edtCamEdtStartDate,
            edtCamEdtDescription, edtCamEdtTestedPeople;
    private AppCompatImageView edtCamImageView;
    private int lastSelectedYear, lastSelectedMonth, lastSelectedDayOfMonth;
    private ActivityResultLauncher captureImageLauncher, pickImageFromPhoto;
    private Uri imageUri;
    private CaptureImageDialogFragment captureImageDialogFragment;
    private AlertDialog loadingProgress;
    private String campaignName, organization, startDate, description, testedPeople;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Campaign currentCampaign;
    private Boolean isImageChange = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_edit_campaign);
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
                            Glide.with(EditCampaignActivity.this).load(imageUri)
                                    .into(edtCamImageView);
                            UpdateUIPickImage("image");
                            isImageChange = true;
                        }
                    }
                });
        pickImageFromPhoto = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {

                            Glide.with(EditCampaignActivity.this).load(result)
                                    .into(edtCamImageView);
                            imageUri = result;
                            UpdateUIPickImage("image");
                            isImageChange = true;
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
        currentCampaign = intent.getParcelableExtra("campaign");

        //declare field
        edtCamBtnPickDate = findViewById(R.id.edtCam_btnPickDate);
        edtCamBtnBack = findViewById(R.id.edtCam_btnBack);
        edtCamBtnEdit = findViewById(R.id.edtCam_btnEdit);
        edtCamBtnChangeImage = findViewById(R.id.edtCam_btnChangeImage);
        edtCamBtnRemoveImage = findViewById(R.id.edtCam_btnRemoveImage);
        edtCamEdtCampaignName = findViewById(R.id.edtCam_edtCampaignName);
        edtCamEdtOrganization = findViewById(R.id.edtCam_edtOrganization);
        edtCamEdtStartDate = findViewById(R.id.edtCam_edtStartDate);
        edtCamEdtDescription = findViewById(R.id.edtCam_edtDescription);
        edtCamEdtTestedPeople = findViewById(R.id.edtCam_edtTestedPeople);
        edtCamImageView = findViewById(R.id.edtCam_image);

        //display campaign information
        loadingProgress.show();
        storage.getReference().child("images/" + currentCampaign.getImageFileName())
                .getDownloadUrl().addOnSuccessListener(
                new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        edtCamEdtCampaignName.setText(currentCampaign.getCampaignName());
                        edtCamEdtOrganization.setText(currentCampaign.getOrganization());
                        edtCamEdtStartDate.setText(currentCampaign.getStartDate());
                        edtCamEdtDescription.setText(currentCampaign.getDescription());
                        edtCamEdtTestedPeople
                                .setText(currentCampaign.getNumberTestedPeople().toString());
                        Glide.with(EditCampaignActivity.this).load(uri).into(edtCamImageView);
                        UpdateUIPickImage("image");
                        loadingProgress.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ToastMessage(e.getMessage());
                loadingProgress.dismiss();
            }
        });
        //set listener
        edtCamBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        edtCamBtnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener dateSetListener =
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month,
                                                  int dayOfMonth) {
                                edtCamEdtStartDate
                                        .setText(dayOfMonth + "/" + (month + 1) + "/" + year);

                            }
                        };

                DatePickerDialog datePickerDialog = null;
                datePickerDialog =
                        new DatePickerDialog(EditCampaignActivity.this, dateSetListener,
                                lastSelectedYear,
                                lastSelectedMonth, lastSelectedDayOfMonth);
                datePickerDialog.show();
            }
        });

        edtCamBtnChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImageDialogFragment = new CaptureImageDialogFragment();
                captureImageDialogFragment
                        .show(getSupportFragmentManager(), CAPTURE_IMAGE_DIALOG_TAG);

            }
        });

        edtCamBtnRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtCamImageView.setImageDrawable(getResources().getDrawable(R.drawable.app_logo));
                UpdateUIPickImage("default");
            }
        });

        edtCamBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgress.show();
                String validateResult = validateUserInput();
                if (validateResult != null) {
                    Toast.makeText(EditCampaignActivity.this, validateResult, Toast.LENGTH_SHORT)
                            .show();
                    loadingProgress.dismiss();
                } else {
                    Campaign updatedCampaign = new Campaign();
                    updatedCampaign.setCampaignName(campaignName);
                    updatedCampaign.setOrganization(organization);
                    updatedCampaign.setStartDate(startDate);
                    updatedCampaign.setDescription(description);
                    updatedCampaign.setListVolunteers(currentCampaign.getListVolunteers());
                    updatedCampaign.setNumberTestedPeople(Integer.parseInt(testedPeople));
                    updatedCampaign.setCreatorId(currentCampaign.getCreatorId());
                    updatedCampaign.setImageFileName(currentCampaign.getImageFileName());
                    updatedCampaign.setLatitude(currentCampaign.getLatitude());
                    updatedCampaign.setLongitude(currentCampaign.getLongitude());
                    db.collection("campaigns")
                            .whereEqualTo("campaignName", currentCampaign.getCampaignName()).get()
                            .addOnSuccessListener(
                                    new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(
                                                QuerySnapshot queryDocumentSnapshots) {
                                            String docId =
                                                    queryDocumentSnapshots.getDocuments().get(0)
                                                            .getId();
                                            db.collection("campaigns").document(docId)
                                                    .set(updatedCampaign, SetOptions.merge())
                                                    .addOnSuccessListener(
                                                            new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    if (isImageChange) {
                                                                        UploadTask uploadTask =
                                                                                storage.getReference()
                                                                                        .child("images/" +
                                                                                                updatedCampaign
                                                                                                        .getImageFileName())
                                                                                        .putFile(
                                                                                                imageUri);
                                                                        uploadTask
                                                                                .addOnFailureListener(
                                                                                        new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(
                                                                                                    @NonNull
                                                                                                            Exception e) {
                                                                                                db.collection(
                                                                                                        "campaigns")
                                                                                                        .document(
                                                                                                                docId)
                                                                                                        .set(currentCampaign);
                                                                                                ToastMessage(
                                                                                                        e.getMessage());
                                                                                                loadingProgress
                                                                                                        .dismiss();
                                                                                            }
                                                                                        });
                                                                        uploadTask
                                                                                .addOnSuccessListener(
                                                                                        new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                                            @Override
                                                                                            public void onSuccess(
                                                                                                    UploadTask.TaskSnapshot taskSnapshot) {
                                                                                                ToastMessage(
                                                                                                        "Edit Successfully!!!");
                                                                                                loadingProgress
                                                                                                        .dismiss();
                                                                                                setResult(
                                                                                                        RESULT_OK,
                                                                                                        new Intent()
                                                                                                                .putExtra(
                                                                                                                        "updated_campaign",
                                                                                                                        updatedCampaign)
                                                                                                                .putExtra(
                                                                                                                        "old_campaign",
                                                                                                                        currentCampaign));
                                                                                                SendNotificationForVolunteers(
                                                                                                        currentCampaign,
                                                                                                        updatedCampaign);
                                                                                                finish();
                                                                                            }
                                                                                        });
                                                                    } else {
                                                                        ToastMessage(
                                                                                "Edit Successfully!!!");
                                                                        loadingProgress
                                                                                .dismiss();
                                                                        setResult(RESULT_OK,
                                                                                new Intent()
                                                                                        .putExtra(
                                                                                                "updated_campaign",
                                                                                                updatedCampaign)
                                                                                        .putExtra(
                                                                                                "old_campaign",
                                                                                                currentCampaign));
                                                                        SendNotificationForVolunteers(
                                                                                currentCampaign,
                                                                                updatedCampaign);

                                                                        finish();
                                                                    }
                                                                }
                                                            })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(
                                                                @NonNull Exception e) {
                                                            ToastMessage(e.getMessage());
                                                            loadingProgress.dismiss();
                                                        }
                                                    });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            ToastMessage(e.getMessage());
                            loadingProgress.dismiss();
                        }
                    });
                }
            }
        });

    }

    private void UpdateUIPickImage(String tag) {
        edtCamImageView.setTag(tag);
        if (edtCamImageView.getTag().equals("default")) {
            edtCamBtnChangeImage.setVisibility(View.VISIBLE);
            edtCamBtnRemoveImage.setVisibility(View.GONE);
        } else {
            edtCamBtnChangeImage.setVisibility(View.GONE);
            edtCamBtnRemoveImage.setVisibility(View.VISIBLE);
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
        campaignName = edtCamEdtCampaignName.getText().toString().trim();
        organization = edtCamEdtOrganization.getText().toString().trim();
        startDate = edtCamEdtStartDate.getText().toString().trim();
        description = edtCamEdtDescription.getText().toString().trim();
        testedPeople = edtCamEdtTestedPeople.getText().toString().trim();
        if (campaignName.isEmpty()) {
            return "Campaign Name can not be blanked.";
        } else if (organization.isEmpty()) {
            return "Organization can not be blanked.";
        } else if (startDate.isEmpty()) {
            return "Start Date can not be blanked.";
        } else if (description.isEmpty()) {
            return "Description can not be blanked.";
        } else if (testedPeople.isEmpty()) {
            return "Tested People can not be blanked.";
        } else if (Integer.parseInt(testedPeople) < 0) {
            return "Tested People can not be negative number.";
        } else if (edtCamImageView.getTag().equals("default")) {
            return "Please upload image.";
        } else {
            return null;
        }
    }

    private void ToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void SendNotificationForVolunteers(Campaign currentCampaign, Campaign updatedCampaign) {
        for (String volunteerId : updatedCampaign.getListVolunteers()) {
            Notification notification =
                    new Notification(updatedCampaign, currentCampaign.getCampaignName());
            db.collection("users").document(volunteerId)
                    .collection(getResources().getString(R.string.notifications_collection))
                    .add(notification).addOnFailureListener(
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(EDIT_CAMPAIGN_TAG, e.getMessage());
                        }
                    });
        }
    }
}