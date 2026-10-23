package com.pascm.inspseg.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "MX"))
private val timeFormat = SimpleDateFormat("HH:mm", Locale("es", "MX"))

fun Long.asDateTime(): String = dateTimeFormat.format(Date(this))

fun Long.asTime(): String = timeFormat.format(Date(this))

/** Texto del plazo restante para una reinspeccion programada. */
fun deadlineLabel(dueAt: Long, now: Long = System.currentTimeMillis()): String {
    val remaining = dueAt - now
    if (remaining <= 0) return "Plazo vencido"
    val hours = TimeUnit.MILLISECONDS.toHours(remaining)
    if (hours >= 1) return "Vence en ${hours} h"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining)
    return "Vence en ${minutes} min"
}
