package reducers

import org.codecranachan.roster.GuildRoster
import org.reduxkotlin.Reducer

data class RosterServerState(
    val settings: GuildRoster = GuildRoster()
)

data class ServerSettingsUpdated(val settings: GuildRoster)

val rosterServerReducer: Reducer<ApplicationState> = { s: ApplicationState, a: Any ->
    val old = s.server
    val new = when (a) {
        is ServerSettingsUpdated -> old.copy(settings = a.settings)
        else -> old
    }
    s.copy(server = new)
}
