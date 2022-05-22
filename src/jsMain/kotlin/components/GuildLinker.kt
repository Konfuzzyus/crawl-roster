package components

import api.fetchDiscordAccountInfo
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.codecranachan.roster.DiscordGuild
import org.codecranachan.roster.Guild
import org.reduxkotlin.Store
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.useEffectOnce
import react.useState
import reducers.ApplicationState
import reducers.linkGuild

external interface GuildLinkerProps : Props {
    var store: Store<ApplicationState>
}

val GuildLinker = FC<GuildLinkerProps> { props ->
    val (ownedGuilds, setOwnedGuilds) = useState<List<DiscordGuild>?>(null)

    useEffectOnce {
        MainScope().launch {
            val info = fetchDiscordAccountInfo()?.guilds ?: listOf()
            setOwnedGuilds(info.filter { it.owner })
        }
    }

    div {
        if (ownedGuilds == null) {
            +"Waiting for your guild memberships to load..."
        } else {
            if (ownedGuilds.isEmpty()) {
                +"You don't seem to own any guilds. You need to own a guild to link it."
            } else {
                ownedGuilds.forEach {
                    button {
                        +"Link ${it.name}"
                        onClick = { e ->
                            props.store.dispatch(linkGuild(Guild(uuid4(), it.name, it.id)))
                            disabled = true
                        }
                    }
                }
            }
        }
    }
}
