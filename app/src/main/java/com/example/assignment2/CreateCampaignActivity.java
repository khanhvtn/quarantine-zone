package com.example.assignment2;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.bumptech.glide.Glide;

import java.io.File;
import java.net.URI;
import java.util.Calendar;

public class CreateCampaignActivity extends AppCompatActivity {
    private static final String ACTIVITY_TAG = "CreateCampaign";
    private static final int REQUEST_ID_READ_WRITE_PERMISSION = 99;
    private static final int REQUEST_ID_IMAGE_CAPTURE = 100;
    private static final int REQUEST_ID_VIDEO_CAPTURE = 101;
    private AppCompatButton crCamBtnBack, crCamBtnCreate, crCamBtnChangeImage, crCamBtnRemoveImage;
    private AppCompatImageButton crCamBtnPickDate;
    private AppCompatEditText crCamEdtCampaignName, crCamEdtOrganization, crCamEdtStartDate,
            crCamEdtDescription;
    private AppCompatImageView crCamImageView;
    private int lastSelectedYear, lastSelectedMonth, lastSelectedDayOfMonth;
    private ActivityResultLauncher<Uri> captureImageLauncher;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_campaign);

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
        crCamEdtCampaignName.setText(latitude.toString());
        crCamEdtOrganization.setText(longitude.toString());

        //set current for start date field.
        crCamEdtStartDate
                .setText(lastSelectedDayOfMonth + "/" + (lastSelectedMonth + 1) + "/" +
                        lastSelectedYear);

        //set listener
        crCamBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
                captureImageLauncher.launch(imageUri);
            }
        });

        crCamBtnRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crCamImageView.setImageDrawable(getResources().getDrawable(R.drawable.app_logo));
                UpdateUIPickImage("default");
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
}