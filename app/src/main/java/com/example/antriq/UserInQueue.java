package com.example.antriq;

public class UserInQueue {
    public String name;
    public String email;
    public String role;
    public String status;
    public int number;

    public UserInQueue() {
        // Wajib untuk Firebase
    }

    public UserInQueue(String name, String email, String role, String status, int number) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.number = number;
    }
}
