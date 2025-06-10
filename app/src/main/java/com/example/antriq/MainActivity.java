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

import com.google.android.gms.common.SignInButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // Jika sudah login, langsung ke dashboard
        if (mAuth.getCurrentUser() != null) {
            redirectBasedOnRole();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initLoginViews();
    }

    private void redirectBasedOnRole() {
        String uid = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        userRef.get().addOnSuccessListener(snapshot -> {
            User user = snapshot.getValue(User.class);
            if (user != null && "admin".equalsIgnoreCase(user.role)) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                startActivity(new Intent(this, UserDashboardActivity.class));
            }
            finish();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Gagal mengambil data user: " + e.getMessage(),
                        Toast.LENGTH_LONG).show()
        );
    }

    private void initLoginViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        SignInButton btnGoogleSignIn = findViewById(R.id.btnGoogleLogin);

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
                                            startActivity(new Intent(this, AdminDashboardActivity.class));
                                        } else {
                                            startActivity(new Intent(this, UserDashboardActivity.class));
                                        }
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Data user tidak ditemukan", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(this, "Gagal mengambil data user", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : "Login gagal, coba lagi";
                            Toast.makeText(this, "Login gagal: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        btnGoogleSignIn.setOnClickListener(v ->
                startActivity(new Intent(this, GoogleSignInActivity.class))
        );
    }
}
