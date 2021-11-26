package com.example.assignment2;

import android.os.Parcel;
import android.os.Parcelable;

public class Campaign implements Parcelable {
    private Double longitude, latitude;
    private String campaignName, organization, startDate, description, creatorId, imageFileName;

    public Campaign() {
    }

    public Campaign(Double longitude, Double latitude, String campaignName,
                    String organization, String startDate, String description,
                    String creatorId) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.campaignName = campaignName;
        this.organization = organization;
        this.startDate = startDate;
        this.description = description;
        this.creatorId = creatorId;
    }

    protected Campaign(Parcel in) {
        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readDouble();
        }
        campaignName = in.readString();
        organization = in.readString();
        startDate = in.readString();
        description = in.readString();
        creatorId = in.readString();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (longitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(longitude);
        }
        if (latitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(latitude);
        }
        dest.writeString(campaignName);
        dest.writeString(organization);
        dest.writeString(startDate);
        dest.writeString(description);
        dest.writeString(creatorId);
        dest.writeString(imageFileName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Campaign> CREATOR = new Creator<Campaign>() {
        @Override
        public Campaign createFromParcel(Parcel in) {
            return new Campaign(in);
        }

        @Override
        public Campaign[] newArray(int size) {
            return new Campaign[size];
        }
    };

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
}
