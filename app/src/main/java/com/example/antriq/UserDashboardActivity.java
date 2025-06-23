package com.example.antriq;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDashboardActivity extends AppCompatActivity {

    private TextView tvQueueNumber, tvStatus, tvTimer, tvAdminName;
    private Button btnScanQR;
    private Button btnLogout;

    private String currentQueueId = null;
    private CountDownTimer countDownTimer;
    private final long TIMER_DURATION_MS = 5 * 60 * 1000; // 5 menit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        tvQueueNumber = findViewById(R.id.tvQueueNumber);
        btnLogout = findViewById(R.id.btnLogout);
        tvStatus = findViewById(R.id.tvStatus);
        tvTimer = findViewById(R.id.tvTimer);
        tvAdminName = findViewById(R.id.tvAdminName);
        btnScanQR = findViewById(R.id.btnScanQR);

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(UserDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // hapus back stack
            startActivity(intent);
            finish();
        });

        btnScanQR.setOnClickListener(v -> {
            Intent intent = new Intent(UserDashboardActivity.this, ScanQRActivity.class);
            startActivity(intent);
        });

        checkUserQueueStatus();
    }

    private void checkUserQueueStatus() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("queues")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean found = false;

                        for (DataSnapshot queueSnapshot : snapshot.getChildren()) {
                            if (queueSnapshot.child("users").hasChild(uid)) {
                                found = true;
                                currentQueueId = queueSnapshot.getKey();
                                UserInQueue user = queueSnapshot.child("users").child(uid).getValue(UserInQueue.class);
                                String adminId = queueSnapshot.child("adminId").getValue(String.class);
                                updateUIWithQueueData(user);
                                loadAdminName(adminId);
                                observeUserStatusRealtime(currentQueueId, uid);
                                break;
                            }
                        }

                        if (!found) {
                            showScanQRButton();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserDashboardActivity.this, "Gagal memuat data antrian", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUIWithQueueData(UserInQueue user) {
        btnScanQR.setVisibility(View.GONE);

        if (user != null) {
            tvQueueNumber.setText("Nomor Antrian: " + formatNumber(user.number));
            tvStatus.setText("Status: " + user.status);

            if ("Dipanggil".equals(user.status)) {
                startCountdownTimer();
            } else {
                stopCountdownTimer();
                tvTimer.setVisibility(View.GONE);
            }
        }
    }

    private void observeUserStatusRealtime(String queueId, String uid) {
        FirebaseDatabase.getInstance().getReference("queues")
                .child(queueId)
                .child("users")
                .child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserInQueue user = snapshot.getValue(UserInQueue.class);
                        if (user != null) {
                            updateUIWithQueueData(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(UserDashboardActivity.this, "Gagal memantau status", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadAdminName(String adminId) {
        FirebaseDatabase.getInstance().getReference("users")
                .child(adminId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User admin = snapshot.getValue(User.class);
                        if (admin != null) {
                            tvAdminName.setText("Retail: " + admin.name);
                        } else {
                            tvAdminName.setText("Retail: -");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvAdminName.setText("Retail: -");
                    }
                });
    }

    private void showScanQRButton() {
        btnScanQR.setVisibility(View.VISIBLE);
        tvQueueNumber.setText("Nomor Antrian: -");
        tvStatus.setText("Status: -");
        tvTimer.setVisibility(View.GONE);
        tvAdminName.setText("Retail: -");

        stopCountdownTimer();
    }

    private void startCountdownTimer() {
        tvTimer.setVisibility(View.VISIBLE);

        stopCountdownTimer(); // stop jika sebelumnya masih jalan

        countDownTimer = new CountDownTimer(TIMER_DURATION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / (60 * 1000);
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format("Timer: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("Timer habis");
            }
        };

        countDownTimer.start();
    }

    private void stopCountdownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private String formatNumber(int number) {
        return String.format("%03d", number);
    }
}
