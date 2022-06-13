package components

import components.events.SeatedTableRow
import components.events.UnseatedRow
import mui.material.Size
import mui.material.Table
import mui.material.TableBody
import mui.material.TableCell
import mui.material.TableContainer
import mui.material.TableRow
import org.codecranachan.roster.Event
import org.codecranachan.roster.Player
import org.codecranachan.roster.TableOccupancy
import react.FC
import react.Props

external interface EventLineupProps : Props {
    var event: Event
    var me: Player
}

val EventLineup = FC<EventLineupProps> { props ->
    TableContainer {
        Table {
            size = Size.small
            TableBody {
                props.event.roster.forEach { entry ->
                    if (entry.key == null) {
                        UnseatedRow {
                            event = props.event
                            me = props.me
                            players = entry.value
                        }
                    } else {
                        SeatedTableRow {
                            event = props.event
                            me = props.me
                            occupancy = TableOccupancy(entry.key!!, entry.value)
                        }
                    }

                    val players = entry.value

                    players.forEachIndexed { idx, player ->
                        TableRow {
                            if (idx == 0) TableCell { rowSpan = players.size }
                            TableCell {
                                +player.details.name
                            }
                            TableCell {
                                +"${player.discordHandle}"
                            }
                            TableCell { }
                        }
                    }
                }
            }
        }
    }
}