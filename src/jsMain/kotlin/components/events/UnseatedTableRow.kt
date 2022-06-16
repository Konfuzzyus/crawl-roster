package components.events

import mui.icons.material.CancelOutlined
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

external interface UnseatedRowProps : Props {
    var event: Event
    var me: Player
}

val UnseatedRow = FC<UnseatedRowProps> { props ->
    val store = useContext(StoreContext)

    val players = props.event.unseated

    val isRegistered = props.event.isRegistered(props.me)
    val isPlayer = players.map(Player::id).contains(props.me.id)

    TableRow {
        TableCell {
            colSpan = 2
            Chip {
                size = Size.medium
                label = ReactNode("Unseated Players")
                icon = CancelOutlined.create()
                color = ChipColor.default
                variant = ChipVariant.filled
            }
        }
        TableCell {
            colSpan = 2
            Seating {
                seatedPlayers = players
            }
        }
    }

}