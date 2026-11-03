package com.meowcode.cocaro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * GomokuView: View tùy chỉnh vẽ bàn cờ Caro (Gomoku).
 * Hỗ trợ chế độ 2 người chơi trên cùng màn hình.
 */
public class GomokuView extends View {

    // Kích thước bàn cờ 15x15 ô (chuẩn Gomoku)
    private static final int BOARD_SIZE = 15;
    private static final int EMPTY = 0;
    private static final int PLAYER_X = 1;
    private static final int PLAYER_O = 2;

    // Ma trận lưu trạng thái bàn cờ
    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE];

    // Người chơi hiện tại (X đi trước)
    private int currentPlayer = PLAYER_X;

    // Game đã kết thúc chưa
    private boolean gameEnded = false;

    // Tọa độ quân thắng đầu tiên và cuối cùng (để vẽ đường thắng)
    private int winStartRow = -1, winStartCol = -1;
    private int winEndRow = -1, winEndCol = -1;

    // Vẽ màn hình
    private Paint paintLine = new Paint();
    private Paint paintX = new Paint();
    private Paint paintO = new Paint();
    private Paint paintWinLine = new Paint();
    private Paint paintBg = new Paint();

    // Kích thước mỗi ô, được tính lại khi vẽ
    private float cellSize;

    // Listener cập nhật trạng thái game ra ngoài (MainActivity)
    public interface OnGameListener {
        void onTurnChanged(String player);
        void onGameWon(String winner);
        void onGameDraw();
    }

    private OnGameListener listener;

    public GomokuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    /** Khởi tạo các cọ vẽ với màu sắc và độ dày phù hợp */
    private void initPaints() {
        // Cọ vẽ lưới bàn cờ
        paintLine.setColor(Color.BLACK);
        paintLine.setStrokeWidth(2f);
        paintLine.setAntiAlias(true);

        // Cọ vẽ quân X (màu đỏ)
        paintX.setColor(Color.RED);
        paintX.setStrokeWidth(6f);
        paintX.setStyle(Paint.Style.STROKE);
        paintX.setAntiAlias(true);

        // Cọ vẽ quân O (màu xanh dương)
        paintO.setColor(Color.BLUE);
        paintO.setStrokeWidth(6f);
        paintO.setStyle(Paint.Style.STROKE);
        paintO.setAntiAlias(true);

        // Cọ vẽ đường thắng (màu xanh lá, dày hơn)
        paintWinLine.setColor(Color.rgb(0, 150, 0));
        paintWinLine.setStrokeWidth(8f);
        paintWinLine.setAntiAlias(true);

        // Cọ màu nền bàn cờ (màu gỗ vàng nhạt)
        paintBg.setColor(Color.rgb(255, 206, 158));
        paintBg.setStyle(Paint.Style.FILL);
    }

    /** Đăng ký listener để cập nhật trạng thái game */
    public void setOnGameListener(OnGameListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Tính kích thước ô = cạnh nhỏ nhất chia số ô
        cellSize = Math.min(w, h) / (float) BOARD_SIZE;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Vẽ nền bàn cờ
        canvas.drawPaint(paintBg);

        // Căn giữa bàn cờ theo chiều ngang và dọc
        float offsetX = (getWidth() - cellSize * BOARD_SIZE) / 2f;
        float offsetY = (getHeight() - cellSize * BOARD_SIZE) / 2f;

        // Vẽ lưới ngang và dọc
        for (int i = 0; i <= BOARD_SIZE; i++) {
            float pos = i * cellSize;
            canvas.drawLine(offsetX + pos, offsetY, offsetX + pos, offsetY + cellSize * BOARD_SIZE, paintLine);
            canvas.drawLine(offsetX, offsetY + pos, offsetX + cellSize * BOARD_SIZE, offsetY + pos, paintLine);
        }

        // Vẽ tất cả quân cờ đã đặt
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] != EMPTY) {
                    drawStone(canvas, row, col, board[row][col], offsetX, offsetY);
                }
            }
        }

        // Nếu có người thắng, vẽ đường nối 5 quân
        if (gameEnded && winStartRow != -1) {
            float startX = offsetX + (winStartCol + 0.5f) * cellSize;
            float startY = offsetY + (winStartRow + 0.5f) * cellSize;
            float endX = offsetX + (winEndCol + 0.5f) * cellSize;
            float endY = offsetY + (winEndRow + 0.5f) * cellSize;
            canvas.drawLine(startX, startY, endX, endY, paintWinLine);
        }
    }

    /** Vẽ 1 quân cờ X hoặc O tại vị trí (row, col) */
    private void drawStone(Canvas canvas, int row, int col, int player, float offsetX, float offsetY) {
        // Tâm của ô
        float centerX = offsetX + (col + 0.5f) * cellSize;
        float centerY = offsetY + (row + 0.5f) * cellSize;
        // Bán kính quân cờ (chừa chút khoảng trống trong ô)
        float radius = cellSize * 0.4f;

        if (player == PLAYER_X) {
            // Vẽ chữ X bằng 2 đường chéo
            canvas.drawLine(centerX - radius, centerY - radius, centerX + radius, centerY + radius, paintX);
            canvas.drawLine(centerX - radius, centerY + radius, centerX + radius, centerY - radius, paintX);
        } else {
            // Vẽ chữ O bằng vòng tròn
            canvas.drawCircle(centerX, centerY, radius, paintO);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Chỉ xử lý khi ngón tay nhấn xuống và game chưa kết thúc
        if (event.getAction() == MotionEvent.ACTION_DOWN && !gameEnded) {
            // Căn giữa bàn cờ giống như khi vẽ
            float offsetX = (getWidth() - cellSize * BOARD_SIZE) / 2f;
            float offsetY = (getHeight() - cellSize * BOARD_SIZE) / 2f;

            // Đổi tọa độ chạm thành vị trí ô trên bàn cờ
            int col = (int) ((event.getX() - offsetX) / cellSize);
            int row = (int) ((event.getY() - offsetY) / cellSize);

            // Kiểm tra chạm trong bàn cờ và ô còn trống
            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE && board[row][col] == EMPTY) {
                // Đặt quân của người chơi hiện tại
                board[row][col] = currentPlayer;

                // Kiểm tra thắng/thua
                if (checkWin(row, col)) {
                    gameEnded = true;
                    if (listener != null) {
                        listener.onGameWon(currentPlayer == PLAYER_X ? "X" : "O");
                    }
                } else if (isBoardFull()) {
                    // Bàn cờ đầy mà không ai thắng -> hòa
                    gameEnded = true;
                    if (listener != null) {
                        listener.onGameDraw();
                    }
                } else {
                    // Đổi lượt chơi
                    currentPlayer = (currentPlayer == PLAYER_X) ? PLAYER_O : PLAYER_X;
                    if (listener != null) {
                        listener.onTurnChanged(currentPlayer == PLAYER_X ? "X" : "O");
                    }
                }

                // Vẽ lại màn hình
                invalidate();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    /** Kiểm tra người chơi vừa đi có thắng không (5 quân liên tiếp) */
    private boolean checkWin(int row, int col) {
        // 4 hướng kiểm tra: ngang, dọc, 2 đường chéo
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int count = 1; // Tính cả quân vừa đặt

            // Đếm về 1 phía
            count += countStones(row, col, dir[0], dir[1]);
            // Đếm về phía ngược lại
            count += countStones(row, col, -dir[0], -dir[1]);

            if (count >= 5) {
                // Lưu vị trí 2 đầu để vẽ đường thắng
                winEndRow = row + findEnd(row, col, dir[0], dir[1]) * dir[0];
                winEndCol = col + findEnd(row, col, dir[0], dir[1]) * dir[1];
                winStartRow = row - findEnd(row, col, -dir[0], -dir[1]) * dir[0];
                winStartCol = col - findEnd(row, col, -dir[0], -dir[1]) * dir[1];
                return true;
            }
        }
        return false;
    }

    /** Đếm số quân liên tiếp từ vị trí (row, col) theo hướng (dRow, dCol) */
    private int countStones(int row, int col, int dRow, int dCol) {
        int count = 0;
        int currentRow = row + dRow;
        int currentCol = col + dCol;
        int player = board[row][col];

        while (currentRow >= 0 && currentRow < BOARD_SIZE
                && currentCol >= 0 && currentCol < BOARD_SIZE
                && board[currentRow][currentCol] == player) {
            count++;
            currentRow += dRow;
            currentCol += dCol;
        }
        return count;
    }

    /** Tìm khoảng cách đến quân cuối cùng theo hướng (dRow, dCol) */
    private int findEnd(int row, int col, int dRow, int dCol) {
        int distance = 0;
        int currentRow = row + dRow;
        int currentCol = col + dCol;
        int player = board[row][col];

        while (currentRow >= 0 && currentRow < BOARD_SIZE
                && currentCol >= 0 && currentCol < BOARD_SIZE
                && board[currentRow][currentCol] == player) {
            distance++;
            currentRow += dRow;
            currentCol += dCol;
        }
        return distance;
    }

    /** Kiểm tra bàn cờ đã đầy chưa */
    private boolean isBoardFull() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Chơi lại từ đầu: xóa bàn cờ và reset trạng thái */
    public void resetGame() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        currentPlayer = PLAYER_X;
        gameEnded = false;
        winStartRow = -1;
        winStartCol = -1;
        winEndRow = -1;
        winEndCol = -1;
        invalidate();
        if (listener != null) {
            listener.onTurnChanged("X");
        }
    }
}