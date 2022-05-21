package reducers

import org.codecranachan.roster.Identity
import org.reduxkotlin.Reducer

data class IdentityState(
    val profile: Identity? = null
)

data class IdentifyUserAction(val profile: Identity?)
class LogoutUserAction

val identityReducer: Reducer<ApplicationState> = { s: ApplicationState, a: Any ->
    val old = s.identity
    val new = when (a) {
        is IdentifyUserAction -> old.copy(profile = a.profile)
        is LogoutUserAction -> old.copy(profile = null)
        else -> old
    }
    s.copy(identity = new)
}
