package components.events

import mui.material.TableCell
import mui.material.TableRow
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.query.ResolvedRegistration
import react.FC
import react.Props

external interface RegistrationRowProps : Props {
    var me: Player
    var registration: ResolvedRegistration
}

val RegistrationRow = FC<RegistrationRowProps> { props ->
    TableRow {
        TableCell {
            val p = if (props.me.id == props.registration.player.id) "★" else ""
            +"$p ${props.registration.player.websiteMention}"
        }
        TableCell {
            val langs = props.registration.player.details.languages.joinToString(" ") { it.flag }
            val tier = props.registration.player.details.playTier.let { if (it == 0) "Beginner" else "Tier $it" }
            +"$langs $tier"
        }
        TableCell {
            +"${props.registration.description}"
        }
    }
}