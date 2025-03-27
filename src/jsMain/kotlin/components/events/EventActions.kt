package components.events

import mui.material.Button
import mui.material.ButtonColor
import mui.material.ButtonGroup
import mui.material.ListItemText
import mui.material.Menu
import mui.material.MenuItem
import mui.material.Tooltip
import org.codecranachan.roster.query.EventQueryResult
import org.reduxkotlin.Store
import web.dom.Element
import react.FC
import react.Props
import react.ReactNode
import react.dom.events.MouseEventHandler
import react.use
import react.useEffectOnceWithCleanup
import react.useState
import reducers.ApplicationState
import reducers.EventEditorOpened
import reducers.StoreContext
import reducers.addRegistration
import reducers.cancelEvent
import reducers.registerTable
import reducers.removeRegistration
import reducers.unregisterTable
import web.file.File
import web.file.FilePropertyBag
import web.navigator.navigator
import web.share.ShareData

external interface EventActionsProps : Props {
    var targetEvent: EventQueryResult
}

val testFileShare = ShareData(
    files = arrayOf(File(arrayOf("test"), "test.txt", FilePropertyBag(type = "text/plain")))
)

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
                Tooltip {
                    title = ReactNode("Cancel your attendance")
                    Button {
                        onClick = { myStore.dispatch(removeRegistration(props.targetEvent.event)) }
                        +"Cancel Registration"
                    }
                }
            }

            isHosting -> {
                Tooltip {
                    title = ReactNode("Cancel your attendance and your table")
                    Button {
                        onClick = { myStore.dispatch(unregisterTable(props.targetEvent.event)) }
                        +"Cancel Table"
                    }
                }
            }

            else -> {
                Tooltip {
                    title = ReactNode("Sign up as a player")
                    Button {
                        onClick = { anchor = it.currentTarget }
                        +"Sign Up"
                    }
                }
                Tooltip {
                    title = ReactNode("Sign up as DM and host a table")
                    Button {
                        onClick = { myStore.dispatch(registerTable(props.targetEvent.event)) }
                        +"Host Table"
                    }
                }
            }
        }
        Tooltip {
            title = ReactNode("Copy announcement message to clipboard")
            Button {
                onClick = {
                    navigator.clipboard.writeTextAsync(compileAnnouncementString(myStore, props.targetEvent))
                }
                +"Share"
            }
        }
        if (currentGuild?.let { userIdentity?.isAdminOf(it.id) } == true) {
            Tooltip {
                title = ReactNode("Edit event details")
                Button {
                    onClick = { myStore.dispatch(EventEditorOpened(props.targetEvent.event)) }
                    +"Edit Event"
                }
            }
            Tooltip {
                title = ReactNode("Cancel the event")
                Button {
                    color = ButtonColor.warning
                    onClick = { myStore.dispatch(cancelEvent(props.targetEvent.event)) }
                    +"Cancel Event"
                }
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

fun compileAnnouncementString(store: Store<ApplicationState>, event: EventQueryResult): String {
    return buildString {
        append("The ${store.state.calendar.selectedLinkedGuild?.name} is hosting an event on ${event.event.formattedDate} and is currently accepting registrations.\n")
        append("\n")
        append("Available Tables:\n")
        append(
            event.rawTables.joinToString("~~\n") { table ->
                buildString {
                    append("> _Adventure:_ **${table.details.adventureTitle}**\n")
                    append("> _Character Level:_ ${table.details.levelRange}\n")
                    append("> _Player Limit:_ ${table.details.playerRange.last}\n")
                    append("> _Language:_ ${table.details.language.flag} ${table.details.language.name}\n")
                }
            }
        )
    }
}