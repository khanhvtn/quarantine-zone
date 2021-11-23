package com.example.assignment2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Map extends Fragment  implements IUpdateUIAuth{

    private GoogleMap mMap;
    private Button btnLogin, btnRegister;
    private ImageButton btnLogout;
    private LinearLayoutCompat wrapperBtn;
    private RelativeLayout wrapperSearch;
    private EditText edtSearch;
    private FirebaseAuth mAuth;
    private FragmentManager fm;

    public Map() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //declare fields
        wrapperBtn = view.findViewById(R.id.map_wrapperBtn);
        wrapperSearch = view.findViewById(R.id.map_wrapperSearchInput);
        edtSearch = view.findViewById(R.id.map_edtSearch);
        btnLogin = view.findViewById(R.id.map_btnLogin);
        btnRegister = view.findViewById(R.id.map_btnRegister);
        btnLogout = view.findViewById(R.id.map_btnLogout);

        //config fragment

        fm = getParentFragmentManager();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft =
                        fm.beginTransaction().setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out  // popExit
                        );

                ft.replace(R.id.frame_layout, new Login());
                ft.commit();
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft =
                        fm.beginTransaction().setCustomAnimations(
                                R.anim.slide_in,  // enter
                                R.anim.fade_out,  // exit
                                R.anim.fade_in,   // popEnter
                                R.anim.slide_out  // popExit
                        );
                ft.replace(R.id.frame_layout, new Register());
                ft.commit();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                UpdateUIUserLogin();
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                //Map
                mMap = googleMap;
                // Add a marker in Sydney and move the camera
                LatLng sydney = new LatLng(-34, 151);
                mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
        });
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        UpdateUIUserLogin();
    }

    @Override
    public void UpdateUIUserLogin() {
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            wrapperBtn.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            wrapperBtn.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
        }
    }
}