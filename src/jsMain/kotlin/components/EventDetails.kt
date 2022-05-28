package components

import csstype.px
import mui.icons.material.ErrorOutline
import mui.material.*
import mui.system.sx
import org.codecranachan.roster.Event
import org.codecranachan.roster.Player
import org.codecranachan.roster.Table
import react.*
import reducers.StoreContext
import reducers.joinTable

external interface EventDetailsProps : Props {
    var event: Event
}

val EventDetails = FC<EventDetailsProps> { props ->
    val store = useContext(StoreContext)
    val me = store.state.identity.data?.profile

    if (me == null) {
        Chip {
            icon = ErrorOutline.create()
            label = ReactNode("Please sign up first")
        }
    } else if (props.event.roster.isEmpty()) {
        Chip {
            icon = ErrorOutline.create()
            label = ReactNode("No one has registered for this event")
        }
    } else {
        Paper {
            sx {
                marginBlock = 2.px
            }
            TableContainer {
                Table {
                    size = Size.small
                    TableBody {
                        props.event.roster.forEach { entry ->
                            val table = entry.key
                            val players = entry.value
                            val isRegistered = props.event.isRegistered(me)
                            val isHost = me.id == table?.dungeonMaster?.id
                            val isPlayer = players.map(Player::id).contains(me.id)

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
    }
}

external interface TableSelectorProps : Props {
    var availableTables: List<Table>
    var selectedTable: Table?
}

val TableSelector = FC<TableSelectorProps> { props ->
    val store = useContext(StoreContext)

    FormControl {
        variant = FormControlVariant.standard
        size = Size.small
        InputLabel {
            id = "guild-select-label"
            +"Table"
        }
        Select {
            id = "guild-select"
            labelId = "guild-select-label"
            value = (props.selectedTable?.id ?: "").unsafeCast<Nothing?>()
            label = ReactNode("Table")
            onChange = { e, _ ->

            }

            MenuItem {
                value = ""
                +"None"
            }
            props.availableTables.forEach {
                MenuItem {
                    value = it.id.toString()
                    +"${it.dungeonMaster.name}'s Table"
                }
            }
        }
    }
}
