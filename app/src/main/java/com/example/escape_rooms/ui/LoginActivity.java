package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.escape_rooms.R;
import com.example.escape_rooms.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private TextView textStatus, textSignUpLink;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);

        // Setup ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Apply window insets for EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameEditText = findViewById(R.id.inputUsername);
        passwordEditText = findViewById(R.id.inputPassword);
        textStatus = findViewById(R.id.textStatus);
        textSignUpLink = findViewById(R.id.textSignUpLink);
        Button loginButton = findViewById(R.id.buttonLogin);

        textStatus.setVisibility(View.GONE);

        // Observe ViewModel states
        observeViewModel();

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            loginViewModel.login(username, password);
        });

        textSignUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        loginViewModel.getLoginResult().observe(this, success -> {
            if (success) {
                textStatus.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                
                textStatus.postDelayed(() -> {
                    Intent intent = new Intent(this, ChoosingGameVarientActivity.class);
                    startActivity(intent);
                    finish();
                }, 1000);
            }
        });

        loginViewModel.getErrorMessage().observe(this, error -> {
            textStatus.setVisibility(View.GONE);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });

        loginViewModel.getIsLoading().observe(this, loading -> {
            // Optional: Show a progress bar here
        });
    }
}
