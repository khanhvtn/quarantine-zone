package com.example.assignment2;

import androidx.fragment.app.Fragment;

public interface IMapManagement {
    public void UpdateBottomNavigationBar();
    public User getCurrentUser();
    public void setCurrentUser(User user);
    public void switchFragmentInMainActivity(Fragment fragment);
}
