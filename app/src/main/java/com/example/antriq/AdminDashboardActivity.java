package com.example.antriq;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements QueueAdapter.OnQueueClickListener {

    private TextView tvWelcomeAdmin;
    private Button btnAddQueue, btnLogout;
    private RecyclerView rvQueues;
    private QueueAdapter queueAdapter;
    private List<Queue> queueList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvWelcomeAdmin = findViewById(R.id.tvWelcomeAdmin);
        btnAddQueue = findViewById(R.id.btnAddQueue);
        btnLogout = findViewById(R.id.btnLogout);
        rvQueues = findViewById(R.id.rvQueues);

        rvQueues.setLayoutManager(new LinearLayoutManager(this));
        queueAdapter = new QueueAdapter(queueList, this);
        rvQueues.setAdapter(queueAdapter);

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnAddQueue.setOnClickListener(v ->
                startActivity(new Intent(this, CreateQueueActivity.class)));

        loadAdminName();
        loadQueues();
    }

    private void loadAdminName() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            tvWelcomeAdmin.setText("Selamat datang, Admin " + user.name);
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminDashboardActivity.this, "Gagal mengambil data admin", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadQueues() {
        String currentAdminId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("queues")
                .orderByChild("adminId").equalTo(currentAdminId)
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        queueList.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Queue queue = dataSnapshot.getValue(Queue.class);
                            if (queue != null) {
                                queue.queueId = dataSnapshot.getKey();
                                queueList.add(queue);
                            }
                        }
                        queueAdapter.notifyDataSetChanged();
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AdminDashboardActivity.this, "Gagal memuat antrian", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onQueueClick(Queue queue) {
        Intent intent = new Intent(this, QueueDetailActivity.class);
        intent.putExtra("queueId", queue.queueId);
        startActivity(intent);
    }
}
