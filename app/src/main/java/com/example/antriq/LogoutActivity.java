package com.example.antriq;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LogoutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Logout Firebase
        FirebaseAuth.getInstance().signOut();

        // Arahkan kembali ke halaman login (MainActivity)
        Intent intent = new Intent(LogoutActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // hapus semua activity sebelumnya
        startActivity(intent);
        finish(); // Tutup LogoutActivity
    }
}
