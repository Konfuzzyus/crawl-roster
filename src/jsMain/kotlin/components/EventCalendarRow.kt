package components

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
import org.codecranachan.roster.query.EventQueryResult
import react.FC
import react.Props
import react.useState

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
    var result: EventQueryResult
}

val EventCalendarBodyRow = FC<EventCalendarRowProps> { props ->
    val result = props.result
    val (isOpen, setIsOpen) = useState(false)

    TableRow {
        TableCell {
            IconButton {
                size = Size.small
                onClick = { setIsOpen(!isOpen) }
                if (isOpen) KeyboardArrowUp {} else KeyboardArrowDown {}
            }
        }
        TableCell {
            Stack {
                direction = responsive(StackDirection.column)
                Typography {
                    variant = TypographyVariant.h6
                    +result.event.formattedDate
                }
                Typography {
                    variant = TypographyVariant.caption
                    val locationStr = result.event.details.location ?: ""
                    val timeStr = result.event.details.time?.let { "at $it" } ?: ""
                    +"$locationStr $timeStr"
                }
            }
        }
        TableCell {
            +"${result.tableSpace} with ${result.playerCount} registered"
        }
        TableCell {
            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(2)
                result.tables.forEach {
                    PlayTableIndicator {
                        data = it.value
                    }
                }
            }
        }
        TableCell {
            EventActions {
                targetEvent = result
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
                    this.result = result
                }
            }
        }
    }
}

