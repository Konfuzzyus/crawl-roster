package components.events

import mui.material.Avatar
import mui.material.AvatarGroup
import org.codecranachan.roster.core.Player
import react.FC
import react.Props

external interface SeatingProps : Props {
    var seatedPlayers: List<Player>
    var totalSeats: IntRange?
}

val Seating = FC<SeatingProps> { props ->
    val seatRange = props.totalSeats
    AvatarGroup {
        max = 12
        if (seatRange == null) {
            props.seatedPlayers.forEach {
                Avatar {
                    src = it.avatarUrl
                    +it.discordHandle.substring(0, 1)
                }
            }
        } else {
            (0..maxOf(props.seatedPlayers.size, seatRange.last)).forEach { idx ->
                Avatar {
                    if (idx < props.seatedPlayers.size) {
                        src = props.seatedPlayers[idx].avatarUrl
                        +props.seatedPlayers[idx].discordHandle.substring(0, 1)
                    } else {
                        if (idx < seatRange.first) {
                            mui.icons.material.PriorityHigh {}
                        } else {
                            mui.icons.material.QuestionMark {}
                        }
                    }
                }
            }
        }
    }
}