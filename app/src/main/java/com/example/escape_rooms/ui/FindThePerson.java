package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.escape_rooms.R;

import java.util.Random;

public class FindThePerson extends AppCompatActivity {

    private ImageView arrowButton;
    private FrameLayout photoFrameContainer;
    private ImageView spawnedPhoto;
    private Button hidden_button;
    private int[] backgrounds = {
            R.drawable.waldow_picture_1,
            R.drawable.background_room_white,
            R.drawable.background_corridor
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_the_person);

        // Get references to the views from the layout
        arrowButton = findViewById(R.id.arrow_button);
        photoFrameContainer = findViewById(R.id.photo_frame_container);
        spawnedPhoto = findViewById(R.id.spawned_photo);
        hidden_button = findViewById(R.id.hidden_button);



        // Set the click listener to "spawn" the frame
        hidden_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FindThePerson.this, DrawerActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    public void PlaceTheButtonInTheCorrectLocation (Button hidden_button) {
        Random random = new Random();
        int randomX = random.nextInt(265);
        int randomY = random.nextInt(485);
        hidden_button.setX(randomX);
        hidden_button.setY(randomY);
    }
}
