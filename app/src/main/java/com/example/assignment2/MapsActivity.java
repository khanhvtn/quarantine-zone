package com.example.assignment2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

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

public class MapsActivity extends AppCompatActivity implements IMapManagement {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;
    private User currentUSer = null;
    private FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

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
                                Toast.makeText(MapsActivity.this, "Campaign", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            case R.id.nvb_report:
                                Toast.makeText(MapsActivity.this, "Report", Toast.LENGTH_SHORT)
                                        .show();
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
        Log.i("onStart", "onStart");
        super.onStart();

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i("onResume", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("onPause", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("onStop", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy", "onDestroy");
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
        if(!fragment.getClass().toString().equals(currentFragment.getTag()))
        {
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
}