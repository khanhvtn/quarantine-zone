package com.example.assignment2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class CreateCampaignActivity extends AppCompatActivity {
    private AppCompatButton crCamBtnBack, crCamBtnCreate, crCamBtnChangeImage, crCamBtnRemoveImage;
    private AppCompatEditText crCamEdtCampaignName, crCamEdtOrganization, crCamEdtStartDate,
            crCamEdtDescription;
    private AppCompatImageView crCamImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_create_campaign);

        //get intent
        Intent intent = getIntent();
        Double latitude = intent.getDoubleExtra("latitude", -1);
        Double longitude = intent.getDoubleExtra("longitude", -1);

        //declare field
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

        crCamBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }
}