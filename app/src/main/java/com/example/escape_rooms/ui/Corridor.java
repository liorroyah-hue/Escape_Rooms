package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.escape_rooms.R;
import com.example.escape_rooms.viewmodel.RatingViewModel;

public class Corridor extends AppCompatActivity {
    
    private RatingBar ratingBar;
    private RatingViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_corridor);
        
        viewModel = new ViewModelProvider(this).get(RatingViewModel.class);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ratingBar = findViewById(R.id.ratingBar);
        Button btnFinishRating = findViewById(R.id.btn_finish_rating);

        observeViewModel();

        btnFinishRating.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            viewModel.submitRating(rating);
        });
    }

    private void observeViewModel() {
        viewModel.getSavedRating().observe(this, rating -> {
            Toast.makeText(this, "Rating saved: " + rating, Toast.LENGTH_SHORT).show();
        });

        viewModel.getNavigateBack().observe(this, shouldNavigate -> {
            if (Boolean.TRUE.equals(shouldNavigate)) {
                Intent intent = new Intent(this, HomePage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }
}
