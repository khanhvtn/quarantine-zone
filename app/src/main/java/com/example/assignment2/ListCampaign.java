package com.example.assignment2;

import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class ListCampaign extends Fragment {
    private RecyclerView listCam_listViewCampaign;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private AlertDialog loadingProcess;
    private ActivityResultLauncher editActivityLauncher;


    public ListCampaign() {
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
        View view = inflater.inflate(R.layout.fragment_list_campaign, container, false);
        listCam_listViewCampaign = view.findViewById(R.id.listCam_listViewCampaign);
        listCam_listViewCampaign.setLayoutManager(new LinearLayoutManager(getContext()));

        //Create Adapter for Recycler View
        CustomAdapter customAdapter = new CustomAdapter();

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
                            for (Campaign campaign: customAdapter.getCampaignList()){
                                if(campaign.getCampaignName().equals(oldCampaign.getCampaignName())){
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

        db.collection("campaigns").whereEqualTo("creatorId", mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(
                        new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                List<Campaign> campaignList =
                                        queryDocumentSnapshots.toObjects(Campaign.class);
                                customAdapter.setCampaignList(campaignList);
                                customAdapter.setEditActivityLauncher(editActivityLauncher);
                                listCam_listViewCampaign.setAdapter(customAdapter);
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
}