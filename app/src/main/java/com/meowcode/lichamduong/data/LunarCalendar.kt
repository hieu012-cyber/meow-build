package com.meowcode.lichamduong.data

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.floor

/**
 * Thông tin đầy đủ của một ngày sau khi quy đổi âm - dương.
 */
data class DayInfo(
    val day: Int,
    val month: Int,
    val year: Int,
    val lunarDay: Int,
    val lunarMonth: Int,
    val lunarYear: Int,
    val lunarLeap: Boolean,
    val julianDay: Int,
    val weekday: Int, // 0 = Chủ nhật, 1 = Thứ hai, ... 6 = Thứ bảy
    val dayCanChi: String,
    val monthCanChi: String,
    val yearCanChi: String,
    val tietKhi: String,
    val truc: String,
    val goodHours: String
) {
    /** Chuỗi ngày âm lịch dạng 4/10 (nhuận) */
    val lunarText: String
        get() = if (lunarLeap) "$lunarDay/$lunarMonth nhuận" else "$lunarDay/$lunarMonth"

    val weekdayName: String
        get() = WEEKDAYS[weekday]

    companion object {
        val WEEKDAYS = arrayOf("Chủ nhật", "Thứ hai", "Thứ ba", "Thứ tư", "Thứ năm", "Thứ sáu", "Thứ bảy")
        val WEEKDAYS_SHORT = arrayOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
    }
}

/**
 * Lõi thuật toán chuyển đổi âm lịch - dương lịch cho múi giờ Việt Nam (GMT+7).
 * Thuật toán dựa trên chu kỳ Sóc (new moon) và vị trí Mặt Trời (tiết khí).
 */
object LunarCalendar {

    /** Múi giờ Việt Nam */
    const val TIME_ZONE = 7.0

    private const val SYNODIC = 29.530588853
    private const val NEW_MOON_BASE = 2415021.076998695

    val CAN = arrayOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
    val CHI = arrayOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")

    /** 24 tiết khí, bắt đầu từ Xuân Phân (kinh độ Mặt Trời 0 độ) */
    val TIET_KHI = arrayOf(
        "Xuân Phân", "Thanh Minh", "Cốc Vũ", "Lập Hạ", "Tiểu Mãn", "Mang Chủng",
        "Hạ Chí", "Tiểu Thử", "Đại Thử", "Lập Thu", "Xử Thử", "Bạch Lộ",
        "Thu Phân", "Hàn Lộ", "Sương Giáng", "Lập Đông", "Tiểu Tuyết", "Đại Tuyết",
        "Đông Chí", "Tiểu Hàn", "Đại Hàn", "Lập Xuân", "Vũ Thủy", "Kinh Trập"
    )

    /** 12 trực */
    val TRUC = arrayOf("Kiến", "Trừ", "Mãn", "Bình", "Định", "Chấp", "Phá", "Nguy", "Thành", "Thu", "Khai", "Bế")

    /** Bảng giờ hoàng đạo theo chi của ngày (mỗi nhóm 2 ngày: Tý-Ngọ, Sửu-Mùi, ...) */
    private val GOOD_HOURS = arrayOf(
        intArrayOf(1, 3, 6, 8, 9, 11),
        intArrayOf(2, 3, 5, 8, 10, 11),
        intArrayOf(0, 1, 4, 5, 7, 10),
        intArrayOf(0, 2, 3, 6, 7, 9),
        intArrayOf(2, 4, 5, 8, 9, 11),
        intArrayOf(1, 4, 6, 7, 10, 11)
    )

    // ------------------------------------------------------------------
    // Chuyển đổi ngày dương lịch <-> số ngày Julius
    // ------------------------------------------------------------------

    fun jdFromDate(dd: Int, mm: Int, yy: Int): Int {
        val a = (14 - mm) / 12
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        var jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        if (jd < 2299161) {
            jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083
        }
        return jd
    }

    /** Trả về mảng [ngày, tháng, năm] dương lịch từ số ngày Julius */
    fun jdToDate(jd: Int): IntArray {
        val a: Int
        val b: Int
        val c: Int
        if (jd > 2299160) {
            a = jd + 32044
            b = (4 * a + 3) / 146097
            c = a - 146097 * b / 4
        } else {
            b = 0
            c = jd + 32082
        }
        val d = (4 * c + 3) / 1461
        val e = c - 1461 * d / 4
        val m = (5 * e + 2) / 153
        val day = e - (153 * m + 2) / 5 + 1
        val month = m + 3 - 12 * (m / 10)
        val year = 100 * b + d - 4800 + m / 10
        return intArrayOf(day, month, year)
    }

    // ------------------------------------------------------------------
    // Tính toán thiên văn
    // ------------------------------------------------------------------

