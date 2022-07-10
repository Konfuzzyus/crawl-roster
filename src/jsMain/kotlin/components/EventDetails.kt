package components

import csstype.Padding
import csstype.px
import mui.icons.material.ErrorOutline
import mui.material.Chip
import mui.system.Box
import mui.system.sx
import org.codecranachan.roster.Event
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useContext
import reducers.StoreContext

external interface EventDetailsProps : Props {
    var event: Event
}

val EventDetails = FC<EventDetailsProps> { props ->
    val store = useContext(StoreContext)
    val profile = store.state.identity.player

    Box {
        sx {
            padding = Padding(10.px, 10.px)
        }
        if (profile == null) {
            Chip {
                icon = ErrorOutline.create()
                label = ReactNode("Please sign up first")
            }
        } else if (props.event.unseated.isEmpty() && props.event.sessions.isEmpty()) {
            Chip {
                icon = ErrorOutline.create()
                label = ReactNode("No one has registered for this event")
            }
        } else {
            EventLineup {
                event = props.event
                me = profile
            }
        }
    }
}
