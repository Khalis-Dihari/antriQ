package com.example.antriq;

public class User {
    public String name;
    public String email;
    public String role;

    public User() {
        // Diperlukan untuk Firebase
    }

    public User(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
