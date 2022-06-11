package reducers

import org.reduxkotlin.*
import react.FC
import react.PropsWithChildren
import react.createContext

data class ApplicationState(
    val identity: IdentityState = IdentityState(),
    val calendar: EventCalendarState = EventCalendarState(),
    val server: RosterServerState = RosterServerState(),
    val ui: InterfaceState = InterfaceState()
)

fun createApplicationStore(): Store<ApplicationState> {
    return createStore(
        combineReducers(identityReducer, eventCalendarReducer, rosterServerReducer, interfaceReducer),
        ApplicationState(),
        applyMiddleware(createThunkMiddleware())
    )
}

val StoreContext = createContext<Store<ApplicationState>>()

val StoreModule = FC<PropsWithChildren> { props ->
    StoreContext(createApplicationStore()) {
        +props.children
    }
}
