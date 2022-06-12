package components

import csstype.px
import mui.material.CircularProgress
import mui.material.FormControl
import mui.material.InputLabel
import mui.material.MenuItem
import mui.material.Select
import mui.system.Box
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.useContext
import react.useEffectOnce
import react.useState
import reducers.StoreContext
import reducers.selectGuild
import reducers.updateLinkedGuilds

val GuildSelector = FC<Props> {
    val store = useContext(StoreContext)
    val (selectedGuild, setSelectedGuild) = useState(store.state.calendar.selectedGuild)
    val (linkedGuilds, setLinkedGuilds) = useState(store.state.server.linkedGuilds)

    useEffectOnce {
        val unsubscribe = store.subscribe {
            setSelectedGuild(store.state.calendar.selectedGuild)
            setLinkedGuilds(store.state.server.linkedGuilds)
        }
        store.dispatch(updateLinkedGuilds())
        cleanup(unsubscribe)
    }

    Box {
        sx { minWidth = 120.px }
        if (linkedGuilds == null) {
            CircularProgress { }
        } else {
            if (linkedGuilds.isEmpty()) {
                GuildLinker { }
            } else {
                FormControl {
                    fullWidth = true
                    InputLabel {
                        id = "guild-select-label"
                        +"Guild"
                    }
                    Select {
                        id = "guild-select"
                        labelId = "guild-select-label"
                        value = selectedGuild?.id.unsafeCast<Nothing?>()
                        label = ReactNode("Guild")
                        onChange = { e, _ ->
                            val g = linkedGuilds.find { it.id.toString() == e.target.value }
                            if (g != null) store.dispatch(selectGuild(g))
                        }

                        linkedGuilds.forEach {
                            MenuItem {
                                value = it.id.toString()
                                +it.name
                            }
                        }
                    }
                }
            }
        }
    }
}
