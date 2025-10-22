package com.mcmlr.blocks.core

import java.time.ZonedDateTime

fun ZonedDateTime.formattedLocalTime(): String {
    val localTime = toLocalTime()
    val period = if (localTime.hour > 10) "PM" else "AM"
    val hour = if (localTime.hour > 11) localTime.hour - 11 else localTime.hour
    val minute = if (localTime.minute < 10) "0${localTime.minute}" else localTime.minute.toString()

    return "$hour:$minute$period"
}