package com.example.escape_rooms.view;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.escape_rooms.R;
import com.example.escape_rooms.model.FindItemTask;
import com.example.escape_rooms.repository.QuestionRepository;
import com.example.escape_rooms.repository.services.GameAudioManager;
import com.example.escape_rooms.viewmodel.GameViewModel;

import java.io.Serializable;

public class FindTheItemActivity extends AppCompatActivity {
    private Button invisibleButton;
    private ImageView findItemImage;
    private TextView textForImage;
    private ProgressBar loadingProgress;

    private static final String PROJECT_ID = "wjwbshqrvbgdtqanztqz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fing_the_item);

        invisibleButton = findViewById(R.id.invisibleButton);
        findItemImage = findViewById(R.id.findItemImage);
        textForImage = findViewById(R.id.textForImage);
        loadingProgress = findViewById(R.id.loadingProgress);

        Intent intent = getIntent();
        int nextLevel = intent.getIntExtra(MainActivity.EXTRA_LEVEL, 1);
        String creationType = intent.getStringExtra(MainActivity.EXTRA_CREATION_TYPE);
        Serializable aiData = intent.getSerializableExtra(MainActivity.EXTRA_AI_GAME_DATA);
        Serializable timings = intent.getSerializableExtra(MainActivity.EXTRA_TIMINGS);

        if (loadingProgress != null) loadingProgress.setVisibility(View.VISIBLE);

        QuestionRepository.getInstance().getRandomFindItemTask(new QuestionRepository.FindItemCallback() {
            @Override
            public void onSuccess(FindItemTask task) {
                if (isFinishing() || isDestroyed()) return;

                String imageUrl = task.getImageName();
//                               if (imageUrl.contains("wjwbshqrvbgdtqanztqz")) {
//                    imageUrl = imageUrl.replace("wjwbshqrvbgdtqanztqz", PROJECT_ID);
//                } else if (!imageUrl.startsWith("http")) {
//                    imageUrl = "https://" + PROJECT_ID + ".supabase.co/storage/v1/object/public/find-item-images/" + imageUrl;
//                }
                if (imageUrl.startsWith("http")) {
                    // ה-URL כבר מלא, אל תשנה כלום
                } else {
                    imageUrl = "https://" + PROJECT_ID +
                            ".supabase.co/storage/v1/object/public/find-item-images/" + imageUrl;
                }

                Log.d("FindTheItem", "Final URL: " + imageUrl);

                Log.d("FindTheItem", "Attempting to load URL: " + imageUrl);

                final String finalUrl = imageUrl;
                runOnUiThread(() -> {
                    textForImage.setText(task.getPromptText());

                    Glide.with(FindTheItemActivity.this)
                            .load(finalUrl)
                            .thumbnail(0.1f)
                            .error(android.R.drawable.stat_notify_error)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("FindTheItem", "Glide Load Failed", e);
                                    if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .centerCrop()
                            .into(findItemImage);

                    MoveButtonToCorrectPlace(invisibleButton, task.getXCord(), task.getYCord());
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("FindTheItem", "Repository Error", e);
                runOnUiThread(() -> {
                    if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(FindTheItemActivity.this, "Database Connection Error", Toast.LENGTH_SHORT).show();
                });
            }
        });

        invisibleButton.setOnClickListener(v -> {
            GameAudioManager.getInstance(this).playSuccessSound();
            if (nextLevel > GameViewModel.MAX_LEVELS) {
                Intent resultsIntent = new Intent(FindTheItemActivity.this, PlayerResultsActivity.class);
                resultsIntent.putExtra(MainActivity.EXTRA_TIMINGS, timings);
                startActivity(resultsIntent);
            } else {
                Intent corridorIntent = new Intent(FindTheItemActivity.this, DrawerActivity.class);
                corridorIntent.putExtra(MainActivity.EXTRA_LEVEL, nextLevel);
                corridorIntent.putExtra(MainActivity.EXTRA_CREATION_TYPE, creationType);
                if (aiData != null) corridorIntent.putExtra(MainActivity.EXTRA_AI_GAME_DATA, aiData);
                if (timings != null) corridorIntent.putExtra(MainActivity.EXTRA_TIMINGS, timings);
                startActivity(corridorIntent);
            }
            finish();
        });
    }

    public void MoveButtonToCorrectPlace(Button button, int x_dp, int y_dp) {
        float density = getResources().getDisplayMetrics().density;
        button.setTranslationX((float) x_dp * density);
        button.setTranslationY((float) y_dp * density);
    }
}
