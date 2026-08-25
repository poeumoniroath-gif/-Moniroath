package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val decimalFormat = DecimalFormat("#,###")
    private val dateIsoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeDisplayFormat = SimpleDateFormat("hh:mm a", Locale.US)
    private val dateKhmerFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    fun formatRiel(amount: Number): String {
        return "${decimalFormat.format(amount)} ៛"
    }

    fun getTodayIsoString(): String {
        return dateIsoFormat.format(Date())
    }

    fun formatTimestampToIso(timestamp: Long): String {
        return dateIsoFormat.format(Date(timestamp))
    }

    fun formatTimestampToTime(timestamp: Long): String {
        val timeStr = timeDisplayFormat.format(Date(timestamp))
        return timeStr.replace("AM", "ព្រឹក").replace("PM", "រសៀល")
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
