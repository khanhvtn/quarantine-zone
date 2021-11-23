package com.example.assignment2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MapsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_maps);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //add map fragment to activity
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, new Map())
                .commit();
        bottomNavigationView = findViewById(R.id.bottom_navigation);


        bottomNavigationView.setOnItemSelectedListener(
                new NavigationBarView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.nvb_home:
                                Toast.makeText(MapsActivity.this, "Home", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            case R.id.nvb_campaigns:
                                Toast.makeText(MapsActivity.this, "Campaign", Toast.LENGTH_SHORT)
                                        .show();
                                break;
                            case R.id.nvb_report:
                                Toast.makeText(MapsActivity.this, "Report", Toast.LENGTH_SHORT)
                                        .show();
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


}