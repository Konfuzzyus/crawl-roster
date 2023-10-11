package org.codecranachan.roster.bot

import kotlinx.datetime.toJavaLocalDate
import org.codecranachan.roster.core.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle
import java.time.temporal.ChronoField

private val EVENT_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendValue(ChronoField.DAY_OF_MONTH, 2)
    .appendLiteral('-')
    .appendValue(ChronoField.MONTH_OF_YEAR, 2)
    .appendLiteral('-')
    .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
    .toFormatter()


fun Event.getChannelName(): String {
    return EVENT_DATE_FORMATTER.format(date.toJavaLocalDate())
}

fun Event.getChannelTopic(): String {
    return "Role playing event on ${formattedDate} posted on Crawl Roster"
}

fun String.parseAsEventDate(): LocalDate? {
    return runCatching {
        EVENT_DATE_FORMATTER.parse(this, LocalDate::from)
    }.getOrElse { null }
}