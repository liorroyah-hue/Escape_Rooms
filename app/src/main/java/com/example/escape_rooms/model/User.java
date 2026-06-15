package com.example.escape_rooms.model;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("user_id")  // תוקן: היה "user id" עם רווח, Supabase משתמש ב-user_id עם קו תחתון
    private Integer id;

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    // Constructor for fetching (with ID)
    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    // Constructor for registration (without ID — Supabase מייצר את ה-ID אוטומטית)
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.id = null;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String name) { this.username = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
