package com.example.assignment2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class Register extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button btnRegister, btnBack;
    private EditText edtFullName, edtEmail, edtPassword, edtPhone, edtAddress;
    IUpdateUIAuth listener;

    public Register() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register, container, false);
        //declare field
        edtFullName = v.findViewById(R.id.register_edtFullName);
        edtEmail = v.findViewById(R.id.register_edtEmail);
        edtPassword = v.findViewById(R.id.register_edtPassword);
        edtPhone = v.findViewById(R.id.register_edtPhone);
        edtAddress = v.findViewById(R.id.register_edtAddress);
        btnBack = v.findViewById(R.id.register_btnBack);
        btnRegister = v.findViewById(R.id.register_btnRegister);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Get activity
        listener = (IUpdateUIAuth) getActivity();

        //Listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               closeRegister();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = edtFullName.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String address = edtAddress.getText().toString().trim();
                String validateResult =
                        validateUserInput(fullName, email, password, phone, address);
                if (validateResult != null) {
                    Toast.makeText(v.getContext(), validateResult, Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    User newUser = new User(fullName, email, phone, address);
                                    if (task.isSuccessful()) {
                                        db.collection("users")
                                                .document(task.getResult().getUser().getUid())
                                                .set(newUser).addOnCompleteListener(
                                                new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(
                                                            @NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            listener.UpdateUIUserLogin();
                                                            Toast.makeText(v.getContext(),
                                                                    "Register Successful. Please Login!!!",
                                                                    Toast.LENGTH_SHORT).show();
                                                            closeRegister();
                                                        } else {
                                                            Toast.makeText(v.getContext(),
                                                                    task.getException()
                                                                            .getMessage(),
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(v.getContext(),
                                                task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
        return v;
    }

    public String validateUserInput(String fullName, String email, String password, String phone,
                                    String address) {
        if (fullName.isEmpty()) {
            return "Full name can not be blanked";
        }
        if (email.isEmpty()) {
            return "Email can not be blanked";
        }
        if (password.isEmpty()) {
            return "Password can not be blanked";
        }
        if (phone.isEmpty()) {
            return "Phone can not be blanked";
        }
        if (address.isEmpty()) {
            return "Address can not be blanked";
        }

        return null;
    }

    public void closeRegister(){
        FragmentManager fragmentManager =
                getParentFragmentManager();
        fragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).remove(fragmentManager.findFragmentById(R.id.fragment_register)).commit();
    }
}