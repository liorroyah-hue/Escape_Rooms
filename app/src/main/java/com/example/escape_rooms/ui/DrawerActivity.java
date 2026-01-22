package com.example.escape_rooms.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Flow;

import com.example.escape_rooms.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DrawerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        Flow imageFlow = findViewById(R.id.image_flow);

        // --- Initial Random Placement with Flow ---
        List<Integer> viewIds = new ArrayList<>();
        viewIds.add(R.id.image1);
        viewIds.add(R.id.image2);
        viewIds.add(R.id.image3);
        viewIds.add(R.id.image4);
        viewIds.add(R.id.image5);
        viewIds.add(R.id.image6);

        Collections.shuffle(viewIds);

        int[] shuffledIds = new int[viewIds.size()];
        for (int i = 0; i < viewIds.size(); i++) {
            shuffledIds[i] = viewIds.get(i);
        }

        imageFlow.setReferencedIds(shuffledIds);
        // --- End Initial Placement ---

        // --- Make each ImageView Draggable ---
        ViewGroup container = findViewById(R.id.image_container);
        for (int id : viewIds) {
            ImageView imageView = container.findViewById(id);
            if (imageView != null) {
                imageView.setOnTouchListener(new DraggableTouchListener());
            }
        }
    }

    /**
     * A touch listener that makes a view draggable.
     */
    private static class DraggableTouchListener implements View.OnTouchListener {
        private float dX, dY;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Record the difference between the view's top-left corner and the touch point
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    // Bring the view to the front so it's drawn on top of other views
                    view.bringToFront();
                    break;

                case MotionEvent.ACTION_MOVE:
                    // Move the view with the finger
                    view.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                    break;

                default:
                    return false;
            }
            return true;
        }
    }
}
