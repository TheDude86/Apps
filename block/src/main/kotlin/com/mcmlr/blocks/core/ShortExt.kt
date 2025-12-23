package com.mcmlr.blocks.core

fun Short.minuteTimeFormat(): String {
    val minutes = this / 60
    val seconds = this % 60
    val secondsText = if (seconds < 10) "0${seconds}" else seconds.toString()

    return "$minutes:$secondsText"
}