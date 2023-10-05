package reducers

import com.benasher44.uuid.Uuid
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.query.EventQueryResult
import org.reduxkotlin.Reducer

data class EventCalendarState(
    val selectedLinkedGuild: LinkedGuild? = null,
    val selectedDateRange: Pair<LocalDate?, LocalDate?> = Clock.System.todayIn(TimeZone.UTC)
        .minus(DatePeriod(months = 1)) to null,
    val events: List<EventQueryResult>? = null
) {
    fun getEvent(id: Uuid): EventQueryResult? {
        return events?.find { it.event.id == id }
    }
}

data class GuildSelected(val linkedGuild: LinkedGuild)
data class EventsUpdated(val events: List<EventQueryResult>)
data class DateRangeSelected(val after: LocalDate?, val before: LocalDate?)

val eventCalendarReducer: Reducer<ApplicationState> = { s: ApplicationState, a: Any ->
    val old = s.calendar
    val new = when (a) {
        is GuildSelected -> old.copy(selectedLinkedGuild = a.linkedGuild)
        is EventsUpdated -> old.copy(events = a.events)
        is DateRangeSelected -> old.copy(selectedDateRange = a.after to a.before)
        else -> old
    }
    s.copy(calendar = new)
}
