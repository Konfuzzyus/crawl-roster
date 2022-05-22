package reducers

import api.addLinkedGuild
import api.addPlayer
import api.fetchEvents
import api.fetchUserId
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.codecranachan.roster.Guild
import org.codecranachan.roster.Player
import org.reduxkotlin.Thunk

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

fun linkGuild(g: Guild): Thunk<ApplicationState> = { dispatch, _, _ ->
    scope.launch {
        addLinkedGuild(g)
        dispatch(selectGuild(g))
    }
}

fun selectGuild(g: Guild): Thunk<ApplicationState> = { dispatch, _, _ ->
    scope.launch {
        val events = fetchEvents(g)
        dispatch(GuildSelected(g))
        dispatch(EventsUpdated(events))
    }
}
