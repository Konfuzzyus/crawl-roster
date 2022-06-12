package components

import api.fetchDiscordAccountInfo
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mui.icons.material.LinkOff
import mui.icons.material.QuestionMark
import mui.material.Chip
import mui.material.ChipVariant
import mui.material.CircularProgress
import mui.material.Link
import mui.system.Box
import org.codecranachan.roster.DiscordGuild
import org.codecranachan.roster.Guild
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useContext
import react.useEffectOnce
import react.useState
import reducers.StoreContext
import reducers.linkGuild

val GuildLinker = FC<Props> {
    val store = useContext(StoreContext)
    val (ownedGuilds, setOwnedGuilds) = useState<List<DiscordGuild>?>(null)
    val (linkedGuilds, setLinkedGuilds) = useState(store.state.server.linkedGuilds)

    useEffectOnce {
        MainScope().launch {
            val info = fetchDiscordAccountInfo()?.guilds ?: listOf()
            setOwnedGuilds(info.filter { it.owner })
        }
        val unsubscribe = store.subscribe { setLinkedGuilds(store.state.server.linkedGuilds) }
        cleanup(unsubscribe)
    }

    Box {
        if (ownedGuilds == null) {
            CircularProgress {}
        } else {
            if (ownedGuilds.isEmpty()) {
                Chip {
                    variant = ChipVariant.outlined
                    icon = QuestionMark.create()
                    label = ReactNode("You don't seem to own any guilds.")
                }
            } else {
                ownedGuilds.forEach { guild ->
                    if (linkedGuilds?.any { it.id.toString() == guild.id } == true) {
                        Chip {
                            variant = ChipVariant.filled
                            icon = Link.create()
                            label = ReactNode(guild.name)
                            onClick = {
                                store.dispatch(linkGuild(Guild(uuid4(), guild.name, guild.id)))
                                disabled = true
                            }
                        }
                    } else {
                        Chip {
                            variant = ChipVariant.filled
                            icon = LinkOff.create()
                            label = ReactNode(guild.name)
                            onClick = {
                                store.dispatch(linkGuild(Guild(uuid4(), guild.name, guild.id)))
                                disabled = true
                            }
                        }
                    }
                }
            }
        }
    }
}
