package com.example.assignment2.fragments;

import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.assignment2.adapters.CustomAdapter;
import com.example.assignment2.R;
import com.example.assignment2.models.Campaign;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class GenerateReport extends Fragment implements SearchView.OnQueryTextListener {
    private RecyclerView genRe_listViewCampaign;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private AlertDialog loadingProcess;
    private ActivityResultLauncher editActivityLauncher;
    private SearchView genRe_edtSearch;
    private AppCompatButton genRe_btnGenReport;
    private CustomAdapter customAdapter;

    public GenerateReport() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.loading_progress);
        loadingProcess = builder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_generate_report, container, false);
        genRe_edtSearch = view.findViewById(R.id.genRe_edtSearch);
        genRe_listViewCampaign = view.findViewById(R.id.genRe_listViewCampaign);
        genRe_btnGenReport = view.findViewById(R.id.genRe_btnGenReport);
        genRe_listViewCampaign.setLayoutManager(new LinearLayoutManager(getContext()));

        //add listener
        genRe_edtSearch.setOnQueryTextListener(this);
        genRe_btnGenReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GenerateReport();
            }
        });


        editActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result != null) {
                            Campaign updatedCampaign =
                                    result.getData().getParcelableExtra("updated_campaign");
                            Campaign oldCampaign =
                                    result.getData().getParcelableExtra("old_campaign");
                            for (Campaign campaign : customAdapter.getCampaignList()) {
                                if (campaign.getCampaignName()
                                        .equals(oldCampaign.getCampaignName())) {
                                    customAdapter.getCampaignList().remove(campaign);
                                    break;
                                }
                            }
                            customAdapter.getCampaignList().add(updatedCampaign);
                            customAdapter.notifyDataSetChanged();
                        }
                    }
                });

        //get list event
        loadingProcess.show();

        db.collection("campaigns").get()
                .addOnSuccessListener(
                        new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                List<Campaign> campaignList =
                                        queryDocumentSnapshots.toObjects(Campaign.class);
                                //Create Adapter for Recycler View
                                customAdapter =
                                        new CustomAdapter(campaignList, editActivityLauncher);
                                genRe_listViewCampaign.setAdapter(customAdapter);
                                loadingProcess.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingProcess.dismiss();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        customAdapter.filter(newText);
        return false;
    }

    private void ToastMessage(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void GenerateReport() {
        loadingProcess.show();
        File dir = new File(getContext().getFilesDir(), "reports");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File reportFile = new File(dir, "report.csv");
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append("Campaign Name,Organization,Start Date,Description,Number of Volunteers,Number of Tested People\n");
            FileWriter writer = new FileWriter(reportFile);
            for (Campaign campaign : customAdapter.getBackupList()) {
                stringBuilder.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%d,%d\n", campaign.getCampaignName(),
                        campaign.getOrganization(), campaign.getStartDate(),
                        campaign.getDescription(), campaign.getListVolunteers().size(),
                        campaign.getNumberTestedPeople()));
            }
            writer.append(stringBuilder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            ToastMessage("Something went wrong. Please try again!!!");
            loadingProcess.dismiss();
        }
        loadingProcess.dismiss();
        ToastMessage("Generate Report Successfully!!!");

    }
}