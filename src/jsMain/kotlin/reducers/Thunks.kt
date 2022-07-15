package reducers

import api.addEvent
import api.addEventRegistration
import api.addTableHosting
import api.fetchEvents
import api.fetchPlayerInfo
import api.fetchServerSettings
import api.removeEventRegistration
import api.removeTableHosting
import api.updateEvent
import api.updateEventRegistration
import api.updatePlayer
import api.updateTableHosting
import com.benasher44.uuid.Uuid
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.codecranachan.roster.Event
import org.codecranachan.roster.EventDetails
import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.PlayerDetails
import org.codecranachan.roster.Table
import org.codecranachan.roster.TableDetails
import org.reduxkotlin.Thunk

private val scope = MainScope()

fun updateUserId(): Thunk<ApplicationState> = { dispatch, _, _ ->
    scope.launch {
        val player = fetchPlayerInfo()
        dispatch(UserIdentified(player))
    }
}

fun updateServerSettings(): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val settings = fetchServerSettings()
        dispatch(ServerSettingsUpdated(settings))
        if (settings.linkedGuilds.isNotEmpty() && getState().calendar.selectedLinkedGuild == null) {
            dispatch(selectGuild(settings.linkedGuilds[0]))
        }
    }
}

fun selectGuild(g: LinkedGuild): Thunk<ApplicationState> = { dispatch, _, _ ->
    scope.launch {
        dispatch(GuildSelected(g))
        dispatch(updateEvents(g))
    }
}

fun createEvent(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        addEvent(e)
        dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
    }
}

fun registerPlayer(e: Event, t: Table? = null): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val player = getState().identity.player
        if (player != null) {
            addEventRegistration(e, player, t)
            dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        }
    }
}

fun unregisterPlayer(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val player = getState().identity.player
        if (player != null) {
            removeEventRegistration(e, player)
            dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        }
    }
}

fun updateEvents(g: LinkedGuild?): Thunk<ApplicationState> = { dispatch, _, _ ->
    scope.launch {
        if (g == null) {
            dispatch(EventsUpdated(listOf()))
        } else {
            val events = fetchEvents(g)
            dispatch(EventsUpdated(events))
        }
    }
}

fun registerTable(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val dm = getState().identity.player
        if (dm != null) {
            addTableHosting(e, dm)
            dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        }
    }
}

fun unregisterTable(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val dm = getState().identity.player
        if (dm != null) {
            removeTableHosting(e, dm)
            dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        }
    }
}

fun joinTable(e: Event, t: Table?): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val p = getState().identity.player
        if (p != null) {
            updateEventRegistration(e, p, t)
            dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        }
    }
}

fun updateTableDetails(tableId: Uuid, details: TableDetails): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        updateTableHosting(tableId, details)
        dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
    }
}

fun updatePlayerDetails(details: PlayerDetails): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        updatePlayer(details)
        dispatch(updateUserId())
        dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
    }
}

fun updateEventDetails(eventId: Uuid, details: EventDetails): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        updateEvent(eventId, details)
        dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
    }
}
