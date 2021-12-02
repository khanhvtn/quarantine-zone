package com.example.assignment2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.assignment2.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements IMapManagement, TaskLoadedCallback,
        LocationListener {
    private final String MAP_ACTIVITY_TAG = "MapsActivity";
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;
    private User currentUSer = null;
    private FirebaseFirestore db;
    private GoogleMap mMap;
    private Polyline currentPolyline;
    private LocationManager locationManager;
    private LatLng userLocationLatLng;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(MAP_ACTIVITY_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(),
                        new ActivityResultCallback<java.util.Map<String, Boolean>>() {
                            @SuppressLint("MissingPermission")
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onActivityResult(java.util.Map<String, Boolean> result) {
                                Boolean fineLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                                Boolean coarseLocationGranted = result.getOrDefault(
                                        Manifest.permission.ACCESS_COARSE_LOCATION, false);
                                if (fineLocationGranted != null && fineLocationGranted) {
                                    // Precise location access granted.
                                    locationManager
                                            .requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                                                    0, MapsActivity.this);
                                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                    // Only approximate location access granted.
                                    locationManager
                                            .requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                                                    0, MapsActivity.this);
                                } else {
                                    // No location access granted.
                                    ToastMessage(
                                            "The application need permission to run making route feature.");
                                }
                            }
                        }
                );

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });


        setContentView(R.layout.activity_maps);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //add map fragment to activity
        Fragment mapFragment = new Map();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, mapFragment, mapFragment.getClass().toString())
                .commit();
        bottomNavigationView = findViewById(R.id.bottom_navigation);


        bottomNavigationView.setOnItemSelectedListener(
                new NavigationBarView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.nvb_home:
                                switchFragmentInMainActivity(new Map());
                                break;
                            case R.id.nvb_campaigns:
                                switchFragmentInMainActivity(new ListCampaign());
                                break;
                            case R.id.nvb_report:
                                switchFragmentInMainActivity(new GenerateReport());
                                break;
                            case R.id.nvb_profile:
                                switchFragmentInMainActivity(new UserProfile());
                                break;
                        }
                        return true;
                    }
                });

    }

    @Override
    protected void onStart() {
        Log.i(MAP_ACTIVITY_TAG, "onStart");
        super.onStart();

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(MAP_ACTIVITY_TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(MAP_ACTIVITY_TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(MAP_ACTIVITY_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(MAP_ACTIVITY_TAG, "onDestroy");
    }

    @Override
    public void UpdateBottomNavigationBar() {
        if (currentUSer != null) {
            if (currentUSer.getAdmin() != null) {
                bottomNavigationView.getMenu().findItem(R.id.nvb_report).setVisible(true);
            } else {
                bottomNavigationView.getMenu().findItem(R.id.nvb_report).setVisible(false);
            }
            bottomNavigationView.getMenu().findItem(R.id.nvb_profile).setVisible(true);
            bottomNavigationView.getMenu().findItem(R.id.nvb_campaigns).setVisible(true);
        } else {
            bottomNavigationView.getMenu().findItem(R.id.nvb_report).setVisible(false);
            bottomNavigationView.getMenu().findItem(R.id.nvb_profile).setVisible(false);
            bottomNavigationView.getMenu().findItem(R.id.nvb_campaigns).setVisible(false);
        }
        bottomNavigationView.getMenu().findItem(R.id.nvb_home).setChecked(true);
    }

    @Override
    public User getCurrentUser() {
        return currentUSer;
    }

    @Override
    public void setCurrentUser(User user) {
        currentUSer = user;
    }

    @Override
    public void switchFragmentInMainActivity(Fragment fragment) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (!fragment.getClass().toString().equals(currentFragment.getTag())) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in,  // enter
                            R.anim.fade_out,  // exit
                            R.anim.fade_in,   // popEnter
                            R.anim.slide_out  // popExit
                    )
                    .setReorderingAllowed(true)
                    .replace(R.id.frame_layout, fragment, fragment.getClass().toString())
                    .commit();
        }
    }

    @Override
    public void setMap(GoogleMap map) {
        this.mMap = map;
    }

    @Override
    public GoogleMap getMap() {
        return this.mMap;
    }

    @Override
    public void setCurrentPolyline(Polyline polyline) {
        this.currentPolyline = polyline;
    }

    @Override
    public Polyline getCurrentPolyline() {
        return this.currentPolyline;
    }

    @Override
    public LatLng getUserLocation() {
        return this.userLocationLatLng;

    }

    @Override
    public void onTaskDone(Object... values) {

        if (currentPolyline != null) {
            currentPolyline.remove();
        }
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocationLatLng, 17));
    }

    private void ToastMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        userLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
    }
}