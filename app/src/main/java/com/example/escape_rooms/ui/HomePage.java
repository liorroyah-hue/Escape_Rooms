package com.example.escape_rooms.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.escape_rooms.R;

public class HomePage extends Fragment {

    private Button myEscapeRoomButton, communityEscapeRoomsButton, createRoomButton;

    public HomePage() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.homepage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupClickListeners();
    }

    private void initializeViews(View view) {
        myEscapeRoomButton = view.findViewById(R.id.MyEscapeRoom);
        communityEscapeRoomsButton = view.findViewById(R.id.CommunityEscapeRoom);
        createRoomButton = view.findViewById(R.id.CreateRoom);
    }

    private void setupClickListeners() {
        // Example of a button click listener
        myEscapeRoomButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "'My Escape Rooms' clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement navigation to the user's personal escape rooms
        });

        communityEscapeRoomsButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "'Community Rooms' clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement navigation to the community rooms section
        });

        createRoomButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "'Create Room' clicked", Toast.LENGTH_SHORT).show();
            // TODO: Implement the room creation flow
        });
    }
}
