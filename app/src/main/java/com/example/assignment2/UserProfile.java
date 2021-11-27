package com.example.assignment2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class UserProfile extends Fragment {

    private AppCompatEditText edtFullName, edtPhone, edtAddress;
    private AppCompatButton btnChange, btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private IMapManagement listener;
    private String mode = "view";
    private AlertDialog loadingProgress;


    public UserProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //generate progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.loading_progress).setCancelable(false);
        loadingProgress = builder.create();

        //firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        edtFullName = view.findViewById(R.id.profile_edtFullName);
        edtPhone = view.findViewById(R.id.profile_edtPhone);
        edtAddress = view.findViewById(R.id.profile_edtAddress);
        btnChange = view.findViewById(R.id.profile_btnChange);
        btnLogout = view.findViewById(R.id.profile_btnLogout);
        FragmentManager fragmentManager =
                getParentFragmentManager();

        //get activity
        listener = (IMapManagement) getActivity();

        //set listener
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                listener.setCurrentUser(null);
                listener.UpdateBottomNavigationBar();
                listener.switchFragmentInMainActivity(new Map());
            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode.equals("view")) {
                    toggleEditMode();
                } else {
                    if (isUserChangeInfo()) {
                        loadingProgress.show();
                        User updatedUserInfo = listener.getCurrentUser();
                        updatedUserInfo.setFullName(edtFullName.getText().toString().trim());
                        updatedUserInfo.setPhone(edtPhone.getText().toString().trim());
                        updatedUserInfo.setAddress(edtAddress.getText().toString().trim());
                        db.collection("users").document(mAuth.getCurrentUser().getUid())
                                .set(updatedUserInfo).addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getContext(), "Change Successfully!!!",
                                                Toast.LENGTH_SHORT).show();
                                        updateEdtText(updatedUserInfo);
                                        toggleEditMode();
                                        loadingProgress.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(),
                                        "Something went wrong. Please try again!!!",
                                        Toast.LENGTH_SHORT).show();
                                toggleEditMode();
                                loadingProgress.dismiss();
                            }
                        });
                    } else {
                        toggleEditMode();
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        IMapManagement listener = (IMapManagement) getActivity();
        User user = listener.getCurrentUser();
        updateEdtText(user);

    }

    public void updateEdtText(User user) {
        edtFullName.setText(user.getFullName());
        edtPhone.setText(user.getPhone());
        edtAddress.setText(user.getAddress());
    }

    public Boolean isUserChangeInfo() {
        User currentUserInfo = listener.getCurrentUser();
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        if (currentUserInfo.getFullName().equals(fullName) &&
                currentUserInfo.getPhone().equals(phone) &&
                currentUserInfo.getAddress().equals(address)) {
            return false;
        } else {
            return true;
        }
    }

    public void toggleEditMode() {
        if (mode.equals("view")) {
            mode = "edit";
            btnChange.setText("Save");
            edtFullName.setEnabled(true);
            edtPhone.setEnabled(true);
            edtAddress.setEnabled(true);
        } else {
            mode = "view";
            btnChange.setText("Change");
            edtFullName.setEnabled(false);
            edtPhone.setEnabled(false);
            edtAddress.setEnabled(false);
        }
    }
}