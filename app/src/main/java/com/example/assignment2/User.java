package com.example.assignment2;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String fullName, email, phone, address, userId;
    private Boolean isAdmin;


    public User(){}
    public User(String fullName, String email, String phone, String address) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    protected User(Parcel in) {
        fullName = in.readString();
        email = in.readString();
        phone = in.readString();
        address = in.readString();
        userId = in.readString();
        byte tmpIsAdmin = in.readByte();
        isAdmin = tmpIsAdmin == 0 ? null : tmpIsAdmin == 1;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fullName);
        dest.writeString(email);
        dest.writeString(phone);
        dest.writeString(address);
        dest.writeString(userId);
        dest.writeByte((byte) (isAdmin == null ? 0 : isAdmin ? 1 : 2));
    }
}
