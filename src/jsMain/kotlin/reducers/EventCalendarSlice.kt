package reducers

import com.benasher44.uuid.Uuid
import org.codecranachan.roster.Event
import org.codecranachan.roster.Guild
import org.reduxkotlin.Reducer

data class EventCalendarState(
    val selectedGuild: Guild? = null,
    val events: List<Event>? = null
) {
    fun getEvent(id: Uuid): Event? {
        return events?.find { it.id == id }
    }
}

data class GuildSelected(val guild: Guild)
data class EventsUpdated(val events: List<Event>)

val eventCalendarReducer: Reducer<ApplicationState> = { s: ApplicationState, a: Any ->
    val old = s.calendar
    val new = when (a) {
        is GuildSelected -> old.copy(selectedGuild = a.guild)
        is EventsUpdated -> old.copy(events = a.events)
        else -> old
    }
    s.copy(calendar = new)
}
