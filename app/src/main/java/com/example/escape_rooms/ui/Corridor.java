package com.example.escape_rooms.ui;

import android.os.Bundle;
import android.widget.RatingBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.escape_rooms.R;

public class Corridor extends AppCompatActivity {
   //debug
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_corridor);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    static int ratingInt =0;
    RatingBar ratingBar =findViewById(R.id.ratingBar);
    private void saveRating(float rating) {
        ratingInt = Math.max(1, Math.min(5, Math.round(rating)));

    }

}
