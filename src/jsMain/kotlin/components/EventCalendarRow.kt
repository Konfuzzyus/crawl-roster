package components

import com.benasher44.uuid.Uuid
import components.events.PlaySessionOccupancyIndicator
import csstype.px
import mui.icons.material.KeyboardArrowDown
import mui.icons.material.KeyboardArrowUp
import mui.material.Button
import mui.material.ButtonGroup
import mui.material.Collapse
import mui.material.IconButton
import mui.material.Size
import mui.material.Stack
import mui.material.StackDirection
import mui.material.TableCell
import mui.material.TableRow
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.useContext
import react.useEffectOnce
import react.useState
import reducers.StoreContext
import reducers.registerPlayer
import reducers.registerTable
import reducers.unregisterPlayer
import reducers.unregisterTable

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
            val me = store.state.identity.data?.profile
            val isRegistered = me?.let { event.isRegistered(it) } == true
            val isHosting = me?.let { event.isHosting(it) } == true
            TableCell {
                IconButton {
                    size = Size.small
                    onClick = { setIsOpen(!isOpen) }
                    if (isOpen) KeyboardArrowUp {} else KeyboardArrowDown {}
                }
            }
            TableCell {
                Typography {
                    variant = TypographyVariant.h6
                    +event.getFormattedDate()
                }
            }
            TableCell {
                +"${event.playerCount()} of ${event.tableSpace()}"
            }
            TableCell {
                Stack {
                    direction = responsive(StackDirection.row)
                    spacing = responsive(2)
                    event.tables().forEach {
                        PlaySessionOccupancyIndicator {
                            occupancy = it
                        }
                    }
                }
            }
            TableCell {
                ButtonGroup {
                    when {
                        isRegistered -> {
                            Button {
                                onClick = { store.dispatch(unregisterPlayer(event)) }
                                +"Cancel Registration"
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
                                +"Register"
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
                colSpan = 5
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

