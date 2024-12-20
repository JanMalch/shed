package io.github.janmalch.shed

import android.icu.text.DateFormat
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date

internal class UiDateTimeFormatter(
    private val tz: TimeZone = TimeZone.currentSystemDefault(),
    private val clock: Clock = Clock.System,
) {
    private val time = DateFormat.getTimeInstance(DateFormat.MEDIUM)
    private val dateTime = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)

    fun format(timestamp: Instant): String {
        val dt = timestamp.toLocalDateTime(tz)
        val now = clock.now().toLocalDateTime(tz)
        val date = Date(timestamp.toEpochMilliseconds())
        return if (dt.date == now.date) {
            time.format(date)
        } else {
            dateTime.format(date)
        }
    }
}

internal val LocalUiDateTimeFormatter = staticCompositionLocalOf { UiDateTimeFormatter() }