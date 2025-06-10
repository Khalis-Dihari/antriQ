package com.example.antriq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.Task;

public class GoogleSignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signIn();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Login Google gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, authTask -> {
                    if (authTask.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            String name = firebaseUser.getDisplayName();
                            String email = firebaseUser.getEmail();

                            DatabaseReference userRef = dbRef.child(uid);
                            userRef.get().addOnCompleteListener(snapshotTask -> {
                                if (!snapshotTask.getResult().exists()) {
                                    User user = new User(name, email, "user");
                                    userRef.setValue(user);
                                }
                                // Setelah login sukses, lanjut ke dashboard
                                startActivity(new Intent(GoogleSignInActivity.this, UserDashboardActivity.class));
                                finish();
                            });
                        }
                    } else {
                        Toast.makeText(this, "Autentikasi Gagal", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
