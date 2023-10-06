package components.events

import mui.icons.material.CancelOutlined
import mui.material.Chip
import mui.material.ChipColor
import mui.material.ChipVariant
import mui.material.Size
import mui.material.TableCell
import mui.material.TableRow
import org.codecranachan.roster.core.Player
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface UnseatedPlayersRowProps : Props {
    var unseatedPlayers: List<Player>
}

val UnseatedPlayersRow = FC<UnseatedPlayersRowProps> { props ->
    TableRow {
        TableCell {
            colSpan = 2
            Chip {
                size = Size.medium
                label = ReactNode("Unseated Players")
                color = ChipColor.default
                variant = ChipVariant.outlined
                icon = CancelOutlined.create()
            }
        }
        TableCell {
        }
        TableCell {
            Seating {
                seatedPlayers = props.unseatedPlayers
            }
        }
    }
}