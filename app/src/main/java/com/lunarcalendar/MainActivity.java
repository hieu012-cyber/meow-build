package com.lunarcalendar;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.*;
import com.lunarcalendar.CalendarAdapter.DayCell;

public class MainActivity extends Activity {
    GridView gridCalendar;
    TextView tvMonthYear, tvSelectedDate, tvLunarDate, tvCanChi;
    Button btnPrev, btnNext, btnToday;
    Calendar currentMonth;
    CalendarAdapter adapter;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);
        gridCalendar = findViewById(R.id.gridCalendar);
        tvMonthYear = findViewById(R.id.tvMonthYear);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvLunarDate = findViewById(R.id.tvLunarDate);
        tvCanChi = findViewById(R.id.tvCanChi);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        btnToday = findViewById(R.id.btnToday);

        currentMonth = Calendar.getInstance();
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);

        adapter = new CalendarAdapter(this, new ArrayList<CalendarAdapter.DayCell>());
        gridCalendar.setAdapter(adapter);

        gridCalendar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                DayCell cell = adapter.getItem(pos);
                if (cell != null && cell.day != 0) showInfo(cell.day, cell.month, cell.year);
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { changeMonth(-1); }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { changeMonth(1); }
        });
        btnToday.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // Về tháng hiện tại và chọn hôm nay
                currentMonth = Calendar.getInstance();
                currentMonth.set(Calendar.DAY_OF_MONTH, 1);
                refresh();
                Calendar now = Calendar.getInstance();
                showInfo(now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR));
            }
        });

        refresh();
        Calendar now = Calendar.getInstance();
        showInfo(now.get(Calendar.DAY_OF_MONTH), now.get(Calendar.MONTH) + 1, now.get(Calendar.YEAR));
    }

    // Đổi tháng
    private void changeMonth(int delta) {
        currentMonth.add(Calendar.MONTH, delta);
        refresh();
    }

    // Dựng lại lưới lịch cho tháng đang chọn
    private void refresh() {
        int y = currentMonth.get(Calendar.YEAR);
        int m = currentMonth.get(Calendar.MONTH) + 1;
        tvMonthYear.setText("Tháng " + m + ", " + y);

        List<CalendarAdapter.DayCell> days = new ArrayList<>();
        Calendar first = Calendar.getInstance();
        first.set(y, m - 1, 1);
        int firstDayOfWeek = first.get(Calendar.DAY_OF_WEEK); // CN=1
        int daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Ô trống trước ngày mùng 1
        for (int i = 1; i < firstDayOfWeek; i++) {
            days.add(new CalendarAdapter.DayCell(0, 0, 0));
        }
        for (int d = 1; d <= daysInMonth; d++) {
            days.add(new CalendarAdapter.DayCell(d, m, y));
        }
        // Ô trống cuối để đủ hàng
        while (days.size() % 7 != 0) days.add(new CalendarAdapter.DayCell(0, 0, 0));

        adapter.clear();
        adapter.addAll(days);
        adapter.notifyDataSetChanged();
    }

    // Hiển thị thông tin dương + âm lịch của một ngày
    private void showInfo(int d, int m, int y) {
        tvSelectedDate.setText("Dương lịch: " + d + "/" + m + "/" + y);
        try {
            LunarCalendar.LunarDate ln = LunarCalendar.solarToLunar(y, m, d);
            tvLunarDate.setText("Âm lịch: " + ln.day + "/" + ln.month + (ln.leap ? " (nhuận)" : "") + "/" + ln.year);
            String canChiNgay = LunarCalendar.getCanChiDay(y, m, d);
            String canChiThang = LunarCalendar.getCanChiMonth(ln.year, ln.month);
            String canChiNam = LunarCalendar.getCanChiYear(ln.year);
            tvCanChi.setText("Ngày " + canChiNgay + "  •  Tháng " + canChiThang + "  •  Năm " + canChiNam);
        } catch (Exception e) {
            tvLunarDate.setText("Âm lịch: không tính được");
            tvCanChi.setText("");
        }
    }
}
