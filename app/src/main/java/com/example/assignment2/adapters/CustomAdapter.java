package com.example.assignment2.adapters;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.assignment2.EditCampaignActivity;
import com.example.assignment2.R;
import com.example.assignment2.models.Campaign;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private List<Campaign> campaignList ;
    private ArrayList<Campaign> backupList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseStorage firebaseStorage;
    private AlertDialog loadingProcess;
    private ActivityResultLauncher editActivityLauncher;
    private Pattern p;
    private Matcher m;

    public CustomAdapter(List<Campaign> campaignList,
                         ActivityResultLauncher editActivityLauncher) {
        this.campaignList = campaignList;
        this.backupList.addAll(campaignList) ;
        this.editActivityLauncher = editActivityLauncher;
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
        builder.setView(R.layout.loading_progress);
        loadingProcess = builder.create();

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_item_list_campaign, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Campaign campaign = this.campaignList.get(position);
        holder.getListCam_txtCampaignName().setText(campaign.getCampaignName());
        holder.getListCam_txtStartDate().setText(campaign.getStartDate());
        holder.getListCam_txtVolunteer()
                .setText(campaign.getListVolunteers().size() + " Volunteers");
        holder.getListCam_txtTestedPeople()
                .setText(campaign.getNumberTestedPeople().toString() + " Tested People");

        holder.listCam_btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Are you sure with your action?").setCancelable(false)
                        .setPositiveButton(
                                "Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        loadingProcess.show();
                                        int targetIndex = campaignList.indexOf(campaign);
                                        db.collection("campaigns").whereEqualTo("campaignName",
                                                campaign.getCampaignName()).get()
                                                .addOnSuccessListener(
                                                        new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(
                                                                    QuerySnapshot queryDocumentSnapshots) {
                                                                String docId =
                                                                        queryDocumentSnapshots
                                                                                .getDocuments()
                                                                                .get(0).getId();
                                                                db.collection("campaigns")
                                                                        .document(docId).delete()
                                                                        .addOnSuccessListener(
                                                                                new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(
                                                                                            Void unused) {
                                                                                        firebaseStorage
                                                                                                .getReference()
                                                                                                .child("images/" +
                                                                                                        campaign.getImageFileName())
                                                                                                .delete()
                                                                                                .addOnSuccessListener(
                                                                                                        new OnSuccessListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(
                                                                                                                    Void unused) {
                                                                                                                campaignList
                                                                                                                        .remove(targetIndex);
                                                                                                                notifyItemRemoved(
                                                                                                                        targetIndex);
                                                                                                                Toast.makeText(
                                                                                                                        v.getContext(),
                                                                                                                        "Delete Successfully",
                                                                                                                        Toast.LENGTH_SHORT)
                                                                                                                        .show();
                                                                                                                loadingProcess
                                                                                                                        .dismiss();
                                                                                                            }
                                                                                                        })
                                                                                                .addOnFailureListener(
                                                                                                        new OnFailureListener() {
                                                                                                            @Override
                                                                                                            public void onFailure(
                                                                                                                    @NonNull
                                                                                                                            Exception e) {
                                                                                                                Toast.makeText(
                                                                                                                        v.getContext(),
                                                                                                                        e.getMessage(),
                                                                                                                        Toast.LENGTH_SHORT)
                                                                                                                        .show();
                                                                                                                loadingProcess
                                                                                                                        .dismiss();
                                                                                                            }
                                                                                                        });

                                                                                    }
                                                                                })
                                                                        .addOnFailureListener(
                                                                                new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(
                                                                                            @NonNull
                                                                                                    Exception e) {
                                                                                        Toast.makeText(
                                                                                                v.getContext(),
                                                                                                e.getMessage(),
                                                                                                Toast.LENGTH_SHORT)
                                                                                                .show();
                                                                                        loadingProcess
                                                                                                .dismiss();
                                                                                    }
                                                                                });
                                                            }
                                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(v.getContext(),
                                                                e.getMessage(), Toast.LENGTH_SHORT)
                                                                .show();
                                                        loadingProcess.dismiss();
                                                    }
                                                });
                                    }
                                })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
            }
        });
        holder.listCam_btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EditCampaignActivity.class);
                intent.putExtra("campaign", campaign);
                editActivityLauncher.launch(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return campaignList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatTextView listCam_txtCampaignName, listCam_txtStartDate,
                listCam_txtVolunteer, listCam_txtTestedPeople;
        private final AppCompatImageButton listCam_btnEdit, listCam_btnRemove;

        public ViewHolder(@NonNull View view) {
            super(view);
            listCam_txtCampaignName = view.findViewById(R.id.listCam_txtCampaignName);
            listCam_txtStartDate = view.findViewById(R.id.listCam_txtStartDate);
            listCam_txtVolunteer = view.findViewById(R.id.listCam_txtVolunteer);
            listCam_txtTestedPeople = view.findViewById(R.id.listCam_txtTestedPeople);
            listCam_btnRemove = view.findViewById(R.id.listCam_btnRemove);
            listCam_btnEdit = view.findViewById(R.id.listCam_btnEdit);
        }

        public AppCompatTextView getListCam_txtCampaignName() {
            return listCam_txtCampaignName;
        }

        public AppCompatTextView getListCam_txtStartDate() {
            return listCam_txtStartDate;
        }

        public AppCompatTextView getListCam_txtVolunteer() {
            return listCam_txtVolunteer;
        }

        public AppCompatTextView getListCam_txtTestedPeople() {
            return listCam_txtTestedPeople;
        }

        public AppCompatImageButton getListCam_btnEdit() {
            return listCam_btnEdit;
        }

        public AppCompatImageButton getListCam_btnRemove() {
            return listCam_btnRemove;
        }
    }


    public ArrayList<Campaign> getBackupList() {
        return backupList;
    }

    public void setBackupList(ArrayList<Campaign> backupList) {
        this.backupList = backupList;
    }

    public List<Campaign> getCampaignList() {
        return campaignList;
    }

    public void setCampaignList(List<Campaign> campaignList) {
        this.campaignList = campaignList;
    }

    public ActivityResultLauncher getEditActivityLauncher() {
        return editActivityLauncher;
    }

    public void setEditActivityLauncher(ActivityResultLauncher editActivityLauncher) {
        this.editActivityLauncher = editActivityLauncher;
    }

    public void filter(String searchText) {
        this.campaignList.clear();
        if (searchText.isEmpty()) {
            this.campaignList.addAll(this.backupList);
        } else {
            p = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
            for (Campaign campaign : this.backupList) {

                m = p.matcher(campaign.getCampaignName());
                if (m.find()) {
                    this.campaignList.add(campaign);
                }
            }
        }
        notifyDataSetChanged();

    }
}
