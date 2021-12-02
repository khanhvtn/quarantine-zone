package com.example.assignment2;

import androidx.fragment.app.Fragment;

import com.example.assignment2.models.User;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.firestore.ListenerRegistration;

public interface IMapManagement {
    public void UpdateBottomNavigationBar();

    public User getCurrentUser();

    public void setCurrentUser(User user);

    public void switchFragmentInMainActivity(Fragment fragment);

    public void setMap(GoogleMap map);

    public GoogleMap getMap();

    public void setCurrentPolyline(Polyline polyline);

    public Polyline getCurrentPolyline();

    public LatLng getUserLocation();
    public ListenerRegistration getListenerRegistrationNotification();

}
