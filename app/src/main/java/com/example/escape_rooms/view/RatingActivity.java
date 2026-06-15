package com.example.escape_rooms.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.escape_rooms.R;
import com.example.escape_rooms.repository.services.GameAudioManager;
import com.example.escape_rooms.viewmodel.RatingViewModel;

/**
 * מסך הדירוג — השחקן נותן ציון 1-5 כוכבים לחוויית המשחק.
 * הציון נשמר ל-Supabase ועובר ללוח התוצאות.
 */
public class RatingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        // עוצר את כל המוזיקה והאפקטים — המשחק הסתיים
        GameAudioManager.getInstance(this).stopAllSounds();

        RatingBar ratingBar = findViewById(R.id.ratingBar); // פס הדירוג (1-5 כוכבים)
        Button btnSubmit = findViewById(R.id.btn_submit_rating);

        RatingViewModel viewModel = new ViewModelProvider(this).get(RatingViewModel.class);

        // לחיצה על "שלח דירוג"
        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating(); // קריאת הציון הנבחר
            if (rating > 0) {
                viewModel.submitRating(rating); // שולח ל-Supabase
                Toast.makeText(this, getString(R.string.msg_rating_saved, (int)rating), Toast.LENGTH_SHORT).show();
                // עובר ללוח התוצאות
                Intent intent = new Intent(this, LeaderboardActivity.class);
                startActivity(intent);
                finish(); // סוגר מסך דירוג
            } else {
                // לא נבחר דירוג — מציג הנחיה
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
