package components

import mui.material.FormControl
import mui.material.InputLabel
import mui.material.MenuItem
import mui.material.Select
import mui.system.Box
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.use
import react.useEffectOnceWithCleanup
import react.useState
import reducers.StoreContext
import reducers.selectGuild
import reducers.updateServerSettings
import web.cssom.px

val GuildSelector = FC<Props> {
    val store = use(StoreContext)!!
    val (selectedGuild, setSelectedGuild) = useState(store.state.calendar.selectedLinkedGuild)
    val (linkedGuilds, setLinkedGuilds) = useState(store.state.server.settings.linkedGuilds)

    useEffectOnceWithCleanup {
        val unsubscribe = store.subscribe {
            setSelectedGuild(store.state.calendar.selectedLinkedGuild)
            setLinkedGuilds(store.state.server.settings.linkedGuilds)
        }
        store.dispatch(updateServerSettings())
        onCleanup(unsubscribe)
    }

    Box {
        sx { minWidth = 120.px }
        if (linkedGuilds.isEmpty()) {
            +"There are no guilds linked to this server"
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
                        web.dom.document
                        web.window.window
                        kotlinx.browser.window.navigator as web.navigator.Navigator
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
