package reducers

import api.*
import com.benasher44.uuid.Uuid
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.codecranachan.roster.*
import org.reduxkotlin.Thunk

private val scope = MainScope()

fun updateUserId(): Thunk<ApplicationState> = { dispatch, _, _ ->
    scope.launch {
        val result = fetchUserId()
        dispatch(UserIdentified(result))
    }
}

fun signUpPlayer(p: Player): Thunk<ApplicationState> = { dispatch, _, _ ->
    scope.launch {
        addPlayer(p)
        val result = fetchUserId()
        dispatch(UserIdentified(result))
    }
}

fun updateLinkedGuilds(): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val guilds = fetchLinkedGuilds()
        dispatch(LinkedGuildsUpdated(guilds))
        if (guilds.isNotEmpty() && getState().calendar.selectedGuild == null) {
            dispatch(selectGuild(guilds[0]))
        }
    }
}

fun linkGuild(g: Guild): Thunk<ApplicationState> = { dispatch, _, _ ->
    scope.launch {
        addLinkedGuild(g)
        dispatch(selectGuild(g))
        dispatch(updateLinkedGuilds())
    }
}

fun selectGuild(g: Guild): Thunk<ApplicationState> = { dispatch, _, _ ->
    scope.launch {
        dispatch(GuildSelected(g))
        dispatch(updateEvents())
    }
}

fun createEvent(e: Event): Thunk<ApplicationState> = { dispatch, _, _ ->
    scope.launch {
        addEvent(e)
        dispatch(updateEvents())
    }
}

fun registerPlayer(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val player = getState().identity.data?.profile
        if (player != null) {
            addEventRegistration(e, player)
            dispatch(updateEvents())
        }
    }
}

fun unregisterPlayer(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val player = getState().identity.data?.profile
        if (player != null) {
            removeEventRegistration(e, player)
            dispatch(updateEvents())
        }
    }
}

fun updateEvents(): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val calendar = getState().calendar
        if (calendar.selectedGuild != null) {
            val events = fetchEvents(calendar.selectedGuild)
            dispatch(EventsUpdated(events))
        }
    }
}

fun registerTable(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val dm = getState().identity.data?.profile
        if (dm != null) {
            addTableHosting(e, dm)
            dispatch(updateEvents())
        }
    }
}

fun unregisterTable(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val dm = getState().identity.data?.profile
        if (dm != null) {
            removeTableHosting(e, dm)
            dispatch(updateEvents())
        }
    }
}

fun joinTable(e: Event, t: Table?): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        val p = getState().identity.data?.profile
        if (p != null) {
            updateEventRegistration(e, p, t)
            dispatch(updateEvents())
        }
    }
}

fun updateTableDetails(tableId: Uuid, details: TableDetails): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        updateTableHosting(tableId, details)
        dispatch(updateEvents())
    }
}
