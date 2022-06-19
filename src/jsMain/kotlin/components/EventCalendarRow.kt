package components

import com.benasher44.uuid.Uuid
import components.events.EventActions
import components.events.PlayTableIndicator
import csstype.px
import mui.icons.material.KeyboardArrowDown
import mui.icons.material.KeyboardArrowUp
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

val EventCalendarHeaderRow = FC<Props> {
    TableRow {
        TableCell {}
        TableCell {
            +"Date"
        }
        TableCell {
            +"Event Capacity"
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
                if (event.capacity() >= 0) {
                    +"${event.openSeatCount()} open seats"
                } else {
                    +"${event.waitingListLength()} on waiting list"
                }

            }
            TableCell {
                Stack {
                    direction = responsive(StackDirection.row)
                    spacing = responsive(2)
                    event.sessions.forEach {
                        PlayTableIndicator {
                            session = it
                        }
                    }
                }
            }
            TableCell {
                EventActions {
                    targetEvent = event
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

