package com.example.antriq;

import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class UserDashboardActivity extends AppCompatActivity {

    private TextView tvQueueNumber, tvStatus, tvTimer, tvAdminName;
    private Button btnScanQR, btnLogout, btnLeaveQueue, btnOpenMaps;

    private String currentQueueId = null;
    private CountDownTimer timer;
    private final long DURATION_MS = 5 * 60 * 1000;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_user_dashboard);

        tvQueueNumber = findViewById(R.id.tvQueueNumber);
        tvStatus = findViewById(R.id.tvStatus);
        tvTimer = findViewById(R.id.tvTimer);
        tvAdminName = findViewById(R.id.tvAdminName);
        btnScanQR = findViewById(R.id.btnScanQR);
        btnLogout = findViewById(R.id.btnLogout);
        btnLeaveQueue = findViewById(R.id.btnLeaveQueue);
        btnOpenMaps = findViewById(R.id.btn_open_maps); // ✅ pindahkan ke sini

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        btnScanQR.setOnClickListener(v -> startActivity(new Intent(this, ScanQRActivity.class)));

        btnLeaveQueue.setOnClickListener(v -> {
            if (currentQueueId != null) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseDatabase.getInstance().getReference("queues")
                        .child(currentQueueId)
                        .child("users")
                        .child(uid)
                        .removeValue()
                        .addOnSuccessListener(a -> {
                            Toast.makeText(this, "Berhasil keluar dari antrian", Toast.LENGTH_SHORT).show();
                            resetUI();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Gagal keluar antrian", Toast.LENGTH_SHORT).show());
            }
        });

        // ✅ Tambahkan listener button open maps
        btnOpenMaps.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("https://maps.app.goo.gl/JfwkPEey2Kj336kDA");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                startActivity(browserIntent);
            }
        });

        checkStatus();
    }

    // (sisa kode tidak berubah)


    private void checkStatus() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("queues")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot ds) {
                        boolean found = false;
                        for (DataSnapshot q : ds.getChildren()) {
                            if (q.child("users").hasChild(uid)) {
                                found = true;
                                currentQueueId = q.getKey();
                                UserInQueue user = q.child("users").child(uid).getValue(UserInQueue.class);
                                String adminId = q.child("adminId").getValue(String.class);
                                if (user != null) {
                                    bindUI(user, adminId);
                                    listenRealtime();
                                }
                                break;
                            }
                        }
                        if (!found) resetUI();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) { }
                });
    }

    private void bindUI(UserInQueue u, String adminId) {
        btnScanQR.setVisibility(View.GONE);
        tvQueueNumber.setText("Nomor Antrian: " + format(u.number));
        tvStatus.setText("Status: " + u.status);
        btnLeaveQueue.setVisibility(u.status.equals("Selesai") || u.status.equals("Dibatalkan") ? View.VISIBLE : View.GONE);
        if ("Dipanggil".equals(u.status)) startTimer(); else stopTimer();
        btnOpenMaps.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("users").child(adminId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s) {
                        User admin = s.getValue(User.class);
                        tvAdminName.setText("Retail: " + (admin != null ? admin.name : "-"));
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void listenRealtime() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("queues")
                .child(currentQueueId)
                .child("users")
                .child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s) {
                        UserInQueue u = s.getValue(UserInQueue.class);
                        if (u != null) bindUI(u, u.role); // role unused here
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    private void resetUI() {
        btnScanQR.setVisibility(View.VISIBLE);
        tvQueueNumber.setText("Nomor Antrian: -");
        tvStatus.setText("Status: -");
        tvTimer.setVisibility(View.GONE);
        tvAdminName.setText("Retail: -");
        btnLeaveQueue.setVisibility(View.GONE);
        btnOpenMaps.setVisibility(View.GONE);
        stopTimer();
    }

    private void startTimer() {
        tvTimer.setVisibility(View.VISIBLE);
        stopTimer();
        timer = new CountDownTimer(DURATION_MS, 1000) {
            @Override public void onTick(long millis) {
                tvTimer.setText(String.format("Timer: %02d:%02d",
                        millis / (60*1000), (millis / 1000) % 60));
            }
            @Override public void onFinish() { tvTimer.setText("Timer habis"); }
        }.start();
    }

    private void stopTimer() {
        if (timer != null) timer.cancel();
    }

    private String format(int num) { return String.format("%03d", num); }
}
