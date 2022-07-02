package reducers

import org.codecranachan.roster.Server
import org.reduxkotlin.Reducer

data class RosterServerState(
    val settings: Server = Server()
)

data class ServerSettingsUpdated(val settings: Server)

val rosterServerReducer: Reducer<ApplicationState> = { s: ApplicationState, a: Any ->
    val old = s.server
    val new = when (a) {
        is ServerSettingsUpdated -> old.copy(settings = a.settings)
        else -> old
    }
    s.copy(server = new)
}
