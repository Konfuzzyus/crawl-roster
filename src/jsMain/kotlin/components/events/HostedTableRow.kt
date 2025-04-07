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
import react.use
import reducers.StoreContext
import reducers.TableEditorOpened
import reducers.addRegistration
import reducers.updateRegistration

external interface HostedTableRowProps : Props {
    var me: Player
    var eventData: EventQueryResult
    var tableData: ResolvedTable
}

val HostedTableRow = FC<HostedTableRowProps> { props ->
    val store = use(StoreContext)!!

    val isDm = props.tableData.isDungeonMaster(props.me.id)
    val isPc = props.tableData.isPlayer(props.me.id)
    val isHosting = props.eventData.isHosting(props.me.id)
    val isRegistered = props.eventData.isRegistered(props.me.id)
    val isFull = props.tableData.isFull

    TableRow {
        TableCell {
            colSpan = 2
            Chip {
                size = Size.medium
                label = ReactNode(props.tableData.name)
                when {
                    isDm -> {
                        color = ChipColor.primary
                        variant = ChipVariant.filled
                        icon = ModeEdit.create()
                        onClick = {
                            store.dispatch(TableEditorOpened(props.tableData.table!!))
                        }
                    }
                    isPc -> {
                        color = ChipColor.primary
                        variant = ChipVariant.filled
                        icon = CheckCircleOutline.create()
                        onClick = {
                            store.dispatch(updateRegistration(props.eventData.event, null))
                        }
                    }
                    isHosting || isFull -> {
                        icon = BlockOutlined.create()
                        color = ChipColor.default
                        variant = ChipVariant.filled
                    }
                    else -> {
                        color = ChipColor.default
                        variant = ChipVariant.outlined
                        icon = CircleOutlined.create()
                        onClick = {
                            if (isRegistered) {
                                store.dispatch(updateRegistration(props.eventData.event, props.tableData.table!!))
                            } else {
                                store.dispatch(addRegistration(props.eventData.event, props.tableData.table!!))
                            }
                        }
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