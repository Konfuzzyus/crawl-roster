package reducers

import api.addDmRegistration
import api.addEvent
import api.addPlayerRegistration
import api.deleteEvent
import api.fetchEvents
import api.fetchPlayerInfo
import api.fetchServerSettings
import api.fetchStats
import api.removeDmRegistration
import api.removePlayerRegistration
import api.updateDmRegistration
import api.updateEvent
import api.updatePlayer
import api.updatePlayerRegistration
import com.benasher44.uuid.Uuid
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.codecranachan.roster.LinkedGuild
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Table
import org.codecranachan.roster.query.EventStatisticsQueryResult
import org.reduxkotlin.thunk.Thunk

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
        dispatch(updateStats(g))
    }
}

fun selectCalendarRange(after: LocalDate?, before: LocalDate?): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        dispatch(DateRangeSelected(after, before))
        dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        dispatch(updateStats(getState().calendar.selectedLinkedGuild))
    }
}

fun createEvent(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        addEvent(e)
        dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
    }
}

fun cancelEvent(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        deleteEvent(e.id)
        dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
    }
}

fun addRegistration(e: Event, t: Table? = null): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val account = getState().identity.player
        if (account != null) {
            addPlayerRegistration(e.id, account.player.id, t?.dungeonMasterId)
            dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        }
    }
}

fun updateRegistration(e: Event, t: Table?): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val account = getState().identity.player
        if (account != null) {
            updatePlayerRegistration(e.id, account.player.id, t?.dungeonMasterId)
            dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        }
    }
}

fun updateRegistration(eventId: Uuid, playerId: Uuid, dmId: Uuid?): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        updatePlayerRegistration(eventId, playerId, dmId)
        dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
    }
}

fun removeRegistration(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val account = getState().identity.player
        if (account != null) {
            removePlayerRegistration(e.id, account.player.id)
            dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        }
    }
}

fun updateStats(g: LinkedGuild?): Thunk<ApplicationState> =
    { dispatch, getState, _ ->
        scope.launch {
            val (after, before) = getState().calendar.selectedDateRange
            if (g == null) {
                dispatch(StatisticsUpdated(EventStatisticsQueryResult()))
            } else {
                val stats = fetchStats(g, after, before)
                dispatch(StatisticsUpdated(stats))
            }
        }
    }

fun updateEvents(g: LinkedGuild?): Thunk<ApplicationState> =
    { dispatch, getState, _ ->
        scope.launch {
            val (after, before) = getState().calendar.selectedDateRange
            if (g == null) {
                dispatch(EventsUpdated(listOf()))
            } else {
                val events = fetchEvents(g, after, before)
                dispatch(EventsUpdated(events))
            }
        }
    }

fun registerTable(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val account = getState().identity.player
        if (account != null) {
            addDmRegistration(e.id, account.player.id)
            dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        }
    }
}

fun unregisterTable(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val account = getState().identity.player
        if (account != null) {
            removeDmRegistration(e.id, account.player.id)
            dispatch(updateEvents(getState().calendar.selectedLinkedGuild))
        }
    }
}

fun updateTableDetails(eventId: Uuid, dungeonMasterId: Uuid, details: Table.Details): Thunk<ApplicationState> =
    { dispatch, getState, _ ->
        scope.launch {
            updateDmRegistration(eventId, dungeonMasterId, details)
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
