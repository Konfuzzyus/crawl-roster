package components

import com.benasher44.uuid.Uuid
import org.reduxkotlin.Store
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ul
import react.useEffectOnce
import react.useState
import reducers.ApplicationState
import reducers.subscribePlayer
import reducers.unsubscribePlayer


external interface EventEntryProps : Props {
    var store: Store<ApplicationState>
    var eventId: Uuid
}

val EventEntry = FC<EventEntryProps> { props ->
    val (event, setEvent) = useState(props.store.state.calendar.getEvent(props.eventId))
    val (isOpen, setIsOpen) = useState(false)

    useEffectOnce {
        val unsubscribe = props.store.subscribe {
            setEvent(props.store.state.calendar.getEvent(props.eventId))
        }
        cleanup(unsubscribe)
    }

    div {
        if (event == null) {
            +"Missing event"
        } else {
            val isSubscribed =
                props.store.state.identity.data?.profile?.id.let { id -> event.registeredPlayers.any { it.id == id } }
            div {
                div {
                    onClick = { _ -> setIsOpen(!isOpen) }
                    +"${event.date.dayOfWeek.name}, ${event.date} - ${event.registeredPlayers.size} players registered"
                }
                button {
                    if (isSubscribed) +"Unsubscribe" else +"Subscribe"
                    onClick = { _ ->
                        if (isSubscribed) props.store.dispatch(unsubscribePlayer(event))
                        else props.store.dispatch(subscribePlayer(event))
                    }
                }
            }
            if (isOpen) {
                if (event.registeredPlayers.isEmpty()) {
                    div {
                        +"No registered Players"
                    }
                } else {
                    ul {
                        event.registeredPlayers.forEach {
                            li {
                                +"${it.discordHandle} (${it.name})"
                            }
                        }
                    }
                }

            }
        }
    }
}
