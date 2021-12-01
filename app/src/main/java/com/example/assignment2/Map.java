package com.example.assignment2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.assignment2.directionhelpers.FetchURL;
import com.example.assignment2.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

public class Map extends Fragment implements SearchView.OnQueryTextListener {

    private FirebaseFirestore db;
    private GoogleMap mMap;
    private Button btnLogin, btnRegister;
    private LinearLayoutCompat wrapperBtn, map_wrapperSearch;
    private SearchView edtSearch;
    private RecyclerView searchResult;
    private SearchCampaignAdapter searchCampaignAdapter;
    private FirebaseAuth mAuth;
    private FirebaseStorage firebaseStorage;
    private FragmentManager fm;
    private IMapManagement listener;
    private ActivityResultLauncher userResultLauncher;
    private ClusterManager<MarkerItem> clusterManager;
    private AlertDialog loadingProgress;


    public Map() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Map", "OnCreate");

        //generate progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.loading_progress).setCancelable(false);
        loadingProgress = builder.create();

        //firebase
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        listener = (IMapManagement) getActivity();

        //register launcher
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
        Log.i("Map", "OnCreateView");
        View view = inflater.inflate(R.layout.fragment_map, container, false);


        //declare fields
        wrapperBtn = view.findViewById(R.id.map_wrapperBtn);
        map_wrapperSearch = view.findViewById(R.id.map_wrapperSearch);
        edtSearch = view.findViewById(R.id.map_edtSearch);
        btnLogin = view.findViewById(R.id.map_btnLogin);
        btnRegister = view.findViewById(R.id.map_btnRegister);
        searchResult = view.findViewById(R.id.map_edtSearchResult);


        edtSearch.setOnQueryTextListener(this);