    /** Thời điểm Sóc thứ k (đơn vị ngày Julius) */
    private fun newMoon(k: Int): Double {
        val t = k / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        val dr = Math.PI / 180
        var jd1 = 2415020.75933 + SYNODIC * k + 0.0001178 * t2 - 0.000000155 * t3
        jd1 += 0.00033 * Math.sin((166.56 + 132.87 * t - 0.009173 * t2) * dr)
        val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
        val mpr = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
        val f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3
        var c1 = (0.1734 - 0.000393 * t) * Math.sin(m * dr) + 0.0021 * Math.sin(2 * dr * m)
        c1 -= 0.4068 * Math.sin(mpr * dr)
        c1 += 0.0161 * Math.sin(dr * 2 * mpr) - 0.0004 * Math.sin(dr * 3 * mpr)
        c1 += 0.0104 * Math.sin(dr * 2 * f) - 0.0051 * Math.sin(dr * (m + mpr))
        c1 -= 0.0074 * Math.sin(dr * (m - mpr)) + 0.0004 * Math.sin(dr * (2 * f + m))
        c1 -= 0.0004 * Math.sin(dr * (2 * f - m)) - 0.0006 * Math.sin(dr * (2 * f + mpr))
        c1 += 0.0010 * Math.sin(dr * (2 * f - mpr)) + 0.0005 * Math.sin(dr * (2 * mpr + m))
        val deltat = if (t < -11) {
            0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3
        } else {
            -0.000278 + 0.000265 * t + 0.000262 * t2
        }
        return jd1 + c1 - deltat
    }

    /** Kinh độ Mặt Trời (độ) tại thời điểm jdn */
    private fun sunLongitude(jdn: Double): Double {
        val t = (jdn - 2451545.0) / 36525.0
        val t2 = t * t
        val dr = Math.PI / 180
        val m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
        var dl = (1.914600 - 0.004817 * t - 0.000014 * t2) * Math.sin(dr * m)
        dl += (0.019993 - 0.000101 * t) * Math.sin(dr * 2 * m) + 0.000290 * Math.sin(dr * 3 * m)
        var l = l0 + dl
        l *= dr
        l -= Math.PI * 2 * floor(l / (Math.PI * 2))
        return l / dr
    }

    /** Ngày (theo múi giờ) bắt đầu tháng âm lịch chứa Sóc thứ k */
    private fun getNewMoonDay(k: Int, timeZone: Double = TIME_ZONE): Int {
        return floor(newMoon(k) + 0.5 + timeZone / 24.0).toInt()
    }

    /** Chỉ số tiết khí (mỗi 30 độ) tại ngày dayNumber */
    private fun getSunLongitude(dayNumber: Int, timeZone: Double = TIME_ZONE): Int {
        return floor(sunLongitude(dayNumber - 0.5 - timeZone / 24.0) / 30.0).toInt()
    }

    /** Ngày Julius của tháng 11 âm lịch trong năm yy */
    private fun getLunarMonth11(yy: Int, timeZone: Double = TIME_ZONE): Int {
        val off = jdFromDate(31, 12, yy) - 2415021
        val k = (off / SYNODIC).toInt()
        var nm = getNewMoonDay(k, timeZone)
        if (getSunLongitude(nm, timeZone) >= 9) {
            nm = getNewMoonDay(k - 1, timeZone)
        }
        return nm
    }

    /** Độ lệch tháng nhuận so với tháng 11 âm lịch */
    private fun getLeapMonthOffset(a11: Int, timeZone: Double = TIME_ZONE): Int {
        val k = ((a11 - 2415021) / SYNODIC + 0.5).toInt()
        var last = 0
        var i = 1
        var arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        do {
            last = arc
            i++
            arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        } while (arc != last && i < 14)
        return i - 1
    }

    // ------------------------------------------------------------------
    // API công khai
    // ------------------------------------------------------------------

