package components.editors

import api.fetchDiscordAccountInfo
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mui.icons.material.Cancel
import mui.icons.material.Domain
import mui.icons.material.LinkOff
import mui.icons.material.QuestionMark
import mui.icons.material.Save
import mui.material.Button
import mui.material.Chip
import mui.material.ChipVariant
import mui.material.CircularProgress
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.Link
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.system.Box
import org.codecranachan.roster.DiscordGuild
import org.codecranachan.roster.DiscordUserInfo
import org.codecranachan.roster.Guild
import org.codecranachan.roster.Server
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useContext
import react.useEffectOnce
import react.useState
import reducers.EditorClosed
import reducers.StoreContext
import reducers.linkGuild

val ServerEditor = FC<Props> {
    val store = useContext(StoreContext)
    var isOpen by useState(false)

    var settings by useState(Server())
    var accountInfo by useState<DiscordUserInfo?>(null)

    useEffectOnce {
        MainScope().launch {
            accountInfo = fetchDiscordAccountInfo()
        }
        val unsubscribe = store.subscribe {
            val s = store.state.ui.editorTarget
            if (s is Server) {
                isOpen = true
                settings = s
            } else {
                isOpen = false
            }
        }
        cleanup(unsubscribe)
    }

    Dialog {
        open = isOpen
        onClose = { _, _ ->
            store.dispatch(EditorClosed())
        }

        DialogTitle {
            +"Server Settings"
        }

        DialogContent {
            val info = accountInfo
            if (info == null) {
                CircularProgress {}
            } else {
                mui.material.List {
                    info.guilds.forEach { guild ->
                        if (guild.owner) ListItemIcon { Domain {} }
                        ListItemText {
                            +"${guild.name} ${if (guild.isAdmin()) "Admin" else "Peon"}"
                        }
                    }
                }
            }
        }
        DialogActions {
            Button {
                startIcon = Cancel.create()
                onClick = { _ ->
                    store.dispatch(EditorClosed())
                }
                +"Cancel"
            }
            Button {
                startIcon = Save.create()
                onClick = { _ ->
                    store.dispatch(EditorClosed())
                }
                +"Save Changes"
            }
        }
    }
}

val GuildLinker = FC<Props> {
    val store = useContext(StoreContext)
    val (ownedGuilds, setOwnedGuilds) = useState<List<DiscordGuild>?>(null)
    val (linkedGuilds, setLinkedGuilds) = useState(store.state.server.settings.guilds)

    useEffectOnce {
        MainScope().launch {
            val info = fetchDiscordAccountInfo()?.guilds ?: listOf()
            setOwnedGuilds(info.filter { it.owner })
        }
        val unsubscribe = store.subscribe { setLinkedGuilds(store.state.server.settings.guilds) }
        cleanup(unsubscribe)
    }

    Box {
        if (ownedGuilds == null) {
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