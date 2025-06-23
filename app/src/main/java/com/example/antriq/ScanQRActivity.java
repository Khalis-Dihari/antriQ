package com.example.antriq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.google.zxing.integration.android.IntentResult;

public class ScanQRActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User tidak terautentikasi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        uid = currentUser.getUid();

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan QR Antrian");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() != null) {
                String queueId = result.getContents();
                joinQueue(queueId);
            } else {
                Toast.makeText(this, "Scan dibatalkan", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void joinQueue(String queueId) {
        DatabaseReference queueRef = FirebaseDatabase.getInstance().getReference("queues").child(queueId);
        DatabaseReference userRef = queueRef.child("users").child(uid);

        queueRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(uid)) {
                    Toast.makeText(ScanQRActivity.this, "Kamu sudah berada dalam antrian ini", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    long queueNumber = snapshot.getChildrenCount() + 1;

                    String name = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User";
                    String email = currentUser.getEmail();

                    UserInQueue newUser = new UserInQueue(name, email, "user", "Menunggu", (int) queueNumber, uid);

                    userRef.setValue(newUser)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ScanQRActivity.this, "Berhasil masuk antrian", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ScanQRActivity.this, UserDashboardActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(ScanQRActivity.this, "Gagal masuk antrian", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ScanQRActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
