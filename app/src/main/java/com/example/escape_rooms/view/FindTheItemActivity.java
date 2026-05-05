package com.example.escape_rooms.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        // Edge-to-edge with fully transparent system bars (no scrim) so the
        // background image truly covers from top of status bar to bottom of
        // navigation bar.
        EdgeToEdge.enable(
                this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT)
        );
        setContentView(R.layout.activity_fing_the_item);

        // Some OEMs/older OS versions still apply theme statusBarColor /
        // navigationBarColor on top of the activity. Force them transparent
        // here so the image is not occluded.
        Window window = getWindow();
        if (window != null) {
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        invisibleButton = findViewById(R.id.invisibleButton);
        findItemImage = findViewById(R.id.findItemImage);
        findItemImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        textForImage = findViewById(R.id.textForImage);
        loadingProgress = findViewById(R.id.loadingProgress);

        // Background image fills the whole screen (including under status/nav bars).
        // Inset only the foreground container so text and progress stay clear of system bars.
        View foreground = findViewById(R.id.foregroundContainer);
        if (foreground != null) {
            ViewCompat.setOnApplyWindowInsetsListener(foreground, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                lp.topMargin = bars.top;
                lp.bottomMargin = bars.bottom;
                lp.leftMargin = bars.left;
                lp.rightMargin = bars.right;
                v.setLayoutParams(lp);
                return insets;
            });
        }

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
                if (imageUrl.startsWith("http")) {
                    // ה-URL כבר מלא, אל תשנה כלום
                } else {
                    imageUrl = "https://" + PROJECT_ID +
                            ".supabase.co/storage/v1/object/public/find-item-images/" + imageUrl;
                }

                Log.d("FindTheItem", "Final URL: " + imageUrl);
                Log.d("FindTheItem", "Attempting to load URL: " + imageUrl);

                final String finalUrl = imageUrl;
                final int xCord = task.getXCord();
                final int yCord = task.getYCord();

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
                                    // Position the click target only AFTER the
                                    // real image is loaded — the button math
                                    // depends on the loaded drawable's
                                    // intrinsic dimensions, not the placeholder.
                                    findItemImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    findItemImage.post(() -> MoveButtonToCorrectPlace(invisibleButton, xCord, yCord));
                                    return false;
                                }
                            })
                            .centerCrop()
                            .into(findItemImage);
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
        findItemImage.post(() -> {
            // Use Android's actual ImageView matrix instead of duplicating the
            // scale/crop math. This keeps the hotspot aligned with whatever
            // transformation ImageView/Glide applies to the loaded drawable.
            Drawable drawable = findItemImage.getDrawable();
            if (drawable == null) return;

            int viewW = findItemImage.getWidth();
            int viewH = findItemImage.getHeight();
            if (viewW <= 0 || viewH <= 0) return;

            float[] point = new float[]{x_dp, y_dp};
            Matrix imageMatrix = findItemImage.getImageMatrix();
            imageMatrix.mapPoints(point);

            int[] imageLocation = new int[2];
            findItemImage.getLocationOnScreen(imageLocation);

            int[] buttonLocation = new int[2];
            button.getLocationOnScreen(buttonLocation);

            float offsetX = (imageLocation[0] - buttonLocation[0])
                    + point[0] - (button.getWidth() / 2f);
            float offsetY = (imageLocation[1] - buttonLocation[1])
                    + point[1] - (button.getHeight() / 2f);

            button.setTranslationX(offsetX);
            button.setTranslationY(offsetY);
            button.bringToFront();
        });
    }
}
