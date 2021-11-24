package com.example.assignment2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends Fragment {
    private FirebaseAuth mAuth;
    private Button btnBack, btnLogin;
    private EditText edtEmail, edtPassword;
    private IMapManagement listener;
    private FirebaseFirestore db;

    public Login() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        btnBack = v.findViewById(R.id.login_btnBack);
        btnLogin = v.findViewById(R.id.login_btnLogin);
        edtEmail = v.findViewById(R.id.login_edtEmail);
        edtPassword = v.findViewById(R.id.login_edtPassword);
        //get activity
        listener = (IMapManagement) getActivity();
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeLogin();
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String userId = task.getResult().getUser().getUid();
                                    db.collection("users")
                                            .document(userId).get()
                                            .addOnSuccessListener(
                                                    new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(
                                                                DocumentSnapshot documentSnapshot) {
                                                            User user = documentSnapshot
                                                                    .toObject(User.class);
                                                            user.setUserId(userId);
                                                            listener.setCurrentUser(user);
                                                            listener.UpdateUIUserLogin();
                                                            closeLogin();
                                                        }
                                                    });
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(v.getContext(), "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        return v;
    }

    public void closeLogin() {
        FragmentManager fragmentManager =
                getParentFragmentManager();
        fragmentManager.beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.frame_layout, new Map()).commit();
    }


}