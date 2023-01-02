package components.events

import mui.icons.material.BlockOutlined
import mui.icons.material.CheckCircleOutline
import mui.icons.material.CircleOutlined
import mui.icons.material.ModeEdit
import mui.material.Chip
import mui.material.ChipColor
import mui.material.ChipVariant
import mui.material.Size
import mui.material.Stack
import mui.material.StackDirection
import mui.material.TableCell
import mui.material.TableRow
import mui.material.Tooltip
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.query.EventQueryResult
import org.codecranachan.roster.query.ResolvedTable
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useContext
import reducers.StoreContext
import reducers.TableEditorOpened
import reducers.joinTable

external interface HostedTableRowProps : Props {
    var me: Player
    var eventData: EventQueryResult
    var tableData: ResolvedTable
}

val HostedTableRow = FC<HostedTableRowProps> { props ->
    val store = useContext(StoreContext)

    val isRegistered = props.eventData.isRegistered(props.me.id)
    val isHost = props.eventData.isHosting(props.me.id)
    val isPlayer = props.tableData.isPlayer(props.me.id)

    TableRow {
        TableCell {
            Chip {
                size = Size.medium
                label = ReactNode(props.tableData.name)
                when {
                    isHost -> {
                        color = ChipColor.primary
                        variant = ChipVariant.filled
                        icon = ModeEdit.create()
                        onClick = {
                            store.dispatch(TableEditorOpened(props.tableData.table!!))
                        }
                    }
                    isRegistered && isPlayer -> {
                        color = ChipColor.primary
                        variant = ChipVariant.filled
                        icon = CheckCircleOutline.create()
                        onClick = {
                            store.dispatch(joinTable(props.eventData.event, null))
                        }
                    }
                    isRegistered && !isPlayer -> {
                        color = ChipColor.default
                        variant = ChipVariant.outlined
                        icon = CircleOutlined.create()
                        onClick = {
                            store.dispatch(joinTable(props.eventData.event, props.tableData.table!!))
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
            Tooltip {
                title = ReactNode(props.tableData.table?.description ?: "DM has not yet hosted his table")
                Stack {
                    direction = responsive(StackDirection.column)
                    Typography {
                        variant = TypographyVariant.body1
                        +(props.tableData.table?.title ?: "Coming Soon")
                    }
                    Typography {
                        variant = TypographyVariant.caption
                        +(props.tableData.table?.settings ?: "")
                    }
                }
            }
        }
        TableCell {
            Seating {
                seatedPlayers = props.tableData.players
            }
        }
    }
}