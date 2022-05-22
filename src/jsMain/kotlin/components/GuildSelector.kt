package components

import api.fetchLinkedGuilds
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.codecranachan.roster.Guild
import org.reduxkotlin.Store
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useEffectOnce
import react.useState
import reducers.ApplicationState

external interface GuildSelectorProps : Props {
    var store: Store<ApplicationState>
    var selectedGuild: Guild?
}

val GuildSelector = FC<GuildSelectorProps> { props ->
    val (linkedGuilds, setLinkedGuilds) = useState<List<Guild>?>(null)
    val selectedGuild = props.selectedGuild

    useEffectOnce {
        MainScope().launch {
            setLinkedGuilds(fetchLinkedGuilds() ?: listOf())
        }
    }

    div {
        if (selectedGuild == null) {
            if (linkedGuilds == null) {
                +"Waiting for linked guilds to load"
            } else {
                if (linkedGuilds.isEmpty()) {
                    +"No guilds have been linked. Link one."
                    GuildLinker {
                        store = props.store
                    }
                } else {
                    +"List of linked guilds goes here"
                }
            }
        } else {
            +"Listing Events in ${selectedGuild.name}"
        }
    }
}
