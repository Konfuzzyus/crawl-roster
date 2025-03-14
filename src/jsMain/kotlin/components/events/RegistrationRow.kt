package components.events

import mui.icons.material.Person
import mui.material.Button
import mui.material.ButtonGroup
import mui.material.Size
import mui.material.SvgIconColor
import mui.material.SvgIconSize
import mui.material.TableCell
import mui.material.TableRow
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.query.ResolvedRegistration
import react.FC
import react.Props
import react.use
import reducers.StoreContext
import reducers.updateRegistration

external interface RegistrationRowProps : Props {
    var me: Player
    var meIsHosting: Boolean
    var registration: ResolvedRegistration
}

val RegistrationRow = FC<RegistrationRowProps> { props ->
    val myStore = use(StoreContext)!!

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
            ButtonGroup {
                if (props.me.id == props.registration.dungeonMaster?.id) {
                    Button {
                        onClick = {
                            myStore.dispatch(
                                updateRegistration(
                                    props.registration.registration.eventId,
                                    props.registration.registration.playerId,
                                    null
                                )
                            )
                        }
                        size = Size.small
                        +"Kick"
                    }
                } else if (props.meIsHosting) {
                    Button {
                        onClick = {
                            myStore.dispatch(
                                updateRegistration(
                                    props.registration.registration.eventId,
                                    props.registration.registration.playerId,
                                    props.me.id
                                )
                            )
                        }
                        size = Size.small
                        +"Invite"
                    }
                }
            }
        }
    }
}