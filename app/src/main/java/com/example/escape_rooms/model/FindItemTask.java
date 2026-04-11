package com.example.escape_rooms.model;

import com.google.gson.annotations.SerializedName;

public class FindItemTask {
    @SerializedName("id")
    private int id;

    @SerializedName("image_name")
    private String imageName;

    @SerializedName("prompt_text")
    private String promptText;

    @SerializedName("x_cord")
    private int xCord;

    @SerializedName("y_cord")
    private int yCord;

    // Getters
    public int getId() { return id; }
    public String getImageName() { return imageName; }
    public String getPromptText() { return promptText; }
    public int getXCord() { return xCord; }
    public int getYCord() { return yCord; }
}
