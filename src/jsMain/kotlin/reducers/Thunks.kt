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
import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Table
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
        val account = getState().identity.player
        if (account != null) {
            addEventRegistration(e.id, account.player.id, t?.dungeonMasterId)
            dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        }
    }
}

fun unregisterPlayer(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val account = getState().identity.player
        if (account != null) {
            removeEventRegistration(e.id, account.player.id)
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
        val account = getState().identity.player
        if (account != null) {
            addTableHosting(e.id, account.player.id)
            dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        }
    }
}

fun unregisterTable(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val account = getState().identity.player
        if (account != null) {
            removeTableHosting(e.id, account.player.id)
            dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        }
    }
}

fun joinTable(e: Event, t: Table?): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val account = getState().identity.player
        if (account != null) {
            updateEventRegistration(e.id, account.player.id, t?.dungeonMasterId)
            dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        }
    }
}

fun updateTableDetails(eventId: Uuid, dungeonMasterId: Uuid, details: Table.Details): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        updateTableHosting(eventId, dungeonMasterId, details)
        dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
    }
}

fun updatePlayerDetails(details: Player.Details): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        updatePlayer(details)
        dispatch(updateUserId())
        dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
    }
}

fun updateEventDetails(eventId: Uuid, details: Event.Details): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        updateEvent(eventId, details)
        dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
    }
}
