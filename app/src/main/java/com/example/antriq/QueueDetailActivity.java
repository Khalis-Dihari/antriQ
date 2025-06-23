package com.example.antriq;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.*;

public class QueueDetailActivity extends AppCompatActivity
        implements QueueUserAdapter.OnUserClickListener {

    private RecyclerView recyclerView;
    private QueueUserAdapter adapter;
    private final List<UserInQueue> userList = new ArrayList<>();

    private String queueId;
    private final Handler handler = new Handler();
    private final Map<String, Runnable> timers = new HashMap<>();
    private Button btnDeleteQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_detail);

        recyclerView = findViewById(R.id.rvQueueUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QueueUserAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        btnDeleteQueue = findViewById(R.id.btnDeleteQueue);

        queueId = getIntent().getStringExtra("queueId");
        if (queueId == null || queueId.isEmpty()) {
            Toast.makeText(this, "ID Antrian tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupQueueListeners();

        btnDeleteQueue.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void setupQueueListeners() {
        DatabaseReference queueRef = FirebaseDatabase.getInstance()
                .getReference("queues").child(queueId);

        // Pantau jika antrian dihapus
        queueRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(QueueDetailActivity.this,
                            "Antrian telah dihapus", Toast.LENGTH_SHORT).show();
                    redirectToDashboard();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QueueDetailActivity.this,
                        "Gagal membaca antrian", Toast.LENGTH_SHORT).show();
            }
        });

        // Pantau data user dalam antrian
        queueRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                boolean allFinished = true;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    UserInQueue u = snap.getValue(UserInQueue.class);
                    if (u != null) {
                        u.userId = snap.getKey(); // Ambil userId dari key Firebase
                        userList.add(u);

                        String status = u.status;
                        if (status != null && (
                                status.equals("Menunggu") ||
                                        status.equals("Dipanggil") ||
                                        status.equals("Sedang Dilayani"))) {
                            allFinished = false;
                        }
                    }
                }

                adapter.setUserList(userList);

                if (userList.isEmpty() || allFinished) {
                    Toast.makeText(QueueDetailActivity.this,
                            "Antrian selesai", Toast.LENGTH_SHORT).show();
                    redirectToDashboard();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QueueDetailActivity.this,
                        "Gagal memuat data pengguna", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Hapus")
                .setMessage("Yakin ingin menghapus antrian ini? Semua data akan hilang.")
                .setPositiveButton("Hapus", (dialog, which) -> deleteQueue())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void deleteQueue() {
        FirebaseDatabase.getInstance().getReference("queues")
                .child(queueId)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Antrian berhasil dihapus", Toast.LENGTH_SHORT).show();
                    redirectToDashboard();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal menghapus antrian", Toast.LENGTH_SHORT).show();
                });
    }

    private void redirectToDashboard() {
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onUpdateStatus(UserInQueue user, String newStatus) {
        String uid = user.userId;
        FirebaseDatabase.getInstance().getReference("queues")
                .child(queueId)
                .child("users")
                .child(uid)
                .child("status")
                .setValue(newStatus);

        if ("Dipanggil".equals(newStatus)) {
            startCallTimer(uid);
        } else {
            cancelCallTimer(uid);
        }
    }

    @Override
    public void onDelete(UserInQueue user) {
        String uid = user.userId;
        FirebaseDatabase.getInstance().getReference("queues")
                .child(queueId)
                .child("users")
                .child(uid)
                .removeValue();
        cancelCallTimer(uid);
    }

    private void startCallTimer(String uid) {
        cancelCallTimer(uid);

        Runnable task = () -> {
            FirebaseDatabase.getInstance().getReference("queues")
                    .child(queueId)
                    .child("users")
                    .child(uid)
                    .child("status")
                    .setValue("Dibatalkan");
            timers.remove(uid);
        };

        handler.postDelayed(task, 5 * 60 * 1000); // 5 menit
        timers.put(uid, task);
    }

    private void cancelCallTimer(String uid) {
        Runnable task = timers.remove(uid);
        if (task != null) {
            handler.removeCallbacks(task);
        }
    }
}
