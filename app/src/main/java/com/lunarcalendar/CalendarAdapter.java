package com.lunarcalendar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

public class CalendarAdapter extends BaseAdapter {
    private Context ctx;
    private List<DayCell> days = new ArrayList<>();
    private int todayDay, todayMonth, todayYear;

    // Một ô trong lưới lịch
    public static class DayCell {
        public int day, month, year;
        public String lunarText = "";   // chuỗi hiển thị âm lịch
        public int lunarDayNum = 0;      // số ngày âm (để đánh dấu mùng 1 / rằm)
        public boolean isToday = false;

        // Ô trống
        public DayCell(int d, int m, int y) {
            day = d; month = m; year = y;
        }
    }

    public CalendarAdapter(Context c, List<DayCell> initial) {
        ctx = c;
        if (initial != null) days.addAll(initial);
        Calendar today = Calendar.getInstance();
        todayDay = today.get(Calendar.DAY_OF_MONTH);
        todayMonth = today.get(Calendar.MONTH) + 1;
        todayYear = today.get(Calendar.YEAR);
    }

    // Nạp lại danh sách ô
    public void clear() { days.clear(); }
    public void addAll(List<DayCell> list) {
        if (list == null) return;
        for (DayCell c : list) {
            // Tính âm lịch cho ô có ngày thực
            if (c.day > 0) {
                try {
                    LunarCalendar.LunarDate ld = LunarCalendar.solarToLunar(c.year, c.month, c.day);
                    c.lunarDayNum = ld.day;
                    // Ngày mùng 1 hiển thị "ngày/tháng âm", các ngày khác chỉ hiện ngày âm
                    c.lunarText = (ld.day == 1) ? (ld.day + "/" + ld.month) : String.valueOf(ld.day);
                } catch (Exception e) {
                    c.lunarText = "";
                }
                c.isToday = (c.day == todayDay && c.month == todayMonth && c.year == todayYear);
            }
            days.add(c);
        }
    }

    @Override public int getCount() { return days.size(); }
    @Override public DayCell getItem(int p) { return days.get(p); }
    @Override public long getItemId(int p) { return p; }

    @Override public View getView(int pos, View v, ViewGroup parent) {
        if (v == null) {
            v = LayoutInflater.from(ctx).inflate(R.layout.calendar_day_item, parent, false);
        }
        DayCell cell = days.get(pos);
        TextView tvDay = v.findViewById(R.id.tvDay);
        TextView tvLunar = v.findViewById(R.id.tvLunarDay);

        // Ô trống
        if (cell.day == 0) {
            tvDay.setText("");
            tvLunar.setText("");
            v.setBackgroundColor(Color.TRANSPARENT);
            return v;
        }

        tvDay.setText(String.valueOf(cell.day));
        tvLunar.setText(cell.lunarText);

        // Nền: hôm nay nổi bật
        v.setBackgroundColor(cell.isToday
                ? ctx.getResources().getColor(R.color.today_bg)
                : Color.WHITE);

        // Mùng 1 và rằm âm lịch tô đỏ, in đậm
        boolean special = (cell.lunarDayNum == 1 || cell.lunarDayNum == 15);
        tvLunar.setTextColor(ctx.getResources().getColor(special ? R.color.red : R.color.gray));
        tvLunar.setTypeface(Typeface.DEFAULT, special ? Typeface.BOLD : Typeface.NORMAL);

        // Chủ nhật tô đỏ cho ngày dương
        Calendar cal = Calendar.getInstance();
        cal.set(cell.year, cell.month - 1, cell.day);
        boolean sunday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
        tvDay.setTextColor(ctx.getResources().getColor(sunday ? R.color.red : R.color.black));

        return v;
    }
}
