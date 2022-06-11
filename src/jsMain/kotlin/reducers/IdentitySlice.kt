package reducers

import org.codecranachan.roster.Identity
import org.reduxkotlin.Reducer

data class IdentityState(
    val data: Identity? = null,
    val isLoaded: Boolean = false
)

data class UserIdentified(val profile: Identity?)
class UserLoggedOut

val identityReducer: Reducer<ApplicationState> = { s: ApplicationState, a: Any ->
    val old = s.identity
    val new = when (a) {
        is UserIdentified -> old.copy(data = a.profile, isLoaded = true)
        is UserLoggedOut -> old.copy(data = null)
        else -> old
    }
    s.copy(identity = new)
}
