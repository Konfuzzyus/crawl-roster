package components

import mui.material.*
import org.codecranachan.roster.Event
import org.codecranachan.roster.Player
import react.FC
import react.Props
import react.ReactNode
import react.useContext
import reducers.StoreContext
import reducers.joinTable

external interface EventLineupProps : Props {
    var event: Event
    var me: Player
}

val EventLineup = FC<EventLineupProps> { props ->
    val store = useContext(StoreContext)

    TableContainer {
        Table {
            size = Size.small
            TableBody {
                props.event.roster.forEach { entry ->
                    val table = entry.key
                    val players = entry.value
                    val isRegistered = props.event.isRegistered(props.me)
                    val isHost = props.me.id == table?.dungeonMaster?.id
                    val isPlayer = players.map(Player::id).contains(props.me.id)

                    TableRow {
                        TableCell {
                            colSpan = 4
                            Chip {
                                size = Size.small
                                label = ReactNode(table?.let { "${it.dungeonMaster.name}'s Table" }
                                    ?: "Unseated")
                                if (isRegistered && !isPlayer) {
                                    variant = ChipVariant.outlined
                                    onClick = {
                                        store.dispatch(joinTable(props.event, table))
                                    }
                                } else {
                                    variant = ChipVariant.filled
                                }
                                color = if (isHost || isPlayer) {
                                    ChipColor.primary
                                } else {
                                    ChipColor.default
                                }
                            }
                        }
                    }

                    players.forEachIndexed { idx, player ->
                        TableRow {
                            if (idx == 0) TableCell { rowSpan = players.size }
                            TableCell {
                                +"${player.name}"
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