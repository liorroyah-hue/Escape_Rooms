package com.example.escape_rooms;

import static kotlin.random.RandomKt.nextInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Room {
    private String difficulty;
    private int Id;
    private int rating;
    public Room(String difficulty,int Id,int rating){
        this.difficulty=difficulty;
        this.Id=Id;
        this.rating=rating;
    }
    public int getId() {
        return Id;
    }
    private ArrayList<Room> List;

    public Room getRoom(ArrayList<Room> List,int Id) {
        return List.get(Id);
    }
    public int getRating() {
        return rating;
    }
    public void setRating(int rating) {
        this.rating = rating;
    }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setId(int Id) { this.Id = Id; }

    public Room getNewRoom(ArrayList<Room> List){
        Room room=new Room(null,0,0);
        Random rand = new Random();
        int randomnumber = rand.nextInt(List.size());
        return(getRoom(List,List.get(randomnumber).getId()));
    }

}



