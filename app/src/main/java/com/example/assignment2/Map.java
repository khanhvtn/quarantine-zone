package com.example.assignment2;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Map extends Fragment {

    private FirebaseFirestore db;
    private GoogleMap mMap;
    private Button btnLogin, btnRegister;
    private ImageButton btnLogout;
    private LinearLayoutCompat wrapperBtn;
    private EditText edtSearch;
    private FirebaseAuth mAuth;
    private FragmentManager fm;
    private IMapManagement listener;
    private BottomNavigationView bottomNavigationView;
    private ActivityResultLauncher userResultLauncher;

    public Map() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Map", "OnCreate");
        listener = (IMapManagement) getActivity();
        userResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getData() != null) {
                            User user = result.getData().getParcelableExtra("user");
                            listener.setCurrentUser(user);
                            UpdateUIUserLogin();
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        //firebase
        db = FirebaseFirestore.getInstance();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //declare fields
        wrapperBtn = view.findViewById(R.id.map_wrapperBtn);
        edtSearch = view.findViewById(R.id.map_edtSearch);
        btnLogin = view.findViewById(R.id.map_btnLogin);
        btnRegister = view.findViewById(R.id.map_btnRegister);
        bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);

        //config fragment
        fm = getParentFragmentManager();
        FragmentTransaction ft =
                fm.beginTransaction().setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                );

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userResultLauncher.launch(new Intent(getContext(), LoginActivity.class));
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userResultLauncher.launch(new Intent(getContext(), RegisterActivity.class));
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


                //set onClick Map
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        Intent intent = new Intent(getContext(), CreateCampaignActivity.class);
                        intent.putExtra("latitude", latLng.latitude);
                        intent.putExtra("longitude", latLng.longitude);
                        startActivity(intent);
                    }
                });
            }
        });
        return view;

    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i("Map", "OnStart");
        UpdateUIUserLogin();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Map", "OnDestroy");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("Map", "OnStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("Map", "OnPause");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void UpdateUIUserLogin() {
        listener = (IMapManagement) getActivity();
        if (listener.getCurrentUser() != null) {
            wrapperBtn.setVisibility(View.GONE);
        } else {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                wrapperBtn.setVisibility(View.GONE);
                db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                User user = documentSnapshot.toObject(User.class);
                                listener.setCurrentUser(user);
                                listener.UpdateUIUserLogin();
                            }
                        });
            } else {
                wrapperBtn.setVisibility(View.VISIBLE);
            }

        }
    }
}