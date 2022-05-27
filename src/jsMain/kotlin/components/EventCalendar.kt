package components

import mui.material.*
import org.codecranachan.roster.Guild
import org.codecranachan.roster.Player
import react.*
import reducers.StoreContext


external interface EventCalendarProps : Props {
    var guild: Guild
    var profile: Player
}

val EventCalendar = FC<EventCalendarProps> { props ->
    val store = useContext(StoreContext)
    val (events, setEvents) = useState(store.state.calendar.events)

    useEffectOnce {
        val unsubscribe = store.subscribe { setEvents(store.state.calendar.events) }
        cleanup(unsubscribe)
    }

    Box {
        if (events == null) {
            CircularProgress {}
        } else {
            TableContainer {
                Table {
                    stickyHeader = true
                    TableHead {
                        EventCalendarHeaderRow {}
                    }
                    TableBody {
                        events.forEach {
                            EventCalendarBodyRow {
                                eventId = it.id
                            }
                        }
                    }
                }
            }
            SubmitEvent {
                guild = props.guild
            }
        }
    }
}
