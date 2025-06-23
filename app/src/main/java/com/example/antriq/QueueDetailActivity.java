package com.example.antriq;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class QueueDetailActivity extends AppCompatActivity {

    private RecyclerView rvQueueUsers;
    private QueueUserAdapter adapter;
    private final List<UserInQueue> userList = new ArrayList<>();
    private String queueId;
    private Button btnBack, btnDeleteQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_detail);

        // Ambil queueId dari intent
        if (getIntent() != null && getIntent().hasExtra("queueId")) {
            queueId = getIntent().getStringExtra("queueId");
        } else {
            Toast.makeText(this, "Queue ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi UI
        rvQueueUsers = findViewById(R.id.rvQueueUsers);
        btnBack = findViewById(R.id.btnBack);
        btnDeleteQueue = findViewById(R.id.btnDeleteQueue);

        // Set RecyclerView
        rvQueueUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QueueUserAdapter(this, userList, queueId);
        rvQueueUsers.setAdapter(adapter);

        // Load data user dalam antrian
        loadUsersInQueue();

        // Tombol kembali
        btnBack.setOnClickListener(v -> finish());

        // Tombol hapus antrian
        btnDeleteQueue.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Hapus Antrian")
                    .setMessage("Apakah Anda yakin ingin menghapus antrian ini?")
                    .setPositiveButton("Ya", (dialog, which) -> deleteQueue())
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    private void loadUsersInQueue() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("queues")
                .child(queueId)
                .child("users");

        ref.orderByChild("number").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    UserInQueue user = ds.getValue(UserInQueue.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QueueDetailActivity.this, "Gagal memuat pengguna", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteQueue() {
        DatabaseReference queueRef = FirebaseDatabase.getInstance().getReference("queues").child(queueId);
        queueRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Antrian berhasil dihapus", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal menghapus antrian", Toast.LENGTH_SHORT).show();
                });
    }
}
