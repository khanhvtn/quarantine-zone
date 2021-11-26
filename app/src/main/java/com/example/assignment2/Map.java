package com.example.assignment2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;

public class Map extends Fragment {

    private FirebaseFirestore db;
    private GoogleMap mMap;
    private Button btnLogin, btnRegister;
    private LinearLayoutCompat wrapperBtn;
    private EditText edtSearch;
    private FirebaseAuth mAuth;
    private FragmentManager fm;
    private IMapManagement listener;
    private ActivityResultLauncher userResultLauncher;
    private ClusterManager<MarkerItem> clusterManager;

    public Map() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Map", "OnCreate");
        //firebase
        db = FirebaseFirestore.getInstance();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        listener = (IMapManagement) getActivity();
        userResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getData() != null) {
                            User user = result.getData().getParcelableExtra("user");
                            listener.setCurrentUser(user);
                            listener.UpdateBottomNavigationBar();
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


        //declare fields
        wrapperBtn = view.findViewById(R.id.map_wrapperBtn);
        edtSearch = view.findViewById(R.id.map_edtSearch);
        btnLogin = view.findViewById(R.id.map_btnLogin);
        btnRegister = view.findViewById(R.id.map_btnRegister);

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
                userResultLauncher.launch(new Intent(getActivity(), LoginActivity.class));
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userResultLauncher.launch(new Intent(getActivity(), RegisterActivity.class));
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                //Map
                mMap = googleMap;

                // Initialize the manager with the context and the map.
                // (Activity extends context, so we can pass 'this' in the constructor.)
                clusterManager = new ClusterManager<MarkerItem>(getContext(), mMap);
                CustomClusterRender renderer = new CustomClusterRender(getContext(), mMap, clusterManager);
                clusterManager.setRenderer(renderer);

                // Point the map's listeners at the listeners implemented by the cluster
                // manager.
                mMap.setOnCameraIdleListener(clusterManager);
                mMap.setOnMarkerClickListener(clusterManager);

                // Move camera to RMIT Vietnam Location
                // Position the map.

                LatLng rmit = new LatLng(10.729567, 106.6930756);
                int height = 70;
                int width = 70;
                BitmapDrawable bitmapdraw =
                        (BitmapDrawable) getResources().getDrawable(R.drawable.icon_marker);
                Bitmap b = bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                mMap.addMarker(
                        new MarkerOptions().position(rmit).title("Marker in RMIT Vietnam").icon(
                                BitmapDescriptorFactory.fromBitmap(smallMarker)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rmit, 15));
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);


                //set onClick Map
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        Intent intent = new Intent(getActivity(), CreateCampaignActivity.class);
                        intent.putExtra("latitude", latLng.latitude);
                        intent.putExtra("longitude", latLng.longitude);
                        startActivity(intent);
                    }
                });
                //add marker campain
                //get all campaigns
                db.collection("campaigns").get().addOnCompleteListener(
                        new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Campaign campaign = document.toObject(Campaign.class);

                                        MarkerItem markerItem = new MarkerItem(
                                                campaign.getLatitude(), campaign.getLongitude(),
                                                campaign.getCampaignName(),
                                                campaign.getDescription());
                                        clusterManager.addItem(markerItem);
                                    }
                                } else {
                                    Log.d("GetAllCampaigns", "Error getting documents: ",
                                            task.getException());
                                }
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
        listener.UpdateBottomNavigationBar();
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
                                listener.UpdateBottomNavigationBar();
                            }
                        });
            } else {
                wrapperBtn.setVisibility(View.VISIBLE);
            }

        }
    }

    private void addItems() {

        // Set some lat/lng coordinates to start with.
        double lat = 51.5145160;
        double lng = -0.1270060;

        // Add ten cluster items in close proximity, for purposes of this example.
        for (int i = 0; i < 10; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            MarkerItem offsetItem = new MarkerItem(lat, lng, "Title " + i, "Snippet " + i);
            clusterManager.addItem(offsetItem);
        }
    }
}