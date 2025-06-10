package com.example.antriq;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
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
        ivQRCode.setVisibility(View.GONE); // awalnya sembunyi

        btnCreateQueue.setOnClickListener(v -> {
            String queueName = etQueueName.getText().toString().trim();

            if (TextUtils.isEmpty(queueName)) {
                Toast.makeText(CreateQueueActivity.this, "Nama antrian tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            createQueue(queueName);
        });
    }

    private void createQueue(String queueName) {
        String adminId = FirebaseAuth.getInstance().getUid();
        if (adminId == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        String queueId = FirebaseDatabase.getInstance().getReference("queues").push().getKey();
        if (queueId == null) {
            Toast.makeText(this, "Gagal membuat ID antrian", Toast.LENGTH_SHORT).show();
            return;
        }

        Queue newQueue = new Queue();
        newQueue.queueId = queueId;
        newQueue.name = queueName;
        newQueue.timestamp = System.currentTimeMillis();
        newQueue.adminId = adminId;

        FirebaseDatabase.getInstance().getReference("queues")
                .child(queueId)
                .setValue(newQueue)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CreateQueueActivity.this, "Antrian berhasil dibuat", Toast.LENGTH_SHORT).show();
                        generateAndShowQRCode(queueId);
                        // jangan finish, biarkan QR code terlihat
                    } else {
                        Toast.makeText(CreateQueueActivity.this, "Gagal membuat antrian", Toast.LENGTH_SHORT).show();
                    }
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
