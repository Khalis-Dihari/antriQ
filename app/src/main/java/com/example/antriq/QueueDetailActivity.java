package com.example.antriq;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.*;

public class QueueDetailActivity extends AppCompatActivity implements QueueUserAdapter.OnUserClickListener {

    private RecyclerView recyclerView;
    private QueueUserAdapter adapter;
    private List<UserInQueue> userList = new ArrayList<>();
    private String queueId;

    private Handler handler = new Handler();
    private final Map<String, Runnable> timers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue_detail);

        recyclerView = findViewById(R.id.rvQueueUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new QueueUserAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        queueId = getIntent().getStringExtra("queueId");
        if (queueId == null || queueId.isEmpty()) {
            Toast.makeText(this, "ID Antrian tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchUsersInQueue(queueId);
    }

    private void fetchUsersInQueue(String queueId) {
        FirebaseDatabase.getInstance().getReference("queues")
                .child(queueId)
                .child("users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            UserInQueue user = userSnapshot.getValue(UserInQueue.class);
                            if (user != null) {
                                userList.add(user);
                            }
                        }
                        adapter.setUserList(userList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(QueueDetailActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onUpdateStatus(UserInQueue user, String newStatus) {
        String uid = user.email.replace(".", "_");
        FirebaseDatabase.getInstance()
                .getReference("queues")
                .child(queueId)
                .child("users")
                .child(uid)
                .child("status")
                .setValue(newStatus);

        if ("Dipanggil".equals(newStatus)) startCallTimer(user);
        else cancelCallTimer(user);
    }

    /* dipanggil adapter ketika admin tekan hapus */
    @Override
    public void onDelete(UserInQueue user) {
        String uid = user.email.replace(".", "_");
        FirebaseDatabase.getInstance()
                .getReference("queues")
                .child(queueId)
                .child("users")
                .child(uid)
                .removeValue();
    }

    private void startCallTimer(UserInQueue user) {
        String userId = user.email.replace(".", "_");
        cancelCallTimer(user);

        Runnable runnable = () -> {
            FirebaseDatabase.getInstance().getReference("queues")
                    .child(queueId)
                    .child("users")
                    .child(userId)
                    .child("status")
                    .setValue("Dibatalkan");
            timers.remove(userId);
        };

        handler.postDelayed(runnable, 5 * 60 * 1000); // 5 menit
        timers.put(userId, runnable);
    }

    private void cancelCallTimer(UserInQueue user) {
        String userId = user.email.replace(".", "_");
        Runnable runnable = timers.get(userId);
        if (runnable != null) {
            handler.removeCallbacks(runnable);
            timers.remove(userId);
        }
    }
}
