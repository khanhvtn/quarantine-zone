package com.example.assignment2;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
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
import com.example.assignment2.fragments.GenerateReport;
import com.example.assignment2.fragments.ListCampaign;
import com.example.assignment2.fragments.Map;
import com.example.assignment2.fragments.UserProfile;
import com.example.assignment2.models.Campaign;
import com.example.assignment2.models.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class MapsActivity extends AppCompatActivity implements IMapManagement, TaskLoadedCallback,
        LocationListener {
    private Integer idNotification = 1;
    private final String CHANNEL_ID = "CAMPAIGN_NOTIFICATION";
    private final String MAP_ACTIVITY_TAG = "MapsActivity";
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;
    private User currentUSer = null;
    private FirebaseFirestore db;
    private GoogleMap mMap;
    private Polyline currentPolyline;
    private LocationManager locationManager;
    private LatLng userLocationLatLng;
    private NotificationManagerCompat notificationManager;
    private Notification campaignNotification;
    private NotificationCompat.Builder builder;
    private ListenerRegistration
            listenerRegistrationNotification;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(MAP_ACTIVITY_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        //create notification
        notificationManager = NotificationManagerCompat.from(this);
        createNotificationChannel();


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
        if (mAuth.getCurrentUser() != null) {
            listenerRegistrationNotification =
                    db.collection("users").document(mAuth.getCurrentUser().getUid())
                            .collection(getResources().getString(R.string.notifications_collection))
                            .addSnapshotListener(
                                    new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value,
                                                            @Nullable
                                                                    FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(MAP_ACTIVITY_TAG, "listen:error", e);
                                                return;
                                            }

                                            for (DocumentChange dc : value.getDocumentChanges()) {
                                                switch (dc.getType()) {
                                                    case ADDED:
//                                            Log.d(MAP_ACTIVITY_TAG, "New city: " + dc.getDocument().getData());

                                                        String notificationId =
                                                                dc.getDocument().getId();
                                                        com.example.assignment2.models.Notification
                                                                newNotification =
                                                                dc.getDocument().toObject(
                                                                        com.example.assignment2.models.Notification.class);
                                                        String newInformation =
                                                                GenerateNewCampaignInformation(
                                                                        newNotification);
                                                        builder = new NotificationCompat.Builder(
                                                                MapsActivity.this, CHANNEL_ID)
                                                                .setSmallIcon(R.drawable.app_logo)
                                                                .setContentTitle(
                                                                        newNotification
                                                                                .getOldCampaignName() +
                                                                                " has changed information")
                                                                .setContentText(newInformation)
                                                                .setStyle(
                                                                        new NotificationCompat.BigTextStyle()
                                                                                .bigText(
                                                                                        newInformation))
                                                                .setPriority(
                                                                        NotificationCompat.PRIORITY_DEFAULT);
                                                        campaignNotification = builder.build();
                                                        notificationManager
                                                                .notify(idNotification++,
                                                                        campaignNotification);
                                                        dc.getDocument().getReference().delete()
                                                                .addOnFailureListener(
                                                                        new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(
                                                                                    @NonNull
                                                                                            Exception e) {
                                                                                ToastMessage(
                                                                                        e.getMessage());
                                                                            }
                                                                        });
                                                        break;
                                                    case MODIFIED:
                                                        Log.d(MAP_ACTIVITY_TAG,
                                                                "Modified city: " +
                                                                        dc.getDocument().getData());
                                                        break;
                                                    case REMOVED:
                                                        Log.d(MAP_ACTIVITY_TAG,
                                                                "Removed city: " +
                                                                        dc.getDocument().getData());
                                                        break;
                                                }
                                            }
                                        }
                                    });
        }
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
    public ListenerRegistration getListenerRegistrationNotification() {
        return listenerRegistrationNotification;
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String GenerateNewCampaignInformation(
            com.example.assignment2.models.Notification newNotification) {
        Campaign campaign = newNotification.getNewInformation();
        //generate new update information
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String
                .format("Campaign Name: %s\nOrganization: %s\nStart Date: %s\nNumber of Volunteers: %d\nNumber of Tested People: %d",
                        campaign.getCampaignName(), campaign.getOrganization(),
                        campaign.getStartDate(), campaign.getListVolunteers().size(),
                        campaign.getNumberTestedPeople()));
        return stringBuilder.toString();
    }
}