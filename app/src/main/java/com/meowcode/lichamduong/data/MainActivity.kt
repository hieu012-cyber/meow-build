package com.meowcode.lichamduong.data

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

// Màn hình chính: lịch tháng động + chi tiết ngày đã chọn.
class MainActivity : AppCompatActivity() {
    private var viewMonth = 11
    private var viewYear = 2026
    private var selectedDay = 5
    private var noteKey = ""
    private var loadingNote = false
    private val cells = mutableListOf<Cell>()
    private lateinit var adapter: DayAdapter
    private lateinit var rv: RecyclerView
    private lateinit var tvMonth: TextView
    private lateinit var tvMonthLunar: TextView
    private lateinit var tvSummary: TextView
    private lateinit var weekHeader: LinearLayout
    private lateinit var tvSolarDay: TextView
    private lateinit var tvWeekday: TextView
    private lateinit var tvLunarDate: TextView
    private lateinit var tvCanChi: TextView
    private lateinit var tvHoliday: TextView
    private lateinit var detailGrid: LinearLayout
    private lateinit var edtNote: EditText
    data class Cell(val day: Int, val info: DayInfo)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.meowcode.lichamduong.R.layout.activity_main)
        val now = Calendar.getInstance()
        viewMonth = now.get(Calendar.MONTH) + 1
        viewYear = now.get(Calendar.YEAR)
        selectedDay = now.get(Calendar.DAY_OF_MONTH)
        tvMonth = findViewById(com.meowcode.lichamduong.R.id.tvMonthTitle)
        tvMonthLunar = findViewById(com.meowcode.lichamduong.R.id.tvMonthLunarTitle)
        tvSummary = findViewById(com.meowcode.lichamduong.R.id.tvSelectedSummary)
        weekHeader = findViewById(com.meowcode.lichamduong.R.id.weekHeader)
        rv = findViewById(com.meowcode.lichamduong.R.id.rvCalendar)
        tvSolarDay = findViewById(com.meowcode.lichamduong.R.id.tvDetailSolarDay)
        tvWeekday = findViewById(com.meowcode.lichamduong.R.id.tvDetailWeekday)
        tvLunarDate = findViewById(com.meowcode.lichamduong.R.id.tvDetailLunarDate)
        tvCanChi = findViewById(com.meowcode.lichamduong.R.id.tvDetailCanChi)
        tvHoliday = findViewById(com.meowcode.lichamduong.R.id.tvDetailHoliday)
        detailGrid = findViewById(com.meowcode.lichamduong.R.id.detailGrid)
        edtNote = findViewById(com.meowcode.lichamduong.R.id.edtNote)
        buildWeekHeader()
        rv.layoutManager = GridLayoutManager(this, 7)
        adapter = DayAdapter()
        rv.adapter = adapter
        rv.isNestedScrollingEnabled = false
        findViewById<ImageButton>(com.meowcode.lichamduong.R.id.btnPrevMonth).setOnClickListener {
            val r = LunarCalendar.shiftMonth(viewMonth, viewYear, -1)
            viewMonth = r[0]; viewYear = r[1]; selectedDay = 1; reload()
        }
        findViewById<ImageButton>(com.meowcode.lichamduong.R.id.btnNextMonth).setOnClickListener {
            val r = LunarCalendar.shiftMonth(viewMonth, viewYear, 1)
            viewMonth = r[0]; viewYear = r[1]; selectedDay = 1; reload()
        }
        findViewById<View>(com.meowcode.lichamduong.R.id.btnToday).setOnClickListener {
            val c = Calendar.getInstance()
            viewMonth = c.get(Calendar.MONTH) + 1
            viewYear = c.get(Calendar.YEAR)
            selectedDay = c.get(Calendar.DAY_OF_MONTH)
            reload()
        }
        edtNote.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (loadingNote || noteKey.isEmpty()) return
                getPreferences(Context.MODE_PRIVATE).edit().putString(noteKey, s.toString()).apply()
            }
        })
        reload()
    }
    private fun buildWeekHeader() {
        weekHeader.removeAllViews()
        val names = arrayOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
        for (i in names.indices) {
            val tv = TextView(this)
            tv.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            tv.gravity = Gravity.CENTER
            tv.text = names[i]
            tv.textSize = 12f
            tv.setTypeface(tv.typeface, Typeface.BOLD)
            tv.setTextColor(if (i == 0) Color.parseColor("#E5484D") else Color.parseColor("#8B93A7"))
            weekHeader.addView(tv)
        }
    }
    private fun reload() {
        cells.clear()
        val max = LunarCalendar.daysInSolarMonth(viewMonth, viewYear)
        if (selectedDay > max) selectedDay = max
        for (d in 1..max) cells.add(Cell(d, LunarCalendar.solarToLunar(d, viewMonth, viewYear)))
        tvMonth.text = "Tháng $viewMonth, $viewYear"
        // Tiêu đề âm lịch: lấy tháng âm của ngày 15.
        val mid = LunarCalendar.solarToLunar(15.coerceAtMost(max), viewMonth, viewYear)
        var name = LunarCalendar.lunarMonthName(mid.lunarMonth, mid.lunarLeap)
        tvMonthLunar.text = "Âm lịch: $name" + if (mid.lunarMonth == 12 || mid.lunarMonth == 1) " ${mid.lunarYear}" else ""
        adapter.notifyDataSetChanged()
        renderDetail()
    }
    private fun selectDay(d: Int) { selectedDay = d; adapter.notifyDataSetChanged(); renderDetail() }
    private fun weekdayName(w: Int): String {
        return when (w) { 0 -> "Chủ Nhật"; 1 -> "Thứ Hai"; 2 -> "Thứ Ba"; 3 -> "Thứ Tư"; 4 -> "Thứ Năm"; 5 -> "Thứ Sáu"; else -> "Thứ Bảy" }
    }
    private fun holidayName(d: Int, m: Int): String? {
        if (d == 1 && m == 1) return "Tết Dương lịch"
        if (d == 30 && m == 4) return "Giải phóng miền Nam"
        if (d == 1 && m == 5) return "Quốc tế Lao động"
        if (d == 2 && m == 9) return "Quốc khánh"
        return null
    }
    private fun lunarHoliday(info: DayInfo): String? {
        if (info.lunarDay == 1 && info.lunarMonth == 1) return "Tết Nguyên Đán"
        if (info.lunarDay == 15 && info.lunarMonth == 1) return "Rằm tháng Giêng"
        if (info.lunarDay == 15 && info.lunarMonth == 8) return "Tết Trung Thu"
        if (info.lunarDay == 10 && info.lunarMonth == 3) return "Giỗ Tổ Hùng Vương"
        return null
    }
    private fun renderDetail() {
        val info = LunarCalendar.solarToLunar(selectedDay, viewMonth, viewYear)
        tvSolarDay.text = "$selectedDay"
        tvWeekday.text = weekdayName(info.weekday)
        val leap = if (info.lunarLeap) " (nhuận)" else ""
        tvLunarDate.text = "Âm lịch: ${info.lunarDay}/${info.lunarMonth}/${info.lunarYear}$leap"
        tvCanChi.text = "${info.dayCanChi} - Tháng ${info.monthCanChi} - Năm ${info.yearCanChi}"
        val hol = holidayName(selectedDay, viewMonth) ?: lunarHoliday(info)
        if (hol == null) tvHoliday.visibility = View.GONE else { tvHoliday.visibility = View.VISIBLE; tvHoliday.text = hol }
        tvSummary.text = "${weekdayName(info.weekday)}, $selectedDay/$viewMonth/$viewYear"
        detailGrid.removeAllViews()
        addRow("Tiết khí", info.tietKhi)
        addRow("Trực", info.truc)
        addRow("Giờ hoàng đạo", info.goodHours)
        addRow("Tháng âm", LunarCalendar.lunarMonthName(info.lunarMonth, info.lunarLeap))
        noteKey = "note_${viewYear}_${viewMonth}_$selectedDay"
        loadingNote = true
        edtNote.setText(getPreferences(Context.MODE_PRIVATE).getString(noteKey, ""))
        loadingNote = false
    }
    private fun addRow(label: String, value: String) {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.Horizontal
        row.setPadding(0, 8, 0, 8)
        val a = TextView(this)
        a.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.42f)
        a.text = label; a.textSize = 13f; a.setTextColor(Color.parseColor("#8B93A7"))
        val b = TextView(this)
        b.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.58f)
        b.text = value; b.textSize = 13.5f; b.setTypeface(b.typeface, Typeface.BOLD)
        b.setTextColor(Color.parseColor("#232946"))
        row.addView(a); row.addView(b)
        detailGrid.addView(row)
        val div = View(this)
        div.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
        div.setBackgroundColor(Color.parseColor("#EDEFF5"))
        detailGrid.addView(div)
    }
    private inner class DayAdapter : RecyclerView.Adapter<DayVH>() {
        override fun getItemCount() = cells.size
        override fun onCreateViewHolder(p: ViewGroup, t: Int): DayVH {
            val root = LinearLayout(this@MainActivity)
            root.orientation = LinearLayout.VERTICAL
            root.gravity = Gravity.CENTER
            root.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            root.setPadding(2, 6, 2, 6)
            val s = TextView(this@MainActivity)
            s.gravity = Gravity.CENTER; s.textSize = 15f; s.setTypeface(s.typeface, Typeface.BOLD)
            val l = TextView(this@MainActivity)
            l.gravity = Gravity.CENTER; l.textSize = 11f
            root.addView(s); root.addView(l)
            return DayVH(root, s, l)
        }
        override fun onBindViewHolder(h: DayVH, pos: Int) {
            val cell = cells[pos]
            h.solar.text = "${cell.day}"
            h.lunar.text = if (cell.info.lunarDay == 1) "${cell.info.lunarMonth}/${cell.info.lunarDay}" else "${cell.info.lunarDay}"
            val isSel = cell.day == selectedDay
            val isSun = cell.info.weekday == 0
            val isHol = holidayName(cell.day, viewMonth) != null
            if (isSel) {
                h.root.setBackgroundResource(com.meowcode.lichamduong.R.drawable.bg_day_selected)
                h.solar.setTextColor(Color.WHITE)
                h.lunar.setTextColor(Color.WHITE)
            } else {
                h.root.setBackgroundResource(com.meowcode.lichamduong.R.drawable.bg_day_normal)
                h.solar.setTextColor(if (isSun || isHol) Color.parseColor("#E5484D") else Color.parseColor("#232946"))
                h.lunar.setTextColor(if (isSun || isHol) Color.parseColor("#E5484D") else Color.parseColor("#8B93A7"))
            }
            if (cell.info.lunarDay == 1 && !isSel) h.lunar.setTextColor(Color.parseColor("#B7791F"))
            h.root.setOnClickListener { selectDay(cell.day) }
        }
    }
    private class DayVH(val root: LinearLayout, val solar: TextView, val lunar: TextView) : RecyclerView.ViewHolder(root)
}
