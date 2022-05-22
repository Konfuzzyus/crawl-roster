package reducers

import org.reduxkotlin.*

data class ApplicationState(
    val identity: IdentityState = IdentityState(),
    val calendar: EventCalendarState = EventCalendarState(),
    val server: RosterServerState = RosterServerState()
)

fun createApplicationStore(): Store<ApplicationState> {
    return createStore(
        combineReducers(identityReducer, eventCalendarReducer, rosterServerReducer),
        ApplicationState(),
        applyMiddleware(createThunkMiddleware())
    )
}