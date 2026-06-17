package com.example.escape_rooms.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.escape_rooms.R;
import com.example.escape_rooms.viewmodel.SignUpViewModel;

/**
 * מסך ההרשמה — יוצר חשבון משתמש חדש.
 */
public class SignUpActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button signUpButton;
    private ProgressBar progressBar; // מוצג בזמן שמירה לשרת
    private SignUpViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        viewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                new androidx.core.view.OnApplyWindowInsetsListener() {
                    // מאזין לשינויים בגבולות המסך — מוסיף padding כדי שתוכן לא יתחבא מאחורי שורת הסטטוס והניווט
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                        return insets;
                    }
                });

        usernameEditText = findViewById(R.id.inputUsername);
        passwordEditText = findViewById(R.id.inputPassword);
        signUpButton = findViewById(R.id.buttonSignUp);
        progressBar = findViewById(R.id.progressBar);

        observeViewModel();

        // לחיצה על כפתור הרשמה — מסיר רווחים ושולח ל-ViewModel
        signUpButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            viewModel.signUp(username, password);
        });
    }

    private void observeViewModel() {
        // מצב טעינה — מציג/מסתיר ProgressBar ומשבית/מאפשר כפתור
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            signUpButton.setEnabled(!isLoading); // לא ניתן ללחוץ שוב בזמן שמירה
        });

        // הרשמה הצליחה — Toast ו-חזרה למסך כניסה
        viewModel.getSignUpResult().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Registration Successful! You can now log in.", Toast.LENGTH_LONG).show();
                finish(); // חוזר ל-LoginActivity
            }
        });

        // שגיאה — מציג Toast
        viewModel.getErrorMessage().observe(this, error -> {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }
}
