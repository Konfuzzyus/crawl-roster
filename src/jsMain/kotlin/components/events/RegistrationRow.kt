package components.events

import csstype.Border
import csstype.LineStyle
import csstype.px
import mui.icons.material.AccessAlarm
import mui.icons.material.AccountCircle
import mui.icons.material.Person
import mui.material.SvgIconColor
import mui.material.SvgIconSize
import mui.material.TableCell
import mui.material.TableRow
import mui.system.sx
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
            if (props.me.id == props.registration.player.id)
                Person {
                    fontSize = SvgIconSize.small
                    color = SvgIconColor.primary
                }
        }
        TableCell {
            +props.registration.player.websiteMention
        }
        TableCell {
            val langs = props.registration.player.details.languages.joinToString(" ") { it.flag }
            val tier = props.registration.player.details.playTier.let { if (it == 0) "Beginner" else "Tier $it" }
            +"$langs $tier"
        }
        TableCell {
        }
    }
}