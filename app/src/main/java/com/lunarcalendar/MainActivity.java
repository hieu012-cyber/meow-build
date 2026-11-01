package com.lunarcalendar;
import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import java.util.*;
public class MainActivity extends Activity {
    GridView gridCalendar;
    TextView tvMonthYear, tvSelectedDate, tvLunarDate;
    Button btnPrev, btnNext;
    Calendar currentMonth;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);
        gridCalendar = findViewById(R.id.gridCalendar);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvLunarDate = findViewById(R.id.tvLunarDate);
        btnPrev = findViewById(R.id.btnPreviousMonth);
        btnNext = findViewById(R.id.btnNextMonth);
        currentMonth = Calendar.getInstance();
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);
        btnPrev.setOnClickListener(v -> { currentMonth.add(Calendar.MONTH, -1); renderCalendar(); });
        btnNext.setOnClickListener(v -> { currentMonth.add(Calendar.MONTH, 1); renderCalendar(); });
        gridCalendar.setOnItemClickListener((p, v, pos, id) -> {
            CalendarAdapter.DayCell c = (CalendarAdapter.DayCell) p.getItemAtPosition(pos);
            if (c.day == 0) return;
            showDateInfo(c.year, c.month, c.day);
        });
        renderCalendar();
        Calendar t = Calendar.getInstance();
        showDateInfo(t.get(Calendar.YEAR), t.get(Calendar.MONTH)+1, t.get(Calendar.DAY_OF_MONTH));
    }
    void renderCalendar() {
        int y = currentMonth.get(Calendar.YEAR);
        int m = currentMonth.get(Calendar.MONTH)+1;
        tvMonthYear.setText("Tháng " + m + ", " + y);
        gridCalendar.setAdapter(new CalendarAdapter(this, y, m));
    }
    void showDateInfo(int y, int m, int d) {
        Calendar c = Calendar.getInstance();
        c.set(y, m-1, d);
        String[] thu = {"", "Chủ Nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy"};
        String dow = thu[c.get(Calendar.DAY_OF_WEEK)];
        tvSelectedDate.setText(dow + ", " + d + "/" + m + "/" + y);
        try {
            LunarCalendar.LunarDate ln = LunarCalendar.solarToLunar(y, m, d);
            tvLunarDate.setText("Âm lịch: " + ln.day + "/" + ln.month + (ln.leap ? " (nhuận)" : "") + "/" + ln.year + " - Năm " + LunarCalendar.getCanChiYear(ln.year));
        } catch (Exception e) { tvLunarDate.setText("Âm lịch: không tính được"); }
    }
}