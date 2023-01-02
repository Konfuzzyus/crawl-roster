package reducers

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.query.EventQueryResult
import org.reduxkotlin.Reducer

data class EventCalendarState(
    val selectedLinkedGuild: LinkedGuild? = null,
    val events: List<EventQueryResult>? = null
) {
    fun getEvent(id: Uuid): EventQueryResult? {
        return events?.find { it.event.id == id }
    }
}

data class GuildSelected(val linkedGuild: LinkedGuild)
data class EventsUpdated(val events: List<EventQueryResult>)

val eventCalendarReducer: Reducer<ApplicationState> = { s: ApplicationState, a: Any ->
    val old = s.calendar
    val new = when (a) {
        is GuildSelected -> old.copy(selectedLinkedGuild = a.linkedGuild)
        is EventsUpdated -> old.copy(events = a.events)
        else -> old
    }
    s.copy(calendar = new)
}
