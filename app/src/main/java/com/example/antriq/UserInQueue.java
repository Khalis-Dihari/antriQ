package com.example.antriq;

public class UserInQueue {
    public String name;
    public String email;
    public String role;
    public String status;
    public int number;
    public String userId;
    public long timestamp; // <== TAMBAH INI

    public UserInQueue() {}

    public UserInQueue(String name, String email, String role, String status, int number, String userId, long timestamp) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.number = number;
        this.userId = userId;
        this.timestamp = timestamp; // <== INISIALISASI
    }
}
