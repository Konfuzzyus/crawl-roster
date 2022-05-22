package reducers

data class ApplicationState(
    val identity: IdentityState = IdentityState(),
    val calendar: EventCalendarState = EventCalendarState()
)
