package components.events

import kotlinx.browser.window
import mui.material.Button
import mui.material.ButtonColor
import mui.material.ButtonGroup
import mui.material.ListItemText
import mui.material.Menu
import mui.material.MenuItem
import org.codecranachan.roster.query.EventQueryResult
import web.dom.Element
import react.FC
import react.Props
import react.dom.events.MouseEventHandler
import react.use
import react.useEffectOnceWithCleanup
import react.useState
import reducers.EventEditorOpened
import reducers.StoreContext
import reducers.addRegistration
import reducers.cancelEvent
import reducers.registerTable
import reducers.removeRegistration
import reducers.unregisterTable

external interface EventActionsProps : Props {
    var targetEvent: EventQueryResult
}

val EventActions = FC<EventActionsProps> { props ->
    val myStore = use(StoreContext)!!
    var anchor by useState<Element>()

    var userIdentity by useState(myStore.state.identity.player)
    var currentGuild by useState(myStore.state.calendar.selectedLinkedGuild)

    useEffectOnceWithCleanup {
        val unsubscribe = myStore.subscribe {
            userIdentity = myStore.state.identity.player
            currentGuild = myStore.state.calendar.selectedLinkedGuild
        }
        onCleanup(unsubscribe)
    }

    val isRegistered = userIdentity?.let { props.targetEvent.isRegistered(it.player.id) } == true
    val isHosting = userIdentity?.let { props.targetEvent.isHosting(it.player.id) } == true

    ButtonGroup {
        when {
            isRegistered -> {
                Button {
                    onClick = { myStore.dispatch(removeRegistration(props.targetEvent.event)) }
                    +"Cancel Registration"
                }
            }

            isHosting -> {
                Button {
                    onClick = { myStore.dispatch(unregisterTable(props.targetEvent.event)) }
                    +"Cancel Table"
                }
            }

            else -> {
                Button {
                    onClick = { anchor = it.currentTarget }
                    +"Sign Up"
                }
                Button {
                    onClick = { myStore.dispatch(registerTable(props.targetEvent.event)) }
                    +"Host Table"
                }
            }
        }
        if (currentGuild?.let { userIdentity?.isAdminOf(it.id) } == true) {
            Button {
                onClick = { myStore.dispatch(EventEditorOpened(props.targetEvent.event)) }
                +"Edit Event"
            }
            Button {
                color = ButtonColor.warning
                onClick = { myStore.dispatch(cancelEvent(props.targetEvent.event)) }
                +"Delete Event"
            }
        }
    }

    val handleClose: MouseEventHandler<*> = { anchor = null }
    Menu {
        open = anchor != null
        if (anchor != null) {
            anchorEl = { anchor as web.dom.Element }
        }
        onClose = handleClose

        props.targetEvent.tables.values.forEach {
            MenuItem {
                if (it.isFull) {
                    disabled = true
                }
                onClick = { e ->
                    myStore.dispatch(addRegistration(props.targetEvent.event, it.table))
                    handleClose(e)
                }
                ListItemText { +"Join ${it.name}" }
            }
        }

        MenuItem {
            onClick = { e ->
                myStore.dispatch(addRegistration(props.targetEvent.event))
                handleClose(e)
            }
            ListItemText { +"Join Waiting List" }
        }
    }
}