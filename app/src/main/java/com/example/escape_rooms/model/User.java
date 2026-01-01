package com.example.escape_rooms.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("user id")
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

    // Constructor for registration (without ID)
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.id = null; // Ensure ID is null so it's omitted from JSON
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String name) { this.username = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
