package components

import mui.icons.material.CheckCircleOutline
import mui.icons.material.CircleOutlined
import mui.icons.material.ModeEdit
import mui.material.*
import org.codecranachan.roster.Event
import org.codecranachan.roster.Player
import react.*
import reducers.StoreContext
import reducers.TableEditorOpened
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
                                label = ReactNode(table?.getName() ?: "Unseated")
                                when {
                                    isHost && table != null -> {
                                        color = ChipColor.primary
                                        variant = ChipVariant.filled
                                        icon = ModeEdit.create()
                                        onClick = {
                                            store.dispatch(TableEditorOpened(table))
                                        }
                                    }
                                    isRegistered && !isPlayer -> {
                                        color = ChipColor.default
                                        variant = ChipVariant.outlined
                                        icon = CircleOutlined.create()
                                        onClick = {
                                            store.dispatch(joinTable(props.event, table))
                                        }
                                    }
                                    isRegistered && isPlayer -> {
                                        color = ChipColor.primary
                                        variant = ChipVariant.filled
                                        icon = CheckCircleOutline.create()
                                        onClick = {
                                            store.dispatch(joinTable(props.event, null))
                                        }
                                    }
                                    else -> {
                                        color = ChipColor.default
                                        variant = ChipVariant.filled
                                    }
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