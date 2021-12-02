package com.example.assignment2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.assignment2.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button btnRegister, btnBack;
    private EditText edtFullName, edtEmail, edtPassword, edtPhone, edtAddress;
    private IMapManagement listener;
    private AlertDialog loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_register);

        //generate progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.loading_progress).setCancelable(false);
        loadingProgress = builder.create();

        //declare field
        edtFullName = findViewById(R.id.register_edtFullName);
        edtEmail = findViewById(R.id.register_edtEmail);
        edtPassword = findViewById(R.id.register_edtPassword);
        edtPhone = findViewById(R.id.register_edtPhone);
        edtAddress = findViewById(R.id.register_edtAddress);
        btnBack = findViewById(R.id.register_btnBack);
        btnRegister = findViewById(R.id.register_btnRegister);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        //Listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgress.show();
                String fullName = edtFullName.getText().toString().trim();
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String address = edtAddress.getText().toString().trim();
                String validateResult =
                        validateUserInput(fullName, email, password, phone, address);
                if (validateResult != null) {
                    Toast.makeText(v.getContext(), validateResult, Toast.LENGTH_SHORT).show();
                    loadingProgress.dismiss();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    User newUser = new User(fullName, email, phone, address);
                                    if (task.isSuccessful()) {
                                        String userId = task.getResult().getUser().getUid();
                                        newUser.setUserId(userId);
                                        db.collection("users")
                                                .document(userId)
                                                .set(newUser).addOnCompleteListener(
                                                new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(
                                                            @NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(v.getContext(),
                                                                    "Register Successful. Please Login!!!",
                                                                    Toast.LENGTH_SHORT).show();
                                                            setResult(RESULT_OK, new Intent()
                                                                    .putExtra("user", newUser));
                                                            loadingProgress.dismiss();
                                                            finish();
                                                        } else {
                                                            Toast.makeText(v.getContext(),
                                                                    task.getException()
                                                                            .getMessage(),
                                                                    Toast.LENGTH_SHORT).show();
                                                            loadingProgress.dismiss();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(v.getContext(),
                                                task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        loadingProgress.dismiss();
                                    }
                                }
                            });
                }
            }
        });
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
}