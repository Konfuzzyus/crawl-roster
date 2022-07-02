package components

import components.events.SeatedTableRow
import components.events.UnseatedRow
import mui.material.Divider
import mui.material.DividerTextAlign
import mui.material.Size
import mui.material.Table
import mui.material.TableBody
import mui.material.TableCell
import mui.material.TableContainer
import mui.material.TableRow
import mui.material.Typography
import org.codecranachan.roster.Event
import org.codecranachan.roster.Player
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
                props.event.sessions.forEach { session ->
                    SeatedTableRow {
                        event = props.event
                        me = props.me
                        occupancy = session
                    }
                    PlayerRow {
                        me = props.me
                        players = session.players
                        capacity = session.table.details.playerRange.last
                    }
                }

                UnseatedRow {
                    event = props.event
                    me = props.me
                }
                PlayerRow {
                    me = props.me
                    players = props.event.unseated
                    capacity = props.event.openSeatCount()
                }
            }
        }
    }
}

external interface PlayerRowProps : Props {
    var me: Player
    var players: List<Player>
    var capacity: Int?
}

val PlayerRow = FC<PlayerRowProps> { props ->
    props.players.forEachIndexed { idx, player ->
        if (idx == props.capacity) {
            TableRow {
                TableCell {
                    colSpan = 4
                    Divider {
                        textAlign = DividerTextAlign.left
                        Typography {
                            +"WAITING LIST"
                        }
                    }
                }
            }
        }
        TableRow {
            val cap = props.capacity ?: Int.MAX_VALUE
            val unseatedCount = minOf(props.players.size, cap)
            val waitingCount = maxOf(0, props.players.size - cap)
            when {
                idx == 0 && unseatedCount > 0 -> TableCell { rowSpan = unseatedCount }
                idx == props.capacity && waitingCount > 0 -> TableCell { rowSpan = waitingCount }
                else -> {}
            }
            TableCell {
                val p = if (props.me == player) "★" else ""
                +"$p ${player.details.name} (${player.discordHandle})"
            }
            TableCell { }
            TableCell { }
        }
    }
}