package com.example.antriq;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import com.google.android.gms.common.SignInButton;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Inisialisasi Firebase
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email harus diisi");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Password harus diisi");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();

                            DatabaseReference userRef = FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(userId);

                            userRef.get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    User user = task1.getResult().getValue(User.class);

                                    if (user != null) {
                                        if ("admin".equalsIgnoreCase(user.role)) {
                                            startActivity(new Intent(MainActivity.this, AdminDashboardActivity.class));
                                        } else {
                                            startActivity(new Intent(MainActivity.this, UserDashboardActivity.class));
                                        }
                                        finish();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Data user tidak ditemukan", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Gagal mengambil data user", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : "Login gagal, coba lagi";

                            Toast.makeText(MainActivity.this, "Login gagal: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        SignInButton btnGoogleSignIn = findViewById(R.id.btnGoogleLogin);


        btnGoogleSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GoogleSignInActivity.class);
            startActivity(intent);
        });


    }}
