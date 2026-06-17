package com.example.escape_rooms.view;

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

/**
 * מסך הכניסה — מאפשר למשתמש להכניס שם משתמש וסיסמה.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText; // שדות קלט
    private TextView textStatus, textSignUpLink;          // תצוגת סטטוס וקישור הרשמה
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // מאפשר תצוגה מלאה עד קצוות המסך
        setContentView(R.layout.activity_log_in);

        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // מגדיר padding לפי גובה שורת הסטטוס/ניווט של המכשיר
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

        // קישור אלמנטי UI
        usernameEditText = findViewById(R.id.inputUsername);
        passwordEditText = findViewById(R.id.inputPassword);
        textStatus = findViewById(R.id.textStatus);
        textSignUpLink = findViewById(R.id.textSignUpLink);
        Button loginButton = findViewById(R.id.buttonLogin);

        textStatus.setVisibility(View.GONE); // מסתיר הודעת סטטוס בהתחלה

        observeViewModel();

        // לחיצה על כפתור כניסה — שולח פרטים ל-ViewModel
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            loginViewModel.login(username, password);
        });

        // לחיצה על "הרשמה" — עובר למסך ההרשמה
        textSignUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    /**
     * מאזין לשינויים ב-ViewModel ומעדכן את ה-UI בהתאם.
     */
    private void observeViewModel() {
        // כניסה הצליחה
        loginViewModel.getLoginResult().observe(this, success -> {
            if (success) {
                textStatus.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                // ממתין שנייה ואז עובר לבחירת משחק
                textStatus.postDelayed(() -> {
                    Intent intent = new Intent(this, ChoosingGameVariantActivity.class);
                    startActivity(intent);
                    finish(); // סוגר את מסך הכניסה
                }, 1000);
            }
        });

        // שגיאת כניסה — מציג Toast עם ההודעה
        loginViewModel.getErrorMessage().observe(this, error -> {
            textStatus.setVisibility(View.GONE);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });

        // מצב טעינה — ניתן להוסיף כאן ProgressBar
        loginViewModel.getIsLoading().observe(this, loading -> {});
    }
}
