package components.events

import mui.material.Button
import mui.material.ButtonGroup
import mui.material.ListItemText
import mui.material.Menu
import mui.material.MenuItem
import org.codecranachan.roster.Event
import org.codecranachan.roster.TableState
import org.w3c.dom.Element
import react.FC
import react.Props
import react.dom.events.MouseEventHandler
import react.useContext
import react.useEffectOnce
import react.useState
import reducers.EventEditorOpened
import reducers.StoreContext
import reducers.registerPlayer
import reducers.registerTable
import reducers.unregisterPlayer
import reducers.unregisterTable

external interface EventActionsProps : Props {
    var targetEvent: Event
}

val EventActions = FC<EventActionsProps> { props ->
    val myStore = useContext(StoreContext)
    var anchor by useState<Element>()

    var userIdentity by useState(myStore.state.identity.player)
    var currentGuild by useState(myStore.state.calendar.selectedLinkedGuild)

    useEffectOnce {
        val unsubscribe = myStore.subscribe {
            userIdentity = myStore.state.identity.player
            currentGuild = myStore.state.calendar.selectedLinkedGuild
        }
        cleanup(unsubscribe)
    }

    val isRegistered = userIdentity?.let { props.targetEvent.isRegistered(it) } == true
    val isHosting = userIdentity?.let { props.targetEvent.isHosting(it) } == true

    ButtonGroup {
        when {
            isRegistered -> {
                Button {
                    onClick = { myStore.dispatch(unregisterPlayer(props.targetEvent)) }
                    +"Cancel Registration"
                }
            }
            isHosting -> {
                Button {
                    onClick = { myStore.dispatch(unregisterTable(props.targetEvent)) }
                    +"Cancel Table"
                }
            }
            else -> {
                Button {
                    onClick = { anchor = it.currentTarget }
                    +"Sign Up"
                }
                Button {
                    onClick = { myStore.dispatch(registerTable(props.targetEvent)) }
                    +"Host Table"
                }
            }
        }
        if (currentGuild?.let { userIdentity?.isAdminOf(it.id) } == true) {
            Button {
                onClick = { myStore.dispatch(EventEditorOpened(props.targetEvent)) }
                +"Edit Event"
            }
        }
    }

    val handleClose: MouseEventHandler<*> = { anchor = null }
    Menu {
        open = anchor != null
        if (anchor != null) {
            anchorEl = { anchor as Element }
        }
        onClose = handleClose

        props.targetEvent.tables.forEach {
            MenuItem {
                if (it.getState() == TableState.Full) {
                    disabled = true
                }
                onClick = { e ->
                    myStore.dispatch(registerPlayer(props.targetEvent, it))
                    handleClose(e)
                }
                ListItemText { +"Join ${it.getName()}" }
            }
        }

        MenuItem {
            onClick = { e ->
                myStore.dispatch(registerPlayer(props.targetEvent))
                handleClose(e)
            }
            ListItemText { +"Join Waiting List" }
        }
    }
}