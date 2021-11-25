package com.example.assignment2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;

public class CreateCampaignActivity extends AppCompatActivity {
    private AppCompatButton crCamBtnBack, crCamBtnCreate, crCamBtnChangeImage, crCamBtnRemoveImage;
    private AppCompatImageButton crCamBtnPickDate;
    private AppCompatEditText crCamEdtCampaignName, crCamEdtOrganization, crCamEdtStartDate,
            crCamEdtDescription;
    private AppCompatImageView crCamImage;
    private int lastSelectedYear, lastSelectedMonth, lastSelectedDayOfMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_campaign);

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
        crCamImage = findViewById(R.id.crCam_image);
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

    }
}