// File: com.example.antriq.model.UserInQueue.java
package com.example.antriq;

public class UserInQueue {
    public String name;
    public String email;
    public String role;
    public String status;
    public int number;
    public String userId;

    public UserInQueue() {
        // Diperlukan oleh Firebase
    }

    public UserInQueue(String name, String email, String role, String status, int number, String userId) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.number = number;
        this.userId = userId;
    }

    // Optional: Getter-setter jika dibutuhkan adapter atau view
}
