package com.example.assignment2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchCampaignAdapter extends RecyclerView.Adapter<SearchCampaignAdapter.ViewHolder>
        implements
        View.OnClickListener {
    private List<Campaign> campaignList;
    private ArrayList<Campaign> backupList;
    private Pattern r;
    private Matcher m;
    private RecyclerView searchResult;
    private GoogleMap map;
    private SearchView edtSearch;

    public SearchCampaignAdapter(RecyclerView searchResult,
                                 GoogleMap mMap,
                                 List<Campaign> campaignList,
                                 SearchView edtSearch) {
        this.backupList = new ArrayList<>();
        this.searchResult = searchResult;
        this.campaignList = campaignList;
        this.map = mMap;
        this.edtSearch = edtSearch;
        this.backupList.addAll(campaignList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_item_search_campaign, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Campaign campaign = this.campaignList.get(position);
        holder.getSearch_txtCampaignName().setText(campaign.getCampaignName());
    }

    @Override
    public int getItemCount() {
        return campaignList.size();
    }

    @Override
    public void onClick(View v) {
        int itemPosition = searchResult.getChildLayoutPosition(v);
        Campaign campaign = campaignList.get(itemPosition);
        map.animateCamera(CameraUpdateFactory
                .newLatLngZoom(new LatLng(campaign.getLatitude(), campaign.getLongitude()), 16f));
        filter("");
        edtSearch.setQuery("", true);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatTextView search_txtCampaignName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            search_txtCampaignName = itemView.findViewById(R.id.search_txtCampaignName);
        }

        public AppCompatTextView getSearch_txtCampaignName() {
            return search_txtCampaignName;
        }
    }

    public List<Campaign> getCampaignList() {
        return campaignList;
    }

    public void setCampaignList(List<Campaign> campaignList) {
        this.campaignList = campaignList;
    }

    public ArrayList<Campaign> getBackupList() {
        return backupList;
    }

    public void setBackupList(List<Campaign> backupList) {
        this.backupList.addAll(backupList);
    }

    public void filter(String query) {
        campaignList.clear();
        if (query.length() == 0) {
            campaignList.addAll(backupList);
        } else {
            // Create a Pattern object
            r = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
            for (Campaign campaign : backupList) {
                // Now create matcher object.
                m = r.matcher(campaign.getCampaignName());
                if (m.find()) {
                    campaignList.add(campaign);
                }
            }
        }
        notifyDataSetChanged();
    }
}
