package com.example.escape_rooms.model;

public class User {
    private Integer id; // Use Integer to allow null
    private String username;
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
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String name) { this.username = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
