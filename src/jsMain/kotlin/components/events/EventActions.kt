package components.events

import mui.material.Button
import mui.material.ButtonGroup
import org.codecranachan.roster.Event
import react.FC
import react.Props
import react.useContext
import reducers.StoreContext
import reducers.registerPlayer
import reducers.registerTable
import reducers.unregisterPlayer
import reducers.unregisterTable

external interface EventActionsProps : Props {
    var targetEvent: Event
}

val EventActions = FC<EventActionsProps> { props ->
    val store = useContext(StoreContext)

    val me = store.state.identity.data?.profile
    val isRegistered = me?.let { props.targetEvent.isRegistered(it) } == true
    val isHosting = me?.let { props.targetEvent.isHosting(it) } == true

    ButtonGroup {
        when {
            isRegistered -> {
                Button {
                    onClick = { store.dispatch(unregisterPlayer(props.targetEvent)) }
                    +"Cancel Registration"
                }
            }
            isHosting -> {
                Button {
                    onClick = { store.dispatch(unregisterTable(props.targetEvent)) }
                    +"Cancel Table"
                }
            }
            else -> {
                Button {
                    onClick = { store.dispatch(registerPlayer(props.targetEvent)) }
                    +"Join Waiting List"
                }
                Button {
                    onClick = { store.dispatch(registerTable(props.targetEvent)) }
                    +"Host Table"
                }
            }
        }
    }
}