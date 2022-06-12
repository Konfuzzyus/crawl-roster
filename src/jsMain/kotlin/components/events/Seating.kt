package components.events

import mui.material.AvatarGroup
import org.codecranachan.roster.Player
import react.FC
import react.Props

external interface SeatingProps : Props {
    var seatedPlayers: List<Player>
    var totalSeats: IntRange
}

val Seating = FC<SeatingProps> { props ->
    AvatarGroup {
        max = 12
        (0..maxOf(props.seatedPlayers.size, props.totalSeats.last)).forEach { idx ->
            mui.material.Avatar {
                if (idx < props.seatedPlayers.size) {
                    src = props.seatedPlayers[idx].avatarUrl
                    alt = props.seatedPlayers[idx].name
                } else {
                    if (idx < props.totalSeats.first) {
                        mui.icons.material.PriorityHigh {}
                    } else {
                        mui.icons.material.QuestionMark {}
                    }
                }
            }
        }
    }
}