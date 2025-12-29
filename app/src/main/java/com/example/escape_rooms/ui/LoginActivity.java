package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.escape_rooms.R;
import com.example.escape_rooms.User;
import com.example.escape_rooms.UserRepository;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);

        // Apply window insets for EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userRepository = new UserRepository();
        usernameEditText = findViewById(R.id.inputUsername);
        passwordEditText = findViewById(R.id.inputPassword);
        Button loginButton = findViewById(R.id.buttonLogin);

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            verifyUserLogin(username, password);
        });
    }

    private void verifyUserLogin(String username, String password) {
        userRepository.getAllUsers(new UserRepository.UsersCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> userList) {
                boolean loginSuccess = false;
                for (User user : userList) {
                    if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                        loginSuccess = true;
                        break;
                    }
                }

                boolean finalLoginSuccess = loginSuccess;
                runOnUiThread(() -> {
                    if (finalLoginSuccess) {
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Finish LoginActivity so user can't go back
                    } else {
                        Toast.makeText(LoginActivity.this, "Login error: Invalid username or password", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("LoginActivity", "Error fetching users", e);
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login error: Could not connect to server", Toast.LENGTH_LONG).show());
            }
        });
    }
}