        //config fragment
        fm = getParentFragmentManager();
        FragmentTransaction ft =
                fm.beginTransaction().setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                );

        searchResult.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener());

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
                listener.setMap(mMap);

                // Initialize the manager with the context and the map.
                // (Activity extends context, so we can pass 'this' in the constructor.)
                clusterManager = new ClusterManager<MarkerItem>(getContext(), mMap);
                new CustomClusterRender(getContext(), mMap, clusterManager);

                // Point the map's listeners at the listeners implemented by the cluster
                // manager.
                mMap.setOnCameraIdleListener(clusterManager);
                mMap.setOnMarkerClickListener(clusterManager);

                // Move camera to RMIT Vietnam Location
                // Position the map.
                LatLng rmitLocation = new LatLng(10.729567, 106.6930756);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rmitLocation, 12));
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMapToolbarEnabled(false);

                //set onClick Map
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if (listener.getCurrentUser() != null) {
                            Intent intent = new Intent(getActivity(), CreateCampaignActivity.class);
                            intent.putExtra("latitude", latLng.latitude);
                            intent.putExtra("longitude", latLng.longitude);
                            startActivity(intent);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setMessage("Please login to create a campaign.")
                                    .setCancelable(false).setPositiveButton(
                                    "Login", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            userResultLauncher.launch(new Intent(getActivity(),
                                                    LoginActivity.class));
                                        }
                                    }).setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                    }
                });
                //on cluster click
                clusterManager.setOnClusterClickListener(
                        new ClusterManager.OnClusterClickListener<MarkerItem>() {
                            @Override
                            public boolean onClusterClick(Cluster<MarkerItem> cluster) {
                                return false;
                            }
                        });
                //on Marker Click
                clusterManager.setOnClusterItemClickListener(
                        new ClusterManager.OnClusterItemClickListener<MarkerItem>() {
                            @Override
                            public boolean onClusterItemClick(MarkerItem marker) {
                                loadingProgress.show();
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                View view = getLayoutInflater()
                                        .inflate(R.layout.custom_info_window, null);
                                AppCompatImageView imageView =
                                        view.findViewById(R.id.win_imageView);
                                AppCompatTextView txtCampaignName =
                                        view.findViewById(R.id.win_txtCampaignName);
                                AppCompatTextView txtStartDate =
                                        view.findViewById(R.id.win_txtStartDate);
                                AppCompatTextView txtNumVolunteer =
                                        view.findViewById(R.id.win_txtNumVolunteer);
                                AppCompatButton btnCancel =
                                        view.findViewById(R.id.win_btnCancel);
                                AppCompatButton btnDetail =
                                        view.findViewById(R.id.win_btnDetail);
                                AppCompatButton btnMakeRoute =
                                        view.findViewById(R.id.win_btnMakeRoute);
                                builder.setView(view);
                                AlertDialog infoDialog = builder.create();

                                btnCancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        infoDialog.dismiss();
                                    }
                                });
                                btnDetail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        infoDialog.dismiss();
                                        Intent intent = new Intent(getContext(),
                                                CampaignDetailActivity.class);
                                        intent.putExtra("campaignName", marker.getTitle());
                                        startActivity(intent);
                                    }
                                });
                                btnMakeRoute.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ToastMessage("Make Route");
                                        new FetchURL(getContext()).execute(
                                                getUrl(listener.getUserLocation(),
                                                        marker.getPosition(), "driving"),
                                                "driving");
                                        infoDialog.dismiss();
                                    }
                                });
                                db.collection("campaigns")
                                        .whereEqualTo("campaignName", marker.getTitle()).get()
                                        .addOnSuccessListener(
                                                new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(
                                                            QuerySnapshot queryDocumentSnapshots) {
                                                        List<Campaign> campaignList =
                                                                queryDocumentSnapshots
                                                                        .toObjects(Campaign.class);
                                                        Campaign targetCampaign =
                                                                campaignList.get(0);
                                                        firebaseStorage.getReference()
                                                                .child("images/" + targetCampaign
                                                                        .getImageFileName())
                                                                .getDownloadUrl()
                                                                .addOnSuccessListener(
                                                                        new OnSuccessListener<Uri>() {
                                                                            @Override
                                                                            public void onSuccess(
                                                                                    Uri uri) {
                                                                                Glide.with(
                                                                                        getContext())
                                                                                        .load(uri)
                                                                                        .into(imageView);
                                                                                txtCampaignName
                                                                                        .setText(
                                                                                                targetCampaign
                                                                                                        .getCampaignName());
                                                                                txtStartDate
                                                                                        .setText(
                                                                                                targetCampaign
                                                                                                        .getStartDate());
                                                                                txtNumVolunteer
                                                                                        .setText(
                                                                                                "100");
                                                                                loadingProgress
                                                                                        .dismiss();
                                                                                infoDialog.show();
                                                                            }
                                                                        }).addOnFailureListener(
                                                                new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(
                                                                            @NonNull Exception e) {
                                                                        Log.e(getContext()
                                                                                        .getClass()
                                                                                        .toString(),
                                                                                e.getMessage());
                                                                        loadingProgress.dismiss();
                                                                    }
                                                                });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(getContext().getClass().toString(), e.getMessage());
                                        loadingProgress.dismiss();
                                    }
                                });

                                return true;
                            }
                        });
                //get all campaigns
                db.collection("campaigns").get().addOnCompleteListener(
                        new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    List<Campaign> campaignList =
                                            task.getResult().toObjects(Campaign.class);
                                    //config search campaign adapter
                                    searchCampaignAdapter =
                                            new SearchCampaignAdapter(searchResult, mMap,
                                                    campaignList, edtSearch);
                                    searchResult.setLayoutManager(
                                            new LinearLayoutManager(getContext()));
                                    searchResult.setAdapter(searchCampaignAdapter);
                                    for (Campaign campaign : campaignList) {
                                        MarkerItem markerItem = new MarkerItem(
                                                campaign.getLatitude(), campaign.getLongitude(),
                                                campaign.getCampaignName(),
                                                campaign.getDescription());
                                        clusterManager.addItem(markerItem);
                                    }
                                    clusterManager.cluster();
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
        if (mMap != null) {
            //update campaign marker
            mMap.clear();
            clusterManager.clearItems();
            clusterManager.cluster();
            //get all campaigns
            db.collection("campaigns").get().addOnCompleteListener(
                    new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task
                                        .getResult()) {
                                    Campaign campaign =
                                            document.toObject(Campaign.class);

                                    MarkerItem markerItem = new MarkerItem(
                                            campaign.getLatitude(),
                                            campaign.getLongitude(),
                                            campaign.getCampaignName(),
                                            campaign.getDescription());
                                    clusterManager.addItem(markerItem);
                                }
                                clusterManager.cluster();
                            } else {
                                Log.d("GetAllCampaigns",
                                        "Error getting documents: ",
                                        task.getException());
                            }
                        }
                    });
        }
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

    @Override
    public boolean onQueryTextSubmit(String query) {
        edtSearch.clearFocus();
        searchCampaignAdapter.filter("");
        return false;
    }

    @Override
    public boolean onQueryTextChange(String searchText) {
        if (!searchText.isEmpty()) {
            searchResult.setVisibility(View.VISIBLE);
        } else {
            searchResult.setVisibility(View.GONE);
        }
        searchCampaignAdapter.filter(searchText);
        return false;
    }

    private void ToastMessage(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url =
                "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters +
                        "&key=" + getString(R.string.direction_api_key);
        return url;
    }


}