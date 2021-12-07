package com.datn.thesocialnetwork.core.util

import android.util.Log
import android.view.View
import android.widget.TextView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

object TimeUtils {
    fun Long.getDateTimeFormatFromMillis(
        format: String = Const.DATE_TIME_FORMAT_MESSAGE,
    ): String {
            val formatter = SimpleDateFormat(format, Locale.getDefault())
            val calendar = Calendar.getInstance()
                .apply { timeInMillis = this@getDateTimeFormatFromMillis }
            return formatter.format(calendar.time)
    }

    fun Long.getDateTimeFormat(): String {
            val formatter = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance().apply { timeInMillis = this@getDateTimeFormat }
            return formatter.format(calendar.time)
    }

    fun showTimeDetail(createdTime: Long): String {
        val timeFromDb = createdTime.getDateTimeFormatFromMillis()
        val simpleDateFormat = SimpleDateFormat(Const.DATE_TIME_FORMAT_MESSAGE, Locale.getDefault())
        val fomatterDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentTime = simpleDateFormat.format(Calendar.getInstance().time)
        var date1: Date? = null
        var date2: Date? = null
        try {
            date1 = simpleDateFormat.parse(timeFromDb)
            date2 = simpleDateFormat.parse(currentTime)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        Log.d("TIME", "fromdb $createdTime - $timeFromDb - ${date1.toString()} --- current ${date2.toString()}")
        val diff = date2!!.time - date1!!.time

        val diffSeconds = diff / 1000
        val diffMinutes = diff / (60 * 1000)
        val diffHours = diff / (60 * 60 * 1000)
        val diffDay = diff / (60 * 60 * 1000 * 24)

        if (diffSeconds < 60) {
            return "$diffSeconds giây trước"
        }
        if (diffSeconds > 60 && diffMinutes < 60) {
            return "$diffMinutes phút trước"
        }
        if (diffMinutes > 60 && diffHours < 24) {
            return "$diffHours giờ trước"
        }
        if (diffHours > 24 && diffDay < 30) {
            return "$diffDay ngày trước"
        }
        return simpleDateFormat.format(date1)
    }
}