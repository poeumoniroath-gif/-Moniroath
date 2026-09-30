package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WeekDayItem(
    val dateIso: String,
    val dayNameKhmer: String,
    val dayOfMonth: String,
    val isToday: Boolean
)

object Formatters {
    private val decimalFormat = DecimalFormat("#,###")
    private val dateIsoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeDisplayFormat = SimpleDateFormat("hh:mm a", Locale.US)
    private val dateKhmerFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    fun formatRiel(amount: Number): String {
        return "${decimalFormat.format(amount)} ៛"
    }

    fun formatUsd(rielAmount: Number): String {
        val usd = rielAmount.toDouble() / 4100.0
        return String.format(Locale.US, "$%.2f", usd)
    }

    fun getTodayIsoString(): String {
        return dateIsoFormat.format(Date())
    }

    fun getCurrentYear(): Int {
        val cal = Calendar.getInstance(Locale.US)
        return cal.get(Calendar.YEAR)
    }

    fun getCurrentMonth(): Int {
        val cal = Calendar.getInstance(Locale.US)
        return cal.get(Calendar.MONTH) + 1
    }

    fun shiftMonth(year: Int, month: Int, offset: Int): Pair<Int, Int> {
        val cal = Calendar.getInstance(Locale.US)
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.MONTH, offset)
        return Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
    }

    fun formatMonthYearKhmer(year: Int, month: Int): String {
        return "${getKhmerMonthName(month)} ឆ្នាំ $year"
    }

    fun formatTimestampToIso(timestamp: Long): String {
        return dateIsoFormat.format(Date(timestamp))
    }

    fun formatTimestampToTime(timestamp: Long): String {
        val timeStr = timeDisplayFormat.format(Date(timestamp))
        return timeStr.replace("AM", "ព្រឹក").replace("PM", "រសៀល")
    }

    fun getWeekBoundaries(referenceDateIso: String): Pair<String, String> {
        val cal = Calendar.getInstance(Locale.US)
        try {
            val parsed = dateIsoFormat.parse(referenceDateIso)
            if (parsed != null) cal.time = parsed
        } catch (_: Exception) {}
        cal.firstDayOfWeek = Calendar.MONDAY
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysToMonday = if (currentDayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - currentDayOfWeek
        cal.add(Calendar.DAY_OF_MONTH, daysToMonday)
        val startIso = dateIsoFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_MONTH, 6)
        val endIso = dateIsoFormat.format(cal.time)
        return Pair(startIso, endIso)
    }

    fun getDaysOfWeek(referenceDateIso: String): List<WeekDayItem> {
        val cal = Calendar.getInstance(Locale.US)
        try {
            val parsed = dateIsoFormat.parse(referenceDateIso)
            if (parsed != null) cal.time = parsed
        } catch (_: Exception) {}
        cal.firstDayOfWeek = Calendar.MONDAY
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysToMonday = if (currentDayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - currentDayOfWeek
        cal.add(Calendar.DAY_OF_MONTH, daysToMonday)
        val khmerNames = listOf("ចន្ទ (Mon)", "អង្គារ (Tue)", "ពុធ (Wed)", "ព្រហស្បតិ៍ (Thu)", "សុក្រ (Fri)", "សៅរ៍ (Sat)", "អាទិត្យ (Sun)")
        val todayIso = getTodayIsoString()
        val result = mutableListOf<WeekDayItem>()
        for (i in 0..6) {
            val dateStr = dateIsoFormat.format(cal.time)
            val dayNum = SimpleDateFormat("dd", Locale.US).format(cal.time)
            result.add(
                WeekDayItem(
                    dateIso = dateStr,
                    dayNameKhmer = khmerNames[i],
                    dayOfMonth = dayNum,
                    isToday = dateStr == todayIso
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return result
    }

    fun shiftWeek(referenceDateIso: String, offsetWeeks: Int): String {
        val cal = Calendar.getInstance(Locale.US)
        try {
            val parsed = dateIsoFormat.parse(referenceDateIso)
            if (parsed != null) cal.time = parsed
        } catch (_: Exception) {}
        cal.add(Calendar.WEEK_OF_YEAR, offsetWeeks)
        return dateIsoFormat.format(cal.time)
    }

    fun getKhmerMonthName(monthNumber: Int): String {
        return when (monthNumber) {
            1 -> "មករា (Jan)"
            2 -> "កុម្ភៈ (Feb)"
            3 -> "មីនា (Mar)"
            4 -> "មេសា (Apr)"
            5 -> "ឧសភា (May)"
            6 -> "មិថុនា (Jun)"
            7 -> "កក្កដា (Jul)"
            8 -> "សីហា (Aug)"
            9 -> "កញ្ញា (Sep)"
            10 -> "តុលា (Oct)"
            11 -> "វិច្ឆិកា (Nov)"
            12 -> "ធ្នូ (Dec)"
            else -> "ខែ $monthNumber"
        }
    }

    fun formatDateToKhmer(dateIso: String): String {
        return try {
            val parsed = dateIsoFormat.parse(dateIso)
            if (parsed != null) {
                val dayFormat = SimpleDateFormat("dd", Locale.US)
                val monthFormat = SimpleDateFormat("MM", Locale.US)
                val yearFormat = SimpleDateFormat("yyyy", Locale.US)

                val day = dayFormat.format(parsed)
                val month = monthFormat.format(parsed)
                val year = yearFormat.format(parsed)

                val khmerMonth = when (month) {
                    "01" -> "មករា"
                    "02" -> "កុម្ភៈ"
                    "03" -> "មីនា"
                    "04" -> "មេសា"
                    "05" -> "ឧសភា"
                    "06" -> "មិថុនា"
                    "07" -> "កក្កដា"
                    "08" -> "សីហា"
                    "09" -> "កញ្ញា"
                    "10" -> "តុលា"
                    "11" -> "វិច្ឆិកា"
                    "12" -> "ធ្នូ"
                    else -> month
                }

                "ថ្ងៃទី $day ខែ $khmerMonth ឆ្នាំ $year"
            } else {
                dateIso
            }
        } catch (e: Exception) {
            dateIso
        }
    }
}
