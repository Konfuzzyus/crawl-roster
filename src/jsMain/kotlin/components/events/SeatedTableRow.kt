package components.events

import mui.icons.material.BlockOutlined
import mui.icons.material.CheckCircleOutline
import mui.icons.material.CircleOutlined
import mui.icons.material.ModeEdit
import mui.material.Chip
import mui.material.ChipColor
import mui.material.ChipVariant
import mui.material.Size
import mui.material.TableCell
import mui.material.TableRow
import org.codecranachan.roster.Event
import org.codecranachan.roster.Player
import org.codecranachan.roster.TableOccupancy
import org.codecranachan.roster.TableState
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useContext
import reducers.StoreContext
import reducers.TableEditorOpened
import reducers.joinTable

external interface SeatedTableRowProps : Props {
    var event: Event
    var me: Player
    var occupancy: TableOccupancy
}

val SeatedTableRow = FC<SeatedTableRowProps> { props ->
    val store = useContext(StoreContext)
    val players = props.occupancy.players
    val table = props.occupancy.table

    val isRegistered = props.event.isRegistered(props.me)
    val isHost = props.me.id == table.dungeonMaster.id
    val isPlayer = players.map(Player::id).contains(props.me.id)

    TableRow {
        TableCell {
            colSpan = 2
            Chip {
                size = Size.medium
                label = ReactNode(table.getName())
                when {
                    isHost -> {
                        color = ChipColor.primary
                        variant = ChipVariant.filled
                        icon = ModeEdit.create()
                        onClick = {
                            store.dispatch(TableEditorOpened(table))
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
                    isRegistered && !isPlayer && props.occupancy.getState() != TableState.Full -> {
                        color = ChipColor.default
                        variant = ChipVariant.outlined
                        icon = CircleOutlined.create()
                        onClick = {
                            store.dispatch(joinTable(props.event, table))
                        }
                    }
                    else -> {
                        icon = BlockOutlined.create()
                        color = ChipColor.default
                        variant = ChipVariant.filled
                    }
                }
            }
        }
        TableCell {
            colSpan = 2
            Seating {
                seatedPlayers = players
                totalSeats = table.details.playerRange
            }
        }
    }
}