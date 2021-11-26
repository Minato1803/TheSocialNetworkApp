package com.datn.thesocialnetwork.core.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

object TimeUtils {
    fun Long.getDateTimeFormatFromMillis(
        format: String = Const.DATE_TIME_FORMAT_MESSAGE
    ): String
    {

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            Instant.ofEpochMilli(this)
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime()
                .format(
                    DateTimeFormatter.ofPattern(
                        format,
                        Locale.getDefault()
                    )
                )
        }
        else
        {
            val formatter = SimpleDateFormat(format, Locale.getDefault())
            val calendar = Calendar.getInstance()
                .apply { timeInMillis = this@getDateTimeFormatFromMillis }
            return formatter.format(calendar.time)
        }
    }

    fun Long.getDateTimeFormat(): String
    {

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            Instant.ofEpochMilli(this)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(
                    DateTimeFormatter.ofPattern(
                        "HH:mm dd MMM yy",
                        Locale.getDefault()
                    )
                )
        }
        else
        {
            val formatter = SimpleDateFormat("HH:mm dd MMM yy", Locale.getDefault())
            val calendar = Calendar.getInstance().apply { timeInMillis = this@getDateTimeFormat }
            return formatter.format(calendar.time)
        }
    }
}