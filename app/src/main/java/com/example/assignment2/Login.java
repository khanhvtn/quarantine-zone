package com.example.assignment2;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
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

public class Login extends Fragment {
    private FirebaseAuth mAuth;
    private Button btnBack, btnLogin;
    private EditText edtEmail, edtPassword;

    public Login() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        btnBack = v.findViewById(R.id.login_btnBack);
        btnLogin = v.findViewById(R.id.login_btnLogin);
        edtEmail = v.findViewById(R.id.login_edtEmail);
        edtPassword = v.findViewById(R.id.login_edtPassword);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
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
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.i("Login", "signInWithEmail:success");
                                    IUpdateUIAuth listener = (IUpdateUIAuth)getActivity();
                                    listener.UpdateUIUserLogin(true);
                                    getActivity().onBackPressed();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.e("Login", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(v.getContext(),"Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        return v;
    }


}