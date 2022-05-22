package components

import org.codecranachan.roster.Guild
import org.reduxkotlin.Store
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.select
import react.useEffectOnce
import react.useState
import reducers.ApplicationState
import reducers.selectGuild
import reducers.updateLinkedGuilds

external interface GuildSelectorProps : Props {
    var store: Store<ApplicationState>
    var selectedGuild: Guild?
}

val GuildSelector = FC<GuildSelectorProps> { props ->
    val selectedGuild = props.selectedGuild
    val (linkedGuilds, setLinkedGuilds) = useState(props.store.state.server.linkedGuilds)

    useEffectOnce {
        val unsubscribe = props.store.subscribe { setLinkedGuilds(props.store.state.server.linkedGuilds) }
        props.store.dispatch(updateLinkedGuilds())
        cleanup(unsubscribe)
    }

    div {
        if (linkedGuilds == null) {
            +"Waiting for linked guilds to load"
        } else {
            if (linkedGuilds.isEmpty()) {
                +"No guilds have been linked with this server - How about you link one of yours right now!"
                GuildLinker {
                    store = props.store
                }
            } else {
                label {
                    +"Showing event calendar for "
                    select {
                        value = selectedGuild?.name
                        onChange = { e ->
                            val g = linkedGuilds.find { it.id.toString() == e.currentTarget.value }
                            if (g != null) props.store.dispatch(selectGuild(g))
                        }
                        linkedGuilds.forEach {
                            option {
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
