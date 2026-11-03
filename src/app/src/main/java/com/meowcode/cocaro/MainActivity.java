package com.meowcode.cocaro;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity: Màn hình chính của app Cờ Caro.
 * Hiển thị bàn cờ và trạng thái lượt chơi.
 */
public class MainActivity extends AppCompatActivity implements GomokuView.OnGameListener {

    private GomokuView gomokuView;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ các view từ layout
        gomokuView = findViewById(R.id.gomokuView);
        tvStatus = findViewById(R.id.tvStatus);
        Button btnReset = findViewById(R.id.btnReset);

        // Đăng ký listener để nhận sự kiện game
        gomokuView.setOnGameListener(this);

        // Xử lý nút Chơi lại
        btnReset.setOnClickListener(v -> gomokuView.resetGame());

        // Hiển thị lượt đầu tiên
        tvStatus.setText(R.string.turn_x);
    }

    @Override
    public void onTurnChanged(String player) {
        // Cập nhật text lượt chơi khi đổi người
        tvStatus.setText(player.equals("X") ? R.string.turn_x : R.string.turn_o);
    }

    @Override
    public void onGameWon(String winner) {
        // Hiển thị thông báo người thắng
        tvStatus.setText(winner.equals("X") ? R.string.player_x_win : R.string.player_o_win);
    }

    @Override
    public void onGameDraw() {
        // Hiển thị thông báo hòa
        tvStatus.setText(R.string.draw_game);
    }
}