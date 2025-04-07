package components

import mui.icons.material.ErrorOutline
import mui.material.Chip
import mui.system.Box
import mui.system.sx
import org.codecranachan.roster.query.EventQueryResult
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.use
import reducers.StoreContext
import web.cssom.Padding
import web.cssom.px

external interface EventDetailsProps : Props {
    var result: EventQueryResult
}

val EventDetails = FC<EventDetailsProps> { props ->
    val store = use(StoreContext)!!
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
        } else if (props.result.players.isEmpty() && props.result.tables.isEmpty()) {
            Chip {
                icon = ErrorOutline.create()
                label = ReactNode("No one has registered for this event")
            }
        } else {
            EventLineup {
                result = props.result
                me = profile.player
            }
        }
    }
}
