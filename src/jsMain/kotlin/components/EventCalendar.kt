package components

import org.codecranachan.roster.Guild
import org.codecranachan.roster.Player
import org.reduxkotlin.Store
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ol
import react.useEffectOnce
import react.useState
import reducers.ApplicationState


external interface EventCalendarProps : Props {
    var store: Store<ApplicationState>
    var guild: Guild
    var profile: Player
}

val EventCalendar = FC<EventCalendarProps> { props ->
    val (events, setEvents) = useState(props.store.state.calendar.events)

    useEffectOnce {
        val unsubscribe = props.store.subscribe { setEvents(props.store.state.calendar.events) }
        cleanup(unsubscribe)
    }

    div {
        if (events == null) {
            +"Loading events"
        } else {
            if (events.isEmpty()) {
                +"No events found"
            } else {
                ol {
                    events.forEach {
                        li {
                            EventEntry {
                                store = props.store
                                eventId = it.id
                            }
                        }
                    }
                }
            }
            SubmitEvent {
                store = props.store
                guild = props.guild
            }
        }

    }
}
