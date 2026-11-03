package com.meowcode.cocaro;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * MenuActivity: Màn hình menu chính của app.
 * Có 2 nút: Chơi ngay (mở MainActivity) và Thoát (đóng app).
 */
public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Nút Chơi ngay: chuyển sang màn chơi cờ
        Button btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        // Nút Thoát: đóng toàn bộ app
        Button btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v -> finishAffinity());
    }
}
