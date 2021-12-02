package com.example.assignment2.models;

public class Notification {
    private Campaign newInformation;
    private String oldCampaignName;

    public Notification() {
    }

    public Notification(Campaign newInformation, String oldCampaignName) {
        this.newInformation = newInformation;
        this.oldCampaignName = oldCampaignName;
    }

    public Campaign getNewInformation() {
        return newInformation;
    }

    public void setNewInformation(Campaign newInformation) {
        this.newInformation = newInformation;
    }

    public String getOldCampaignName() {
        return oldCampaignName;
    }

    public void setOldCampaignName(String oldCampaignName) {
        this.oldCampaignName = oldCampaignName;
    }
}
