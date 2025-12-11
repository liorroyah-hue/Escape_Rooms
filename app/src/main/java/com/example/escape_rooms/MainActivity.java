package com.example.escape_rooms;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
       setContentView(R.layout.activity_main);
        initNavigationBar(savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }
    private void initNavigationBar(Bundle savedInstanceState) {
        BottomNavigationView bottomNavBar = findViewById(R.id.bottom_navigation);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomePage()).commit();
        bottomNavBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
               Fragment selectedFragment= null;
                if(menuItem.getItemId()==R.id.nav_first)
                    selectedFragment=new HomePage();
                else if(menuItem.getItemId()==R.id.nav_second)
                    selectedFragment=new SignIn();
                else if(menuItem.getItemId()==R.id.nav_third)
                    selectedFragment=new ThirdFragment();
                if(selectedFragment!=null)
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();
                return true;

            }
        });
    }
}




