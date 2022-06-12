package components.events

import mui.icons.material.BlockOutlined
import mui.icons.material.CheckCircleOutline
import mui.icons.material.CircleOutlined
import mui.material.Avatar
import mui.material.AvatarGroup
import mui.material.Chip
import mui.material.ChipColor
import mui.material.ChipVariant
import mui.material.Size
import mui.material.TableCell
import mui.material.TableRow
import org.codecranachan.roster.Event
import org.codecranachan.roster.Player
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useContext
import reducers.StoreContext
import reducers.joinTable

external interface UnseatedRowProps : Props {
    var event: Event
    var me: Player
    var players: List<Player>
}

val UnseatedRow = FC<UnseatedRowProps> { props ->
    val store = useContext(StoreContext)

    val players = props.players

    val isRegistered = props.event.isRegistered(props.me)
    val isPlayer = players.map(Player::id).contains(props.me.id)

    TableRow {
        TableCell {
            colSpan = 2
            Chip {
                size = Size.medium
                label = ReactNode("Unseated")
                when {
                    isRegistered && isPlayer -> {
                        icon = CheckCircleOutline.create()
                        color = ChipColor.primary
                        variant = ChipVariant.filled
                    }
                    isRegistered && !isPlayer -> {
                        icon = CircleOutlined.create()
                        color = ChipColor.default
                        variant = ChipVariant.outlined
                        onClick = {
                            store.dispatch(joinTable(props.event, null))
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
            AvatarGroup {
                max = 12
                props.players.forEach {
                    Avatar {
                        src = it.avatarUrl
                        alt = it.name
                    }
                }
            }
        }
    }

}