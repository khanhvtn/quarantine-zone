package com.example.assignment2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class CampaignDetailActivity extends AppCompatActivity {
    private final String CAMPAIGN_NAME = "campaignName";
    private AppCompatTextView txtCampaignName, txtDescription, txtOrganization, txtStartDate,
            txtVolunteers, txtTestedPeople, txtCreator;
    private AppCompatImageView campaignImage;
    private AppCompatButton btnJoin;
    private AppCompatImageButton btnBack;
    private AlertDialog loadingDialog;
    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    private FirebaseAuth mAuth;
    private Campaign currentCampaign;
    private String currentCampaignId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_campaign_detail);
        //get intent
        String campaignName = getIntent().getStringExtra(CAMPAIGN_NAME);

        //Firebase
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.loading_progress);
        loadingDialog = builder.create();

        //declare field
        txtCampaignName = findViewById(R.id.detail_txtCampaignName);
        txtDescription = findViewById(R.id.detail_txtDescription);
        txtOrganization = findViewById(R.id.detail_txtOrganization);
        txtStartDate = findViewById(R.id.detail_txtStarDate);
        txtVolunteers = findViewById(R.id.detail_txtVolunteers);
        txtTestedPeople = findViewById(R.id.detail_txtTestedPeople);
        txtCreator = findViewById(R.id.detail_txtCreator);
        campaignImage = findViewById(R.id.detail_image);
        btnJoin = findViewById(R.id.detail_btnJoin);
        btnBack = findViewById(R.id.detail_btnBack);

        //display campaign information
        loadingDialog.show();
        db.collection("campaigns").whereEqualTo(CAMPAIGN_NAME, campaignName).get()
                .addOnSuccessListener(
                        new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                List<Campaign> campaignList =
                                        queryDocumentSnapshots.toObjects(Campaign.class);
                                currentCampaignId =
                                        queryDocumentSnapshots.getDocuments().get(0).getId();
                                currentCampaign = campaignList.get(0);
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                //disable join button if current user is creator
                                if (!currentCampaign.getCreatorId().equals(currentUser.getUid())) {
                                    if (currentCampaign.getListVolunteers()
                                            .contains(currentUser.getUid())) {
                                        btnJoin.setText("Leave");
                                    }
                                    btnJoin.setVisibility(View.VISIBLE);
                                }
                                db.collection("users").document(currentCampaign.getCreatorId())
                                        .get().addOnSuccessListener(
                                        new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(
                                                    DocumentSnapshot documentSnapshot) {
                                                User creator =
                                                        documentSnapshot.toObject(User.class);
                                                firebaseStorage.getReference().child("images/" +
                                                        currentCampaign.getImageFileName())
                                                        .getDownloadUrl().addOnSuccessListener(
                                                        new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                Glide.with(
                                                                        CampaignDetailActivity.this)
                                                                        .load(uri)
                                                                        .into(campaignImage);
                                                                txtCampaignName.setText(
                                                                        currentCampaign
                                                                                .getCampaignName());
                                                                txtDescription.setText(
                                                                        currentCampaign
                                                                                .getDescription());
                                                                txtOrganization.setText(
                                                                        currentCampaign
                                                                                .getOrganization());
                                                                txtStartDate.setText(currentCampaign
                                                                        .getStartDate());
                                                                txtVolunteers
                                                                        .setText("100 Volunteers");
                                                                txtTestedPeople.setText(
                                                                        "100 Tested People");
                                                                txtCreator.setText(
                                                                        creator.getFullName());
                                                                loadingDialog.dismiss();
                                                            }
                                                        }).addOnFailureListener(
                                                        new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(
                                                                    @NonNull Exception e) {
                                                                Toast.makeText(
                                                                        CampaignDetailActivity.this,
                                                                        e.getMessage(),
                                                                        Toast.LENGTH_SHORT).show();
                                                                loadingDialog.dismiss();
                                                            }
                                                        });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(CampaignDetailActivity.this, e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CampaignDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
                loadingDialog.dismiss();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.show();
                ArrayList<String> campaignArrayList = currentCampaign.getListVolunteers();
                if (btnJoin.getText().equals("Join")) {
                    campaignArrayList.add(mAuth.getCurrentUser().getUid());
                } else {
                    campaignArrayList.remove(mAuth.getCurrentUser().getUid());
                }
                currentCampaign.setListVolunteers(campaignArrayList);
                db.collection("campaigns").document(currentCampaignId)
                        .set(currentCampaign, SetOptions
                                .merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (btnJoin.getText().equals("Join")) {
                            ToastMessage("Join Successfully!!!");
                            btnJoin.setText("Leave");
                        } else {
                            ToastMessage("Leave Successfully!!!");
                            btnJoin.setText("Join");
                        }
                        loadingDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        ToastMessage(e.getMessage());
                        loadingDialog.dismiss();
                    }
                });

            }
        });
    }

    private void ToastMessage(String message) {
        Toast.makeText(CampaignDetailActivity.this, message,
                Toast.LENGTH_SHORT).show();
    }
}