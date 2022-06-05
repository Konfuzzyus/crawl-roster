package components

import csstype.*
import emotion.react.useTheme
import mui.icons.material.ErrorOutline
import mui.material.Chip
import mui.material.Stack
import mui.material.StackDirection
import mui.system.Box
import mui.system.responsive
import mui.system.sx
import org.codecranachan.roster.Event
import react.*
import reducers.StoreContext

external interface EventDetailsProps : Props {
    var event: Event
}

val EventDetails = FC<EventDetailsProps> { props ->
    val store = useContext(StoreContext)
    val profile = store.state.identity.data?.profile

    Box {
        sx {
            padding = Padding(10.px, 10.px)
        }
        if (profile == null) {
            Chip {
                icon = ErrorOutline.create()
                label = ReactNode("Please sign up first")
            }
        } else if (props.event.roster.isEmpty()) {
            Chip {
                icon = ErrorOutline.create()
                label = ReactNode("No one has registered for this event")
            }
        } else {
            Stack {
                spacing = responsive(5.px)
                direction = responsive(StackDirection.row)
                EventLineup {
                    event = props.event
                    me = profile
                }
            }
        }
    }
}
