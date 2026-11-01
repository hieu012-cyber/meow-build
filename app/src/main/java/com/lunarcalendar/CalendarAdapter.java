package com.lunarcalendar;
import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;
public class CalendarAdapter extends BaseAdapter {
    Context ctx;
    List<DayCell> days;
    int todayDay, todayMonth, todayYear;
    public static class DayCell {
        int day, month, year;
        String lunarDay;
        boolean isToday;
        public DayCell(int d, int m, int y, String ld, boolean t) {
            day = d; month = m; year = y; lunarDay = ld; isToday = t;
        }
    }
    public CalendarAdapter(Context c, int year, int month) {
        ctx = c;
        days = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        todayDay = today.get(Calendar.DAY_OF_MONTH);
        todayMonth = today.get(Calendar.MONTH) + 1;
        todayYear = today.get(Calendar.YEAR);
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=CN, 2=T2...
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        // Thêm ô trống trước ngày 1
        for (int i = 1; i < firstDayOfWeek; i++) {
            days.add(new DayCell(0, month, year, "", false));
        }
        // Thêm các ngày trong tháng
        for (int d = 1; d <= daysInMonth; d++) {
            String lunar = "";
            try {
                LunarCalendar.LunarDate ld = LunarCalendar.solarToLunar(year, month, d);
                lunar = ld.day + "/" + ld.month;
            } catch (Exception e) { lunar = "-"; }
            boolean isToday = (d == todayDay && month == todayMonth && year == todayYear);
            days.add(new DayCell(d, month, year, lunar, isToday));
        }
    }
    @Override public int getCount() { return days.size(); }
    @Override public Object getItem(int p) { return days.get(p); }
    @Override public long getItemId(int p) { return p; }
    @Override public View getView(int pos, View v, ViewGroup parent) {
        if (v == null) {
            v = LayoutInflater.from(ctx).inflate(R.layout.calendar_day_item, parent, false);
        }
        DayCell cell = days.get(pos);
        TextView tvDay = v.findViewById(R.id.tvDay);
        TextView tvLunar = v.findViewById(R.id.tvLunarDay);
        if (cell.day == 0) {
            tvDay.setText("");
            tvLunar.setText("");
            v.setBackgroundColor(Color.TRANSPARENT);
        } else {
            tvDay.setText(String.valueOf(cell.day));
            tvLunar.setText(cell.lunarDay);
            if (cell.isToday) {
                v.setBackgroundColor(ctx.getResources().getColor(R.color.today_bg));
            } else {
                v.setBackgroundColor(Color.WHITE);
            }
            // Chủ nhật màu đỏ
            Calendar cal = Calendar.getInstance();
            cal.set(cell.year, cell.month - 1, cell.day);
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                tvDay.setTextColor(ctx.getResources().getColor(R.color.red));
            } else {
                tvDay.setTextColor(Color.BLACK);
            }
        }
        return v;
    }
}
