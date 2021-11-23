package com.example.assignment2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;


public class CreateCampaign extends Fragment {
    private LatLng newLocation;
    private TextView txtLocation;
    public CreateCampaign() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle result = getArguments();
        Double latitude = result.getDouble("latitude");
        Double longitude = result.getDouble("longitude");
        newLocation = new LatLng(latitude, longitude);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_campaign, container, false);
        txtLocation = view.findViewById(R.id.txtLocation);
        txtLocation.setText(newLocation.toString());
        return view;
    }
}