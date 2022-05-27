package components

import csstype.px
import mui.icons.material.ErrorOutline
import mui.icons.material.Person
import mui.material.*
import mui.system.sx
import org.codecranachan.roster.Event
import react.*
import reducers.StoreContext

external interface EventDetailsProps : Props {
    var event: Event
}

val EventDetails = FC<EventDetailsProps> { props ->
    val store = useContext(StoreContext)

    if (props.event.registeredPlayers.isEmpty()) {
        Chip {
            icon = ErrorOutline.create()
            label = ReactNode("No one has registered for this event")
        }
    } else {
        Paper {
            sx {
                marginBlock = 2.px
            }
            TableContainer {
                Table {
                    size = Size.small
                    TableHead {
                        TableRow {
                            TableCell { +"Player Name" }
                            TableCell { +"Discord Handle" }
                            TableCell { +"Character" }
                            TableCell { +"Table" }
                        }
                    }
                    TableBody {
                        props.event.registeredPlayers.forEach { player ->
                            val me = store.state.identity.data?.profile

                            TableRow {
                                TableCell {
                                    if (me?.id == player.id) {
                                        Chip {
                                            variant = ChipVariant.outlined
                                            label = ReactNode(player.name)
                                            icon = Person.create()
                                        }
                                    } else {
                                        +"${player.name}"
                                    }
                                }
                                TableCell {
                                    +"${player.discordHandle}"
                                }
                                TableCell {}
                                TableCell {}
                            }
                        }
                    }
                }
            }
        }
    }
}