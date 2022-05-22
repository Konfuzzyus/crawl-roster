package reducers

import api.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.codecranachan.roster.Event
import org.codecranachan.roster.Guild
import org.codecranachan.roster.Player
import org.reduxkotlin.Thunk
import react.dom.svg.ReactSVG.g

private val scope = MainScope()

fun updateUserId(): Thunk<ApplicationState> = { dispatch, _, _ ->
    scope.launch {
        val result = fetchUserId()
        dispatch(IdentifyUserAction(result))
    }
}

fun signUpPlayer(p: Player): Thunk<ApplicationState> = { dispatch, _, _ ->
    scope.launch {
        addPlayer(p)
        val result = fetchUserId()
        dispatch(IdentifyUserAction(result))
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
        val events = fetchEvents(g)
        dispatch(GuildSelected(g))
        dispatch(EventsUpdated(events))
    }
}

fun createEvent(e: Event): Thunk<ApplicationState> = { dispatch, getState, _ ->
    scope.launch {
        addEvent(e)
        val calendar = getState().calendar
        if (calendar.selectedGuild?.id == e.guildId) {
            val events = fetchEvents(calendar.selectedGuild)
            dispatch(EventsUpdated(events))
        }
    }
}