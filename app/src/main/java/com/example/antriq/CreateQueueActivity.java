package com.example.antriq;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class CreateQueueActivity extends AppCompatActivity {

    private EditText etQueueName;
    private Button btnCreateQueue;
    private ImageView ivQRCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_queue);

        etQueueName = findViewById(R.id.etQueueName);
        btnCreateQueue = findViewById(R.id.btnCreateQueue);
        ivQRCode = findViewById(R.id.ivQRCode);
        ivQRCode.setVisibility(View.GONE);

        btnCreateQueue.setOnClickListener(v -> {
            generateQueueNameAndCreate();
        });

    }

    private void listenForUserJoin(String queueId) {
        FirebaseDatabase.getInstance().getReference("queues")
                .child(queueId)
                .child("users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Saat user pertama bergabung ke antrian, pindah ke dashboard admin
                            Intent intent = new Intent(CreateQueueActivity.this, AdminDashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CreateQueueActivity.this, "Gagal mendeteksi pengguna", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createQueue(String queueName) {
        String adminId = FirebaseAuth.getInstance().getUid();
        if (adminId == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference("queues")
                .get()
                .addOnSuccessListener(snapshot -> {
                    int count = (int) snapshot.getChildrenCount();
                    String numberedName = String.format("Antrian %03d", count + 1);

                    String queueId = FirebaseDatabase.getInstance().getReference("queues").push().getKey();
                    if (queueId == null) {
                        Toast.makeText(this, "Gagal membuat ID antrian", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Queue newQueue = new Queue();
                    newQueue.queueId = queueId;
                    newQueue.name = numberedName;
                    newQueue.timestamp = System.currentTimeMillis();
                    newQueue.adminId = adminId;

                    FirebaseDatabase.getInstance().getReference("queues")
                            .child(queueId)
                            .setValue(newQueue)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(CreateQueueActivity.this, "Antrian berhasil dibuat", Toast.LENGTH_SHORT).show();
                                    generateAndShowQRCode(queueId);
                                    listenForUserJoin(queueId); // ✅ Mulai deteksi user setelah antrian berhasil dibuat
                                } else {
                                    Toast.makeText(CreateQueueActivity.this, "Gagal membuat antrian", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal menghitung jumlah antrian", Toast.LENGTH_SHORT).show();
                });
    }

    private void generateQueueNameAndCreate() {
        FirebaseDatabase.getInstance().getReference("queues")
                .get()
                .addOnSuccessListener(snapshot -> {
                    long count = snapshot.getChildrenCount();
                    String autoName = String.format("%03d", count + 1); // 3 digit format
                    createQueue(autoName);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal menghitung antrian", Toast.LENGTH_SHORT).show();
                });
    }

    private void generateAndShowQRCode(String data) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(data, com.google.zxing.BarcodeFormat.QR_CODE, 400, 400);
            ivQRCode.setImageBitmap(bitmap);
            ivQRCode.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membuat QR Code", Toast.LENGTH_SHORT).show();
        }
    }
}
