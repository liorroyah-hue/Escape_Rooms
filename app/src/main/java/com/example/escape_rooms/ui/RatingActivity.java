package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.escape_rooms.R;
import com.example.escape_rooms.viewmodel.RatingViewModel;

public class RatingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        RatingBar ratingBar = findViewById(R.id.ratingBar);
        Button btnSubmit = findViewById(R.id.btn_submit_rating);

        RatingViewModel viewModel = new ViewModelProvider(this).get(RatingViewModel.class);

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            if (rating > 0) {
                // Corrected method call to submitRating
                viewModel.submitRating(rating);
                Toast.makeText(this, getString(R.string.msg_rating_saved, (int)rating), Toast.LENGTH_SHORT).show();
                
                // Mission complete - go back to home (Corridor)
                Intent intent = new Intent(RatingActivity.this, Corridor.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
