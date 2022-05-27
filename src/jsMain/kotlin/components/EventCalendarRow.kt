package components

import com.benasher44.uuid.Uuid
import csstype.px
import mui.icons.material.KeyboardArrowDown
import mui.icons.material.KeyboardArrowUp
import mui.material.*
import mui.system.sx
import react.*
import reducers.*

val EventCalendarHeaderRow = FC<Props> {
    TableRow {
        TableCell {}
        TableCell {
            +"Date"
        }
        TableCell {
            +"Players"
        }
        TableCell {
            +"Tables"
        }
        TableCell { }
    }
}

external interface EventCalendarRowProps : Props {
    var eventId: Uuid
}

val EventCalendarBodyRow = FC<EventCalendarRowProps> { props ->
    val store = useContext(StoreContext)
    val (event, setEvent) = useState(store.state.calendar.getEvent(props.eventId))
    val (isOpen, setIsOpen) = useState(false)

    useEffectOnce {
        val unsubscribe = store.subscribe {
            setEvent(store.state.calendar.getEvent(props.eventId))
        }
        cleanup(unsubscribe)
    }
    if (event == null) {
        TableRow {
            TableCell {
                colSpan = 4
                +"Unable to retrieve event information"
            }
        }
    } else {
        TableRow {
            val isRegistered =
                store.state.identity.data?.profile?.id.let { id -> event.registeredPlayers.any { it.id == id } }
            val isHosting =
                store.state.identity.data?.profile?.id.let { id -> event.hostedTables.any { it.dungeonMaster.id == id } }
            TableCell {
                IconButton {
                    size = Size.small
                    onClick = { setIsOpen(!isOpen) }
                    if (isOpen) KeyboardArrowUp {} else KeyboardArrowDown {}
                }
            }
            TableCell {
                +"${event.date.dayOfWeek.name}, ${event.date}"
            }
            TableCell {
                +"${event.registeredPlayers.size}"
            }
            TableCell {
                +"${event.hostedTables.size}"
            }
            TableCell {
                ButtonGroup {
                    when {
                        isRegistered -> {
                            Button {
                                onClick = { store.dispatch(unregisterPlayer(event)) }
                                +"Register"
                            }
                        }
                        isHosting -> {
                            Button {
                                onClick = { store.dispatch(unregisterTable(event)) }
                                +"Cancel Table"
                            }
                        }
                        else -> {
                            Button {
                                onClick = { store.dispatch(registerPlayer(event)) }
                                +"Cancel Registration"
                            }
                            Button {
                                onClick = { store.dispatch(registerTable(event)) }
                                +"Host Table"
                            }
                        }
                    }
                }
            }
        }

        TableRow {
            TableCell {
                colSpan = 4
                sx {
                    paddingBottom = 0.px
                    paddingTop = 0.px
                }
                Collapse {
                    `in` = isOpen
                    timeout = "auto"
                    EventDetails {
                        this.event = event
                    }
                }
            }
        }
    }
}

