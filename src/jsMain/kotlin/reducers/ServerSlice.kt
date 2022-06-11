package reducers

import org.codecranachan.roster.Event
import org.codecranachan.roster.Guild
import org.reduxkotlin.Reducer

data class RosterServerState(
    val linkedGuilds: List<Guild>? = null
)

data class LinkedGuildsUpdated(val guilds: List<Guild>)

val rosterServerReducer: Reducer<ApplicationState> = { s: ApplicationState, a: Any ->
    val old = s.server
    val new = when (a) {
        is LinkedGuildsUpdated -> old.copy(linkedGuilds = a.guilds)
        else -> old
    }
    s.copy(server = new)
}