    /** Chuyển ngày dương lịch sang thông tin đầy đủ (âm lịch, can chi, tiết khí...) */
    fun solarToLunar(dd: Int, mm: Int, yy: Int): DayInfo {
        val dayNumber = jdFromDate(dd, mm, yy)
        val k = floor((dayNumber - NEW_MOON_BASE) / SYNODIC).toInt()
        var monthStart = getNewMoonDay(k + 1)
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k)
        }
        var a11 = getLunarMonth11(yy)
        var b11 = a11
        var lunarYear: Int
        if (a11 >= monthStart) {
            lunarYear = yy
            a11 = getLunarMonth11(yy - 1)
        } else {
            lunarYear = yy + 1
            b11 = getLunarMonth11(yy + 1)
        }
        val lunarDay = dayNumber - monthStart + 1
        val diff = floor((monthStart - a11) / 29.0).toInt()
        var lunarLeap = false
        var lunarMonth = diff + 11
        if (b11 - a11 > 365) {
            val leapMonthDiff = getLeapMonthOffset(a11)
            if (diff >= leapMonthDiff) {
                lunarMonth = diff + 10
                if (diff == leapMonthDiff) {
                    lunarLeap = true
                }
            }
        }
        if (lunarMonth > 12) {
            lunarMonth -= 12
        }
        if (lunarMonth >= 11 && diff < 4) {
            lunarYear -= 1
        }

        // Can chi của ngày
        val dayCan = (dayNumber + 9) % 10
        val dayChi = (dayNumber + 1) % 12
        // Can chi của năm
        val yearCan = (lunarYear + 6) % 10
        val yearChi = (lunarYear + 8) % 12
        // Can chi của tháng (tháng Giêng là Dần)
        val monthCan = ((yearCan * 2 + 2) + (lunarMonth - 1)) % 10
        val monthChi = (lunarMonth + 1) % 12

        // Tiết khí theo kinh độ Mặt Trời (mỗi 15 độ)
        val sunLong = sunLongitude(dayNumber - 0.5 - TIME_ZONE / 24.0)
        val tietKhi = TIET_KHI[(floor(sunLong / 15.0).toInt() % 24 + 24) % 24]

        // Trực trong ngày
        val trucIndex = ((dayChi - monthChi) % 12 + 12) % 12

        // Giờ hoàng đạo
        val hours = GOOD_HOURS[dayChi % 6].joinToString(", ") { chiIdx ->
            val start = (chiIdx * 2 + 23) % 24
            val end = (start + 2) % 24
            "${CHI[chiIdx]} (${pad(start)}-${pad(end)})"
        }

        return DayInfo(
            day = dd,
            month = mm,
            year = yy,
            lunarDay = lunarDay,
            lunarMonth = lunarMonth,
            lunarYear = lunarYear,
            lunarLeap = lunarLeap,
            julianDay = dayNumber,
            weekday = (dayNumber + 1) % 7,
            dayCanChi = "${CAN[dayCan]} ${CHI[dayChi]}",
            monthCanChi = "${CAN[monthCan]} ${CHI[monthChi]}",
            yearCanChi = "${CAN[yearCan]} ${CHI[yearChi]}",
            tietKhi = tietKhi,
            truc = TRUC[trucIndex],
            goodHours = hours
        )
    }

    /** Chuyển ngày âm lịch sang dương lịch, trả về [ngày, tháng, năm] */
    fun lunarToSolar(lunarDay: Int, lunarMonth: Int, lunarYear: Int, lunarLeap: Boolean): IntArray {
        val a11: Int
        val b11: Int
        if (lunarMonth < 11) {
            a11 = getLunarMonth11(lunarYear - 1)
            b11 = getLunarMonth11(lunarYear)
        } else {
            a11 = getLunarMonth11(lunarYear)
            b11 = getLunarMonth11(lunarYear + 1)
        }
        var off = lunarMonth - 11
        if (off < 0) off += 12
        if (b11 - a11 > 365) {
            val leapOff = getLeapMonthOffset(a11)
            var leapMonth = leapOff - 2
            if (leapMonth < 0) leapMonth += 12
            if (lunarLeap && lunarMonth != leapMonth) {
                return intArrayOf(0, 0, 0)
            } else if (lunarLeap || off >= leapOff) {
                off += 1
            }
        }
        val k = floor(0.5 + (a11 - NEW_MOON_BASE) / SYNODIC).toInt()
        val monthStart = getNewMoonDay(k + off)
        return jdToDate(monthStart + lunarDay - 1)
    }

    // ------------------------------------------------------------------
    // Tiện ích
    // ------------------------------------------------------------------

    private fun pad(n: Int): String = if (n < 10) "0$n" else "$n"

    /** Lấy ngày hôm nay theo múi giờ Việt Nam */
    fun today(): DayInfo {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"))
        return solarToLunar(
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR)
        )
    }

    /** Số ngày của một tháng dương lịch */
    fun daysInMonth(month: Int, year: Int): Int {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"))
        cal.set(year, month - 1, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /** Tên tháng âm lịch, ví dụ: Tháng Mười, Tháng Chạp, Tháng Giêng, kèm chữ nhuận */
    fun lunarMonthName(lunarMonth: Int, isLeap: Boolean): String {
        val base = when (lunarMonth) {
            1 -> "Tháng Giêng"
            2 -> "Tháng Hai"
            3 -> "Tháng Ba"
            4 -> "Tháng Tư"
            5 -> "Tháng Năm"
            6 -> "Tháng Sáu"
            7 -> "Tháng Bảy"
            8 -> "Tháng Tám"
            9 -> "Tháng Chín"
            10 -> "Tháng Mười"
            11 -> "Tháng Mười Một"
            else -> "Tháng Chạp"
        }
        return if (isLeap) "$base (nhuận)" else base
    }

    /** Cộng/trừ tháng, trả về [tháng, năm] */
    fun shiftMonth(month: Int, year: Int, delta: Int): IntArray {
        var total = year * 12 + (month - 1) + delta
        val newYear = total / 12
        val newMonth = total % 12 + 1
        return intArrayOf(newMonth, newYear)
    }
}
