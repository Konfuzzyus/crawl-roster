package reducers

import org.codecranachan.roster.DiscordUserInfo
import org.codecranachan.roster.Player
import org.reduxkotlin.Reducer

data class IdentityState(
    val discord: DiscordUserInfo? = null,
    val player: Player? = null,
    val isLoaded: Boolean = false
)

data class UserIdentified(val discord: DiscordUserInfo?, val player: Player?)
class UserLoggedOut

val identityReducer: Reducer<ApplicationState> = { s: ApplicationState, a: Any ->
    val old = s.identity
    val new = when (a) {
        is UserIdentified -> old.copy(discord = a.discord, player = a.player, isLoaded = true)
        is UserLoggedOut -> old.copy(discord = null, player = null)
        else -> old
    }
    s.copy(identity = new)
}
