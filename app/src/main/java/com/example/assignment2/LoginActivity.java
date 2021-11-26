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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button btnBack, btnLogin;
    private EditText edtEmail, edtPassword;
    private IMapManagement listener;
    private FirebaseFirestore db;
    private AlertDialog loadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        //generate progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.loading_progress).setCancelable(false);

        loadingProgress = builder.create();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.login_btnBack);
        btnLogin = findViewById(R.id.login_btnLogin);
        edtEmail = findViewById(R.id.login_edtEmail);
        edtPassword = findViewById(R.id.login_edtPassword);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgress.show();
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String validateResult= validateUserInput();

                if(validateUserInput() != null){
                    Toast.makeText(v.getContext(), validateResult,
                            Toast.LENGTH_SHORT).show();
                    loadingProgress.dismiss();
                }else{
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
                                                                Intent intent = new Intent();
                                                                intent.putExtra("user", user);
                                                                setResult(RESULT_OK, intent);
                                                                loadingProgress.dismiss();
                                                                finish();
                                                            }
                                                        });
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(v.getContext(), "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        loadingProgress.dismiss();
                                    }
                                }
                            });
                }


            }
        });
    }

    private String validateUserInput() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        if (email.isEmpty()) {
            return "Email can not be blanked.";
        } else if (password.isEmpty()) {
            return "Password can not be blanked.";
        } else {
            return null;
        }
    }

}