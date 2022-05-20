package reducers

import org.codecranachan.roster.UserIdentity
import org.reduxkotlin.Reducer

data class IdentityState(
    val user: UserIdentity? = null
)

data class IdentifyUserAction(val user: UserIdentity?)
class LogoutUserAction

val identityReducer: Reducer<ApplicationState> = { s: ApplicationState, a: Any ->
    val old = s.identity
    val new = when (a) {
        is IdentifyUserAction -> old.copy(user = a.user)
        is LogoutUserAction -> old.copy(user = null)
        else -> old
    }
    s.copy(identity = new)
}
